package server.auth;

import util.AuthJdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class JdbcUserRepository implements UserRepository {

    @Override
    public Optional<UserAccount> findByUsername(String username) {
        Connection connection = AuthJdbcUtil.getConnection();
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        String sql = "select id,username,password,status from user where username = ?";
        try {
            if (connection == null) {
                throw new RepositoryAccessException("database unavailable");
            }
            statement = connection.prepareStatement(sql);
            statement.setString(1, username);
            resultSet = statement.executeQuery();
            if (!resultSet.next()) {
                return Optional.empty();
            }
            return Optional.of(new UserAccount(
                    resultSet.getString("id"),
                    resultSet.getString("username"),
                    resultSet.getString("password"),
                    resultSet.getInt("status") == 1
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
        String sql = "insert into user(id,username,password,status) values(?,?,?,?)";
        try {
            if (connection == null) {
                throw new RepositoryAccessException("database unavailable");
            }
            statement = connection.prepareStatement(sql);
            statement.setString(1, userAccount.getId());
            statement.setString(2, userAccount.getUsername());
            statement.setString(3, userAccount.getPassword());
            statement.setInt(4, userAccount.isStatus() ? 1 : 0);
            statement.executeUpdate();
            // TODO 后续在这里补登录时间持久化，用数据库时间字段支撑一周登录态。
        } catch (SQLException e) {
            throw new RepositoryAccessException("save user failed", e);
        } finally {
            AuthJdbcUtil.close(null, statement, connection);
        }
    }
}
