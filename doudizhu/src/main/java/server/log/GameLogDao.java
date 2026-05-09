package server.log;

import util.AuthJdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GameLogDao {
    public static final String NORMAL_SETTLEMENT = "NORMAL_SETTLEMENT";
    public static final String PLAYER_DISCONNECTED = "PLAYER_DISCONNECTED";
    public static final String PLAYER_EXIT_AFTER_SETTLEMENT = "PLAYER_EXIT_AFTER_SETTLEMENT";
    public static final String LANDLORD = "LANDLORD";
    public static final String FARMER = "FARMER";

    private final Map<String, Integer> stepCounters = new HashMap<>();

    public GameLogDao() {
        ensureTables();
    }

    public String startSession(List<String> playerNames) {
        String sessionId = UUID.randomUUID().toString();
        executeUpdate(
                "insert into game_session_log(session_id, started_at, ended_at, player1_name, player2_name, player3_name, landlord_player_id, winner_player_id, winner_side, end_reason) values(?,?,?,?,?,?,?,?,?,?)",
                statement -> {
                    statement.setString(1, sessionId);
                    statement.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                    statement.setTimestamp(3, null);
                    statement.setString(4, playerNames.get(0));
                    statement.setString(5, playerNames.get(1));
                    statement.setString(6, playerNames.get(2));
                    statement.setObject(7, null);
                    statement.setObject(8, null);
                    statement.setString(9, null);
                    statement.setString(10, null);
                }
        );
        stepCounters.put(sessionId, 0);
        return sessionId;
    }

    public void appendAction(String sessionId,
                             String phase,
                             int playerId,
                             String playerName,
                             String actionInput,
                             String actionResult,
                             int remainingCardsP1,
                             int remainingCardsP2,
                             int remainingCardsP3) {
        int stepNo = stepCounters.compute(sessionId, (key, current) -> current == null ? 1 : current + 1);
        executeUpdate(
                "insert into game_action_log(session_id, step_no, phase, player_id, player_name, action_input, action_result, remaining_cards_p1, remaining_cards_p2, remaining_cards_p3, created_at) values(?,?,?,?,?,?,?,?,?,?,?)",
                statement -> {
                    statement.setString(1, sessionId);
                    statement.setInt(2, stepNo);
                    statement.setString(3, phase);
                    statement.setInt(4, playerId);
                    statement.setString(5, playerName);
                    statement.setString(6, actionInput);
                    statement.setString(7, actionResult);
                    statement.setInt(8, remainingCardsP1);
                    statement.setInt(9, remainingCardsP2);
                    statement.setInt(10, remainingCardsP3);
                    statement.setTimestamp(11, Timestamp.valueOf(LocalDateTime.now()));
                }
        );
    }

    public void updateLandlordPlayerId(String sessionId, Integer landlordPlayerId) {
        executeUpdate(
                "update game_session_log set landlord_player_id = ? where session_id = ?",
                statement -> {
                    statement.setObject(1, landlordPlayerId);
                    statement.setString(2, sessionId);
                }
        );
    }

    public void finishSession(String sessionId,
                              Integer landlordPlayerId,
                              String winnerSide,
                              Integer winnerPlayerId,
                              String endReason) {
        executeUpdate(
                "update game_session_log set landlord_player_id = ?, winner_side = ?, winner_player_id = ?, end_reason = ?, ended_at = ? where session_id = ?",
                statement -> {
                    statement.setObject(1, landlordPlayerId);
                    statement.setString(2, winnerSide);
                    statement.setObject(3, winnerPlayerId);
                    statement.setString(4, endReason);
                    statement.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                    statement.setString(6, sessionId);
                }
        );
        stepCounters.remove(sessionId);
    }

    static String sessionTableName() {
        return "game_session_log";
    }

    static String actionTableName() {
        return "game_action_log";
    }

    private void ensureTables() {
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

    @FunctionalInterface
    private interface StatementBinder {
        void bind(PreparedStatement statement) throws SQLException;
    }
}
