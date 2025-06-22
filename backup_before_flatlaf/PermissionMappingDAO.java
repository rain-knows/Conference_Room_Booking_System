import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 权限映射DAO类
 * 用于管理用户角色和会议室类型权限映射的数据库操作
 */
public class PermissionMappingDAO {
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

    /**
     * 获取所有权限映射
     */
    public List<PermissionMapping> getAllPermissionMappings() throws SQLException {
        List<PermissionMapping> mappings = new ArrayList<>();
        String sql = "SELECT * FROM PermissionMapping ORDER BY userRole, roomTypeCode";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                PermissionMapping mapping = new PermissionMapping(
                        rs.getInt("mappingId"),
                        rs.getString("userRole"),
                        rs.getString("roomTypeCode"),
                        rs.getBoolean("canBook"),
                        rs.getBoolean("canView"),
                        rs.getBoolean("canManage"),
                        rs.getString("description"),
                        rs.getString("createTime"),
                        rs.getString("updateTime"));
                mappings.add(mapping);
            }
        }
        return mappings;
    }

    /**
     * 根据用户角色和会议室类型代码获取权限映射
     */
    public PermissionMapping getPermissionMapping(String userRole, String roomTypeCode) throws SQLException {
        String sql = "SELECT * FROM PermissionMapping WHERE userRole = ? AND roomTypeCode = ?";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userRole);
            pstmt.setString(2, roomTypeCode);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new PermissionMapping(
                            rs.getInt("mappingId"),
                            rs.getString("userRole"),
                            rs.getString("roomTypeCode"),
                            rs.getBoolean("canBook"),
                            rs.getBoolean("canView"),
                            rs.getBoolean("canManage"),
                            rs.getString("description"),
                            rs.getString("createTime"),
                            rs.getString("updateTime"));
                }
            }
        }
        return null;
    }

    /**
     * 添加新的权限映射
     */
    public boolean addPermissionMapping(PermissionMapping mapping) throws SQLException {
        String sql = "INSERT INTO PermissionMapping (userRole, roomTypeCode, canBook, canView, canManage, description) "
                +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, mapping.getUserRole());
            pstmt.setString(2, mapping.getRoomTypeCode());
            pstmt.setBoolean(3, mapping.canBook());
            pstmt.setBoolean(4, mapping.canView());
            pstmt.setBoolean(5, mapping.canManage());
            pstmt.setString(6, mapping.getDescription());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * 更新权限映射
     */
    public boolean updatePermissionMapping(PermissionMapping mapping) throws SQLException {
        String sql = "UPDATE PermissionMapping SET canBook = ?, canView = ?, canManage = ?, " +
                "description = ?, updateTime = CURRENT_TIMESTAMP " +
                "WHERE mappingId = ?";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBoolean(1, mapping.canBook());
            pstmt.setBoolean(2, mapping.canView());
            pstmt.setBoolean(3, mapping.canManage());
            pstmt.setString(4, mapping.getDescription());
            pstmt.setInt(5, mapping.getMappingId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * 删除权限映射
     */
    public boolean deletePermissionMapping(int mappingId) throws SQLException {
        String sql = "DELETE FROM PermissionMapping WHERE mappingId = ?";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, mappingId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * 检查用户是否有权限预订指定类型的会议室
     */
    public boolean canUserBookRoomType(String userRole, String roomTypeCode) throws SQLException {
        PermissionMapping mapping = getPermissionMapping(userRole, roomTypeCode);
        return mapping != null && mapping.canBook();
    }

    /**
     * 检查用户是否有权限查看指定类型的会议室
     */
    public boolean canUserViewRoomType(String userRole, String roomTypeCode) throws SQLException {
        PermissionMapping mapping = getPermissionMapping(userRole, roomTypeCode);
        return mapping != null && mapping.canView();
    }

    /**
     * 检查用户是否有权限管理指定类型的会议室
     */
    public boolean canUserManageRoomType(String userRole, String roomTypeCode) throws SQLException {
        PermissionMapping mapping = getPermissionMapping(userRole, roomTypeCode);
        return mapping != null && mapping.canManage();
    }

    /**
     * 获取用户角色可访问的会议室类型代码列表
     */
    public List<String> getAccessibleRoomTypeCodes(String userRole) throws SQLException {
        List<String> roomTypeCodes = new ArrayList<>();
        String sql = "SELECT roomTypeCode FROM PermissionMapping WHERE userRole = ? AND (canView = 1 OR canBook = 1 OR canManage = 1)";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userRole);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    roomTypeCodes.add(rs.getString("roomTypeCode"));
                }
            }
        }
        return roomTypeCodes;
    }

    /**
     * 初始化默认权限映射
     */
    public void initializeDefaultPermissions() throws SQLException {
        // 检查是否已有权限映射数据
        String checkSql = "SELECT COUNT(*) FROM PermissionMapping";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(checkSql);
                ResultSet rs = pstmt.executeQuery()) {

            if (rs.next() && rs.getInt(1) > 0) {
                return; // 已有数据，不需要初始化
            }
        }

        // 添加默认权限映射
        List<PermissionMapping> defaultMappings = new ArrayList<>();

        // SYSTEM_ADMIN 拥有所有权限
        defaultMappings.add(new PermissionMapping("SYSTEM_ADMIN", "BASIC", true, true, true, "系统管理员可完全管理基础会议室"));
        defaultMappings.add(new PermissionMapping("SYSTEM_ADMIN", "PREMIUM", true, true, true, "系统管理员可完全管理高级会议室"));
        defaultMappings.add(new PermissionMapping("SYSTEM_ADMIN", "VIP", true, true, true, "系统管理员可完全管理VIP会议室"));

        // LEADER 拥有大部分权限
        defaultMappings.add(new PermissionMapping("LEADER", "BASIC", true, true, false, "领导可预订和查看基础会议室"));
        defaultMappings.add(new PermissionMapping("LEADER", "PREMIUM", true, true, false, "领导可预订和查看高级会议室"));
        defaultMappings.add(new PermissionMapping("LEADER", "VIP", true, true, false, "领导可预订和查看VIP会议室"));

        // NORMAL_EMPLOYEE 只有基础权限
        defaultMappings.add(new PermissionMapping("NORMAL_EMPLOYEE", "BASIC", true, true, false, "普通员工可预订和查看基础会议室"));
        defaultMappings.add(new PermissionMapping("NORMAL_EMPLOYEE", "PREMIUM", false, true, false, "普通员工只能查看高级会议室"));
        defaultMappings.add(new PermissionMapping("NORMAL_EMPLOYEE", "VIP", false, false, false, "普通员工无VIP会议室权限"));

        // 批量插入默认权限映射
        for (PermissionMapping mapping : defaultMappings) {
            addPermissionMapping(mapping);
        }
    }
}