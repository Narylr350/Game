package server.auth;

import util.AuthJdbcUtil;

import java.sql.DatabaseMetaData;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

public class JdbcUserRepository implements UserRepository {
    private static final String LOGIN_TIME_COLUMN = "last_login_time";

    @Override
    public Optional<UserAccount> findByUsername(String username) {
        Connection connection = AuthJdbcUtil.getConnection();
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            if (connection == null) {
                throw new RepositoryAccessException("database unavailable");
            }
            ensureLoginTimeColumn(connection);
            String sql = "select id,username,password,status,last_login_time from user where username = ?";
            statement = connection.prepareStatement(sql);
            statement.setString(1, username);
            resultSet = statement.executeQuery();
            if (!resultSet.next()) {
                return Optional.empty();
            }
            Timestamp loginTimestamp = resultSet.getTimestamp(LOGIN_TIME_COLUMN);
            return Optional.of(new UserAccount(
                    resultSet.getString("id"),
                    resultSet.getString("username"),
                    resultSet.getString("password"),
                    resultSet.getInt("status") == 1,
                    loginTimestamp == null ? null : loginTimestamp.toLocalDateTime()
            ));
        } catch (SQLException e) {
            throw new RepositoryAccessException("query user failed", e);
        } finally {
            AuthJdbcUtil.close(resultSet, statement, connection);
        }
    }

    @Override
    public void save(UserAccount userAccount) {
        Connection connection = AuthJdbcUtil.getConnection();
        PreparedStatement statement = null;
        try {
            if (connection == null) {
                throw new RepositoryAccessException("database unavailable");
            }
            ensureLoginTimeColumn(connection);
            String sql = "insert into user(id,username,password,status,last_login_time) values(?,?,?,?,?)";
            statement = connection.prepareStatement(sql);
            statement.setString(1, userAccount.getId());
            statement.setString(2, userAccount.getUsername());
            statement.setString(3, userAccount.getPassword());
            statement.setInt(4, userAccount.isStatus() ? 1 : 0);
            if (userAccount.getLastLoginAt() == null) {
                statement.setTimestamp(5, null);
            } else {
                statement.setTimestamp(5, Timestamp.valueOf(userAccount.getLastLoginAt()));
            }
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryAccessException("save user failed", e);
        } finally {
            AuthJdbcUtil.close(null, statement, connection);
        }
    }

    @Override
    public void updateLoginTime(String userId, LocalDateTime loginTime) {
        Connection connection = AuthJdbcUtil.getConnection();
        PreparedStatement statement = null;
        try {
            if (connection == null) {
                throw new RepositoryAccessException("database unavailable");
            }
            ensureLoginTimeColumn(connection);
            statement = connection.prepareStatement("update user set last_login_time = ? where id = ?");
            statement.setTimestamp(1, Timestamp.valueOf(loginTime));
            statement.setString(2, userId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryAccessException("update login time failed", e);
        } finally {
            AuthJdbcUtil.close(null, statement, connection);
        }
    }

    private void ensureLoginTimeColumn(Connection connection) {
        if (hasLoginTimeColumn(connection)) {
            return;
        }

        Statement statement = null;
        try {
            statement = connection.createStatement();
            statement.executeUpdate("alter table user add column last_login_time datetime null");
        } catch (SQLException e) {
            throw new RepositoryAccessException("ensure login time column failed", e);
        } finally {
            AuthJdbcUtil.close(null, statement, null);
        }
    }

    private boolean hasLoginTimeColumn(Connection connection) {
        ResultSet resultSet = null;
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            resultSet = metaData.getColumns(connection.getCatalog(), null, "user", LOGIN_TIME_COLUMN);
            return resultSet.next();
        } catch (SQLException e) {
            throw new RepositoryAccessException("check login time column failed", e);
        } finally {
            AuthJdbcUtil.close(resultSet, null, null);
        }
    }
}
