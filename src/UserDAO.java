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
            // 查询用户表验证用户名和密码，只允许激活状态的用户登录
            String sql = "SELECT userId, username, role, email, phone, active FROM user WHERE username = ? AND password = ? AND active = 1";
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
                boolean active = rs.getBoolean("active");
                user = new User(userId, username, role, email, phone, active);
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
        String sql = "SELECT userId, username, role, email, phone, active FROM user WHERE userId != ? ORDER BY username";
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
                            rs.getString("phone"),
                            rs.getBoolean("active")));
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
        String sql = "INSERT INTO user (username, password, role, email, phone, active) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, password);
            pstmt.setString(3, user.getRole());
            pstmt.setString(4, user.getEmail());
            pstmt.setString(5, user.getPhone());
            pstmt.setBoolean(6, user.isActive());
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
        String sql = "UPDATE user SET username = ?, role = ?, email = ?, phone = ?, active = ? WHERE userId = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getRole());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getPhone());
            pstmt.setBoolean(5, user.isActive());
            pstmt.setInt(6, user.getUserId());
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
     * 切换用户激活状态。
     * 
     * @param userId 用户ID
     * @param active 新的激活状态
     * @return 如果更新成功，返回 true
     * @throws SQLException 数据库访问异常
     */
    public boolean toggleUserStatus(int userId, boolean active) throws SQLException {
        String sql = "UPDATE user SET active = ? WHERE userId = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, active);
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
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false); // 开启事务
            
            // 首先删除用户相关的预订记录
            String deleteReservationsSql = "DELETE FROM reservation WHERE userId = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteReservationsSql)) {
                pstmt.setInt(1, userId);
                pstmt.executeUpdate();
            }
            
            // 然后删除用户记录
            String deleteUserSql = "DELETE FROM user WHERE userId = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteUserSql)) {
                pstmt.setInt(1, userId);
                int affectedRows = pstmt.executeUpdate();
                
                if (affectedRows > 0) {
                    conn.commit(); // 提交事务
                    return true;
                } else {
                    conn.rollback(); // 回滚事务
                    return false;
                }
            }
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // 发生异常时回滚事务
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            throw e; // 重新抛出异常
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // 恢复自动提交
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
