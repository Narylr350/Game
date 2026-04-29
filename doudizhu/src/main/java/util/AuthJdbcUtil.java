package util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public final class AuthJdbcUtil {
    private static final String CONFIG_FILE = "db.properties";
    private static final String DEFAULT_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/doudizhu";
    private static final String DEFAULT_USERNAME = "root";
    private static final String DEFAULT_PASSWORD = "200518";

    private AuthJdbcUtil() {
    }

    public static Connection getConnection() {
        Connection connection = null;
        DatabaseConfig config = loadConfig();
        try {
            Class.forName(config.driver());
            connection = DriverManager.getConnection(config.url(), config.username(), config.password());
        } catch (ClassNotFoundException e) {
            System.out.println("加载驱动失败");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("连接失败");
            e.printStackTrace();
        }
        return connection;
    }

    private static DatabaseConfig loadConfig() {
        Properties properties = new Properties();
        try (InputStream inputStream = AuthJdbcUtil.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (IOException e) {
            System.out.println("读取数据库配置失败，使用默认配置");
            e.printStackTrace();
        }

        return new DatabaseConfig(
                properties.getProperty("db.driver", DEFAULT_DRIVER),
                properties.getProperty("db.url", DEFAULT_URL),
                properties.getProperty("db.username", DEFAULT_USERNAME),
                properties.getProperty("db.password", DEFAULT_PASSWORD)
        );
    }

    public static void close(ResultSet resultSet, Statement statement, Connection connection) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private record DatabaseConfig(String driver, String url, String username, String password) {
    }
}
