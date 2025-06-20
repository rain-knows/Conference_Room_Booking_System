import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * MeetingRoomDAO 类用于处理与 MeetingRoom 表相关的数据库操作。
 */
public class MeetingRoomDAO {

    /**
     * 获取会议室总数。
     * 
     * @return 会议室总数
     * @throws SQLException 数据库访问异常
     */
    public int getTotalRoomCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM MeetingRoom";
        try (Connection conn = UserDAO.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * 获取当前可用（状态为空闲）的会议室数量。
     * 
     * @return 可用会议室数量
     * @throws SQLException 数据库访问异常
     */
    public int getAvailableRoomCount() throws SQLException {
        // "可用"指的是会议室自身状态为Available，并且当前没有正在进行的预订
        String sql = "SELECT COUNT(*) FROM MeetingRoom m " +
                "WHERE m.status = ? AND NOT EXISTS (" +
                "  SELECT 1 FROM Reservation r " +
                "  WHERE r.roomId = m.roomId AND NOW() BETWEEN r.startTime AND r.endTime AND r.status =?" +
                ")";
        try (Connection conn = UserDAO.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, MeetingRoom.STATUS_AVAILABLE);
            pstmt.setInt(2, Reservation.STATUS_CONFIRMED); // 正在进行的预订必须是已确认的
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * 根据ID获取单个会议室的完整信息。
     * 
     * @param roomId 要查找的会议室ID
     * @return 如果找到，返回 MeetingRoom 对象；否则返回 null
     * @throws SQLException 数据库访问异常
     */
    public MeetingRoom getMeetingRoomById(int roomId) throws SQLException {
        String sql = "SELECT roomId, name, capacity, location, description, status FROM MeetingRoom WHERE roomId = ?";
        try (Connection conn = UserDAO.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, roomId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new MeetingRoom(
                            rs.getInt("roomId"),
                            rs.getString("name"),
                            rs.getInt("capacity"),
                            rs.getString("location"),
                            rs.getString("description"),
                            rs.getInt("status"));
                }
            }
        }
        return null;
    }

    /**
     * 获取所有会议室及其当前状态和预订信息。
     * 
     * @return 包含 MeetingRoomStatusDTO 对象的列表，每个对象代表一个会议室及其状态。
     * @throws SQLException 如果发生数据库访问错误。
     */
    public List<MeetingRoomStatusDTO> getAllMeetingRoomsWithStatus() throws SQLException {
        List<MeetingRoomStatusDTO> roomStatuses = new ArrayList<>();
        String query = "SELECT m.roomId, m.name AS roomName, m.status AS roomStatus, " +
                "r.subject AS currentBookingSubject, r.startTime AS currentBookingStartTime, r.endTime AS currentBookingEndTime "
                +
                "FROM MeetingRoom m " +
                "LEFT JOIN Reservation r ON m.roomId = r.roomId AND NOW() BETWEEN r.startTime AND r.endTime AND r.status = 1 "
                + // 假设 status 1 代表有效预订
                "ORDER BY m.name;";

        try (Connection conn = UserDAO.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int roomId = rs.getInt("roomId");
                String roomName = rs.getString("roomName");
                int roomDbStatus = rs.getInt("roomStatus"); // 从数据库读取的会议室自身状态 (e.g., 0:可用, 1:维护中)
                String currentBookingSubject = rs.getString("currentBookingSubject");
                Timestamp currentBookingStartTime = rs.getTimestamp("currentBookingStartTime");
                Timestamp currentBookingEndTime = rs.getTimestamp("currentBookingEndTime");

                String displayStatus;
                String bookingTime = "";

                // 优先判断是否被预订（使用中）
                if (currentBookingSubject != null) {
                    displayStatus = "使用中";
                    if (currentBookingStartTime != null && currentBookingEndTime != null) {
                        bookingTime = String.format("%s - %s",
                                new java.text.SimpleDateFormat("HH:mm").format(currentBookingStartTime),
                                new java.text.SimpleDateFormat("HH:mm").format(currentBookingEndTime));
                    }
                } else {
                    // 如果没有被预订，则根据会议室自身状态判断
                    switch (roomDbStatus) {
                        case MeetingRoom.STATUS_AVAILABLE:
                            displayStatus = "空闲";
                            break;
                        case MeetingRoom.STATUS_MAINTENANCE:
                            displayStatus = "维护中";
                            break;
                        case MeetingRoom.STATUS_DECOMMISSIONED:
                            displayStatus = "已停用";
                            break;
                        default:
                            displayStatus = "未知状态";
                            break;
                    }
                }

                roomStatuses.add(
                        new MeetingRoomStatusDTO(roomId, roomName, displayStatus, currentBookingSubject, bookingTime));
            }
        }
        return roomStatuses;
    }

    /**
     * DTO (Data Transfer Object) 用于封装会议室及其当前状态和预订信息。
     */
    public static class MeetingRoomStatusDTO {
        private int roomId;
        private String roomName;
        private String status; // 显示状态: 空闲, 使用中, 维护中
        private String currentBookingSubject;
        private String currentBookingTime;

        public MeetingRoomStatusDTO(int roomId, String roomName, String status, String currentBookingSubject,
                String currentBookingTime) {
            this.roomId = roomId;
            this.roomName = roomName;
            this.status = status;
            this.currentBookingSubject = currentBookingSubject;
            this.currentBookingTime = currentBookingTime;
        }

        public int getRoomId() {
            return roomId;
        }

        public String getRoomName() {
            return roomName;
        }

        public String getStatus() {
            return status;
        }

        public String getCurrentBookingSubject() {
            return currentBookingSubject;
        }

        public String getCurrentBookingTime() {
            return currentBookingTime;
        }
    }

    // --- CRUD Operations for Admin ---

    /**
     * 获取所有会议室的完整信息列表。
     * 
     * @return 会议室对象列表
     * @throws SQLException 数据库访问异常
     */
    public List<MeetingRoom> getAllMeetingRooms() throws SQLException {
        List<MeetingRoom> rooms = new ArrayList<>();
        String sql = "SELECT roomId, name, capacity, location, description, status FROM MeetingRoom ORDER BY name";
        try (Connection conn = UserDAO.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                rooms.add(new MeetingRoom(
                        rs.getInt("roomId"),
                        rs.getString("name"),
                        rs.getInt("capacity"),
                        rs.getString("location"),
                        rs.getString("description"),
                        rs.getInt("status")));
            }
        }
        return rooms;
    }

    /**
     * 添加一个新的会议室。
     * 
     * @param room 要添加的会议室对象（不含ID）
     * @return 如果添加成功，返回 true
     * @throws SQLException 数据库访问异常
     */
    public boolean addMeetingRoom(MeetingRoom room) throws SQLException {
        String sql = "INSERT INTO MeetingRoom (name, capacity, location, description, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = UserDAO.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, room.getName());
            pstmt.setInt(2, room.getCapacity());
            pstmt.setString(3, room.getLocation());
            pstmt.setString(4, room.getDescription());
            pstmt.setInt(5, room.getStatus());
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * 更新一个已有的会议室信息。
     * 
     * @param room 包含更新信息的会议室对象
     * @return 如果更新成功，返回 true
     * @throws SQLException 数据库访问异常
     */
    public boolean updateMeetingRoom(MeetingRoom room) throws SQLException {
        String sql = "UPDATE MeetingRoom SET name = ?, capacity = ?, location = ?, description = ?, status = ? WHERE roomId = ?";
        try (Connection conn = UserDAO.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, room.getName());
            pstmt.setInt(2, room.getCapacity());
            pstmt.setString(3, room.getLocation());
            pstmt.setString(4, room.getDescription());
            pstmt.setInt(5, room.getStatus());
            pstmt.setInt(6, room.getRoomId());
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * 删除一个会议室。
     * 
     * @param roomId 要删除的会议室ID
     * @return 如果删除成功，返回 true
     * @throws SQLException 数据库访问异常
     */
    public boolean deleteMeetingRoom(int roomId) throws SQLException {
        // 注意：在实际应用中，删除前可能需要检查该会议室是否有未来的预订
        String sql = "DELETE FROM MeetingRoom WHERE roomId = ?";
        try (Connection conn = UserDAO.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, roomId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }
}