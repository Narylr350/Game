package server.log;

import util.AuthJdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

public class JdbcGameLogRepository implements GameLogRepository {
    @Override
    public void ensureTables() {
        Connection connection = AuthJdbcUtil.getConnection();
        Statement statement = null;
        try {
            if (connection == null) {
                throw new IllegalStateException("database unavailable");
            }
            statement = connection.createStatement();
            statement.executeUpdate("""
                    create table if not exists game_session_log (
                        session_id varchar(64) primary key,
                        started_at datetime not null,
                        ended_at datetime null,
                        player1_name varchar(64) not null,
                        player2_name varchar(64) not null,
                        player3_name varchar(64) not null,
                        landlord_player_id int null,
                        winner_player_id int null,
                        winner_side varchar(16) null,
                        end_reason varchar(32) null
                    )
                    """);
            statement.executeUpdate("""
                    create table if not exists game_action_log (
                        id bigint primary key auto_increment,
                        session_id varchar(64) not null,
                        step_no int not null,
                        phase varchar(32) not null,
                        player_id int not null,
                        player_name varchar(64) not null,
                        action_input varchar(255) not null,
                        action_result varchar(255) not null,
                        remaining_cards_p1 int not null,
                        remaining_cards_p2 int not null,
                        remaining_cards_p3 int not null,
                        created_at datetime not null
                    )
                    """);
        } catch (SQLException e) {
            throw new IllegalStateException("ensure game log tables failed", e);
        } finally {
            AuthJdbcUtil.close(null, statement, connection);
        }
    }

    @Override
    public void insertSession(GameSessionLog sessionLog) {
        executeUpdate(
                "insert into game_session_log(session_id, started_at, ended_at, player1_name, player2_name, player3_name, landlord_player_id, winner_player_id, winner_side, end_reason) values(?,?,?,?,?,?,?,?,?,?)",
                statement -> {
                    statement.setString(1, sessionLog.sessionId());
                    statement.setTimestamp(2, Timestamp.valueOf(sessionLog.startedAt()));
                    statement.setTimestamp(3, nullableTimestamp(sessionLog.endedAt()));
                    statement.setString(4, sessionLog.player1Name());
                    statement.setString(5, sessionLog.player2Name());
                    statement.setString(6, sessionLog.player3Name());
                    statement.setObject(7, sessionLog.landlordPlayerId());
                    statement.setObject(8, sessionLog.winnerPlayerId());
                    statement.setString(9, sessionLog.winnerSide() == null ? null : sessionLog.winnerSide().name());
                    statement.setString(10, sessionLog.endReason() == null ? null : sessionLog.endReason().name());
                }
        );
    }

    @Override
    public void insertAction(GameActionLog actionLog) {
        executeUpdate(
                "insert into game_action_log(session_id, step_no, phase, player_id, player_name, action_input, action_result, remaining_cards_p1, remaining_cards_p2, remaining_cards_p3, created_at) values(?,?,?,?,?,?,?,?,?,?,?)",
                statement -> {
                    statement.setString(1, actionLog.sessionId());
                    statement.setInt(2, actionLog.stepNo());
                    statement.setString(3, actionLog.phase());
                    statement.setInt(4, actionLog.playerId());
                    statement.setString(5, actionLog.playerName());
                    statement.setString(6, actionLog.actionInput());
                    statement.setString(7, actionLog.actionResult());
                    statement.setInt(8, actionLog.remainingCardsP1());
                    statement.setInt(9, actionLog.remainingCardsP2());
                    statement.setInt(10, actionLog.remainingCardsP3());
                    statement.setTimestamp(11, Timestamp.valueOf(actionLog.createdAt()));
                }
        );
    }

    @Override
    public void finishSession(String sessionId,
                              Integer landlordPlayerId,
                              WinnerSide winnerSide,
                              Integer winnerPlayerId,
                              GameEndReason endReason,
                              LocalDateTime endedAt) {
        executeUpdate(
                "update game_session_log set landlord_player_id = ?, winner_side = ?, winner_player_id = ?, end_reason = ?, ended_at = ? where session_id = ?",
                statement -> {
                    statement.setObject(1, landlordPlayerId);
                    statement.setString(2, winnerSide == null ? null : winnerSide.name());
                    statement.setObject(3, winnerPlayerId);
                    statement.setString(4, endReason == null ? null : endReason.name());
                    statement.setTimestamp(5, nullableTimestamp(endedAt));
                    statement.setString(6, sessionId);
                }
        );
    }

    @Override
    public void updateLandlordPlayerId(String sessionId, Integer landlordPlayerId) {
        executeUpdate(
                "update game_session_log set landlord_player_id = ? where session_id = ?",
                statement -> {
                    statement.setObject(1, landlordPlayerId);
                    statement.setString(2, sessionId);
                }
        );
    }

    @Override
    public Optional<GameSessionLog> findSession(String sessionId) {
        Connection connection = AuthJdbcUtil.getConnection();
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            if (connection == null) {
                throw new IllegalStateException("database unavailable");
            }
            statement = connection.prepareStatement("select * from game_session_log where session_id = ?");
            statement.setString(1, sessionId);
            resultSet = statement.executeQuery();
            if (!resultSet.next()) {
                return Optional.empty();
            }
            return Optional.of(new GameSessionLog(
                    resultSet.getString("session_id"),
                    resultSet.getTimestamp("started_at").toLocalDateTime(),
                    resultSet.getTimestamp("ended_at") == null ? null : resultSet.getTimestamp("ended_at").toLocalDateTime(),
                    resultSet.getString("player1_name"),
                    resultSet.getString("player2_name"),
                    resultSet.getString("player3_name"),
                    (Integer) resultSet.getObject("landlord_player_id"),
                    (Integer) resultSet.getObject("winner_player_id"),
                    resultSet.getString("winner_side") == null ? null : WinnerSide.valueOf(resultSet.getString("winner_side")),
                    resultSet.getString("end_reason") == null ? null : GameEndReason.valueOf(resultSet.getString("end_reason"))
            ));
        } catch (SQLException e) {
            throw new IllegalStateException("query game session failed", e);
        } finally {
            AuthJdbcUtil.close(resultSet, statement, connection);
        }
    }

    static String sessionTableName() {
        return "game_session_log";
    }

    static String actionTableName() {
        return "game_action_log";
    }

    private void executeUpdate(String sql, StatementBinder binder) {
        Connection connection = AuthJdbcUtil.getConnection();
        PreparedStatement statement = null;
        try {
            if (connection == null) {
                throw new IllegalStateException("database unavailable");
            }
            statement = connection.prepareStatement(sql);
            binder.bind(statement);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("game log update failed", e);
        } finally {
            AuthJdbcUtil.close(null, statement, connection);
        }
    }

    private Timestamp nullableTimestamp(LocalDateTime value) {
        return value == null ? null : Timestamp.valueOf(value);
    }

    @FunctionalInterface
    private interface StatementBinder {
        void bind(PreparedStatement statement) throws SQLException;
    }
}
