import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    // 数据库连接信息
    private static final String URL = "jdbc:mysql://117.72.60.69:3306/conference_room_booking";
    private static final String USER = "conference_room_booking";
    private static final String PASSWORD = "rainknows";

    // 获取数据库连接
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // 关闭资源
    public static void closeResources(Connection conn, Statement stmt, ResultSet rs) {
        try {
            if (rs != null)
                rs.close();
            if (stmt != null)
                stmt.close();
            if (conn != null)
                conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 验证用户登录
    public User checkLogin(String username, String password) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        User user = null;

        try {
            conn = getConnection();
            // 查询用户表验证用户名和密码
            String sql = "SELECT userId, username, role, email, phone FROM user WHERE username = ? AND password = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            rs = stmt.executeQuery();

            // 如果结果集有数据则登录成功,并创建User对象
            if (rs.next()) {
                int userId = rs.getInt("userId");
                String role = rs.getString("role");
                String email = rs.getString("email");
                String phone = rs.getString("phone");
                user = new User(userId, username, role, email, phone);
            }
        } catch (SQLException e) {
            // 向上抛出异常，由调用者处理
            throw new SQLException("数据库验证失败", e);
        } finally {
            closeResources(conn, stmt, rs);
        }

        return user;
    }

    /**
     * 更新用户个人资料（邮箱和电话）。
     * 
     * @param userId 用户ID
     * @param email  新邮箱
     * @param phone  新电话
     * @return 如果更新成功，返回 true
     * @throws SQLException 数据库访问异常
     */
    public boolean updateUserProfile(int userId, String email, String phone) throws SQLException {
        String sql = "UPDATE user SET email = ?, phone = ? WHERE userId = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setString(2, phone);
            pstmt.setInt(3, userId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * 修改用户密码。
     * 
     * @param userId      用户ID
     * @param oldPassword 旧密码，用于验证
     * @param newPassword 新密码
     * @return 如果密码修改成功，返回 true。如果旧密码错误，返回 false。
     * @throws SQLException 数据库访问异常
     */
    public boolean changePassword(int userId, String oldPassword, String newPassword) throws SQLException {
        // 首先，验证旧密码是否正确
        String checkSql = "SELECT password FROM user WHERE userId = ? AND password = ?";
        try (Connection conn = getConnection();
                PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

            checkStmt.setInt(1, userId);
            checkStmt.setString(2, oldPassword);

            try (ResultSet rs = checkStmt.executeQuery()) {
                if (!rs.next()) {
                    return false; // 旧密码不匹配
                }
            }

            // 旧密码验证通过，更新为新密码
            String updateSql = "UPDATE user SET password = ? WHERE userId = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setString(1, newPassword);
                updateStmt.setInt(2, userId);
                int affectedRows = updateStmt.executeUpdate();
                return affectedRows > 0;
            }
        }
    }

    // --- Admin-specific User Management ---

    /**
     * 获取所有用户的列表（可选择排除某个用户，如当前管理员自己）。
     * 
     * @param excludeUserId 要排除的用户ID，如果不想排除任何用户，可以传入 -1 或 0。
     * @return 用户对象列表
     * @throws SQLException 数据库访问异常
     */
    public List<User> getAllUsers(int excludeUserId) throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT userId, username, role, email, phone FROM user WHERE userId != ? ORDER BY username";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, excludeUserId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    users.add(new User(
                            rs.getInt("userId"),
                            rs.getString("username"),
                            rs.getString("role"),
                            rs.getString("email"),
                            rs.getString("phone")));
                }
            }
        }
        return users;
    }

    /**
     * 管理员添加一个新用户。
     * 
     * @param user     要添加的用户对象（不含ID）
     * @param password 初始密码
     * @return 如果添加成功，返回 true
     * @throws SQLException 数据库访问异常
     */
    public boolean addUser(User user, String password) throws SQLException {
        String sql = "INSERT INTO user (username, password, role, email, phone) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, password);
            pstmt.setString(3, user.getRole());
            pstmt.setString(4, user.getEmail());
            pstmt.setString(5, user.getPhone());
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * 管理员更新用户信息（不包括密码）。
     * 
     * @param user 包含更新信息的用户对象
     * @return 如果更新成功，返回 true
     * @throws SQLException 数据库访问异常
     */
    public boolean updateUserByAdmin(User user) throws SQLException {
        String sql = "UPDATE user SET username = ?, role = ?, email = ?, phone = ? WHERE userId = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getRole());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getPhone());
            pstmt.setInt(5, user.getUserId());
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * 管理员重置用户密码。
     * 
     * @param userId      用户ID
     * @param newPassword 新的密码
     * @return 如果重置成功，返回 true
     * @throws SQLException 数据库访问异常
     */
    public boolean resetPasswordByAdmin(int userId, String newPassword) throws SQLException {
        String sql = "UPDATE user SET password = ? WHERE userId = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newPassword);
            pstmt.setInt(2, userId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * 删除一个用户。
     * 
     * @param userId 要删除的用户ID
     * @return 如果删除成功，返回 true
     * @throws SQLException 数据库访问异常
     */
    public boolean deleteUser(int userId) throws SQLException {
        // 警告: 在生产环境中，直接删除用户可能导致外键约束问题（如他们的预订记录）。
        // 更好的做法可能是将用户标记为 "deactivated"。
        // 为简化起见，这里执行硬删除。
        String sql = "DELETE FROM user WHERE userId = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }
}
