import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 会议室类型DAO类
 * 用于管理会议室类型的数据库操作
 */
public class RoomTypeDAO {
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
     * 获取所有会议室类型
     */
    public List<RoomType> getAllRoomTypes() throws SQLException {
        List<RoomType> roomTypes = new ArrayList<>();
        String sql = "SELECT * FROM RoomType ORDER BY roomTypeId";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                RoomType roomType = new RoomType(
                        rs.getInt("roomTypeId"),
                        rs.getString("typeName"),
                        rs.getString("typeCode"),
                        rs.getString("description"),
                        rs.getString("createTime"),
                        rs.getString("updateTime"));
                roomTypes.add(roomType);
            }
        }
        return roomTypes;
    }

    /**
     * 根据ID获取会议室类型
     */
    public RoomType getRoomTypeById(int roomTypeId) throws SQLException {
        String sql = "SELECT * FROM RoomType WHERE roomTypeId = ?";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, roomTypeId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new RoomType(
                            rs.getInt("roomTypeId"),
                            rs.getString("typeName"),
                            rs.getString("typeCode"),
                            rs.getString("description"),
                            rs.getString("createTime"),
                            rs.getString("updateTime"));
                }
            }
        }
        return null;
    }

    /**
     * 根据类型代码获取会议室类型
     */
    public RoomType getRoomTypeByCode(String typeCode) throws SQLException {
        String sql = "SELECT * FROM RoomType WHERE typeCode = ?";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, typeCode);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new RoomType(
                            rs.getInt("roomTypeId"),
                            rs.getString("typeName"),
                            rs.getString("typeCode"),
                            rs.getString("description"),
                            rs.getString("createTime"),
                            rs.getString("updateTime"));
                }
            }
        }
        return null;
    }

    /**
     * 添加新的会议室类型
     */
    public boolean addRoomType(RoomType roomType) throws SQLException {
        String sql = "INSERT INTO RoomType (typeName, typeCode, description) VALUES (?, ?, ?)";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, roomType.getTypeName());
            pstmt.setString(2, roomType.getTypeCode());
            pstmt.setString(3, roomType.getDescription());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * 更新会议室类型
     */
    public boolean updateRoomType(RoomType roomType) throws SQLException {
        String sql = "UPDATE RoomType SET typeName = ?, typeCode = ?, description = ?, " +
                "updateTime = CURRENT_TIMESTAMP WHERE roomTypeId = ?";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, roomType.getTypeName());
            pstmt.setString(2, roomType.getTypeCode());
            pstmt.setString(3, roomType.getDescription());
            pstmt.setInt(4, roomType.getRoomTypeId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * 删除会议室类型
     */
    public boolean deleteRoomType(int roomTypeId) throws SQLException {
        // 检查是否有会议室使用此类型
        String checkSql = "SELECT COUNT(*) FROM MeetingRoom WHERE roomTypeId = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(checkSql)) {

            pstmt.setInt(1, roomTypeId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new SQLException("无法删除会议室类型：仍有会议室使用此类型");
                }
            }
        }

        String sql = "DELETE FROM RoomType WHERE roomTypeId = ?";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, roomTypeId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * 初始化默认会议室类型
     */
    public void initializeDefaultRoomTypes() throws SQLException {
        // 检查是否已有会议室类型数据
        String checkSql = "SELECT COUNT(*) FROM RoomType";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(checkSql);
                ResultSet rs = pstmt.executeQuery()) {

            if (rs.next() && rs.getInt(1) > 0) {
                return; // 已有数据，不需要初始化
            }
        }

        // 添加默认会议室类型
        List<RoomType> defaultRoomTypes = new ArrayList<>();

        defaultRoomTypes.add(new RoomType("基础会议室", "BASIC", "标准配置的会议室，适合日常会议"));
        defaultRoomTypes.add(new RoomType("高级会议室", "PREMIUM", "配备高级设备的会议室，适合重要会议"));
        defaultRoomTypes.add(new RoomType("VIP会议室", "VIP", "豪华配置的会议室，适合高层会议"));

        // 批量插入默认会议室类型
        for (RoomType roomType : defaultRoomTypes) {
            addRoomType(roomType);
        }
    }
}