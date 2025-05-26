// filepath: d:\code\java_code\jform\src\UserDAO.java
import java.sql.*;

public class UserDAO {
    // 数据库连接信息
    private static final String URL = "jdbc:mysql://117.72.60.69:3306/rainknows";
    private static final String USER = "rainknows";
    private static final String PASSWORD = "rainknows";

    // 获取数据库连接
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // 关闭资源
    public static void closeResources(Connection conn, Statement stmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 验证用户登录
    public boolean checkLogin(String username, String password) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        boolean success = false;

        try {
            conn = getConnection();
            // 查询用户表验证用户名和密码
            String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            rs = stmt.executeQuery();

            // 如果结果集有数据则登录成功
            if (rs.next()) {
                success = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, stmt, rs);
        }

        return success;
    }
}
