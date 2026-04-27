package client.JDBC;

import java.sql.*;

public class JDBCUtil {

    //加载驱动获取连接
    public static Connection getConnection(){
        Connection con = null;
        String url = "jdbc:mysql://localhost:3306/doudizhu";
        String username = "root";
        String password = "666666";
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            //2、获取连接
            con = DriverManager.getConnection(url,username,password);
        } catch (ClassNotFoundException e) {
            System.out.println("加载驱动失败");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("连接失败");
            e.printStackTrace();
        }
        return con;
    }

    //释放资源
    public static void close(ResultSet rs, Statement ps, Connection con){

        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (ps != null) {
            try {
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}

