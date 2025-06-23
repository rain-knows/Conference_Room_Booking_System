import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ReservationDAO 类用于处理与 Reservation 表相关的数据库操作。
 * 包括获取预订信息等。
 */
public class ReservationDAO {
    /**
     * 获取指定日期的预订总数（不包括已取消的）。
     * 
     * @param date 日期
     * @return 当日的预订数量
     * @throws SQLException 数据库访问异常
     */
    public int getBookingCountForDate(java.util.Date date) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Reservation WHERE DATE(startTime) = ? AND status != ?";
        try (Connection conn = UserDAO.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, new java.sql.Date(date.getTime()));
            pstmt.setInt(2, Reservation.STATUS_CANCELLED);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * 检查在指定时间段内，某个会议室是否已被预订。
     * 
     * @param roomId               会议室ID
     * @param startTime            开始时间
     * @param endTime              结束时间
     * @param excludeReservationId 要排除的预订ID（用于修改自身预订时）
     * @return 如果有冲突，返回 true
     * @throws SQLException 数据库访问异常
     */
    public boolean hasConflict(int roomId, java.sql.Timestamp startTime, java.sql.Timestamp endTime,
            int excludeReservationId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Reservation " +
                "WHERE roomId = ? AND status != ? AND reservationId != ? AND " +
                "(? < endTime AND ? > startTime)";
        try (Connection conn = UserDAO.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, roomId);
            pstmt.setInt(2, Reservation.STATUS_CANCELLED);
            pstmt.setInt(3, excludeReservationId);
            pstmt.setTimestamp(4, startTime);
            pstmt.setTimestamp(5, endTime);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * 创建一个新的预订。
     * 
     * @param reservation 预订对象
     * @return 如果创建成功，返回 true
     * @throws SQLException 数据库访问异常
     */
    public boolean createReservation(Reservation reservation) throws SQLException {
        if (hasConflict(reservation.getRoomId(), reservation.getStartTime(), reservation.getEndTime(), 0)) {
            throw new SQLException("预订时间冲突！该时间段已被占用。");
        }
        String sql = "INSERT INTO Reservation (userId, roomId, subject, description, startTime, endTime, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = UserDAO.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, reservation.getUserId());
            pstmt.setInt(2, reservation.getRoomId());
            pstmt.setString(3, reservation.getSubject());
            pstmt.setString(4, reservation.getDescription());
            pstmt.setTimestamp(5, reservation.getStartTime());
            pstmt.setTimestamp(6, reservation.getEndTime());
            pstmt.setInt(7, reservation.getStatus());
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * 更新一个已有的预订。
     * 
     * @param reservation 包含更新信息的预订对象
     * @return 如果更新成功，返回 true
     * @throws SQLException 数据库访问异常
     */
    public boolean updateReservation(Reservation reservation) throws SQLException {
        if (hasConflict(reservation.getRoomId(), reservation.getStartTime(), reservation.getEndTime(),
                reservation.getReservationId())) {
            throw new SQLException("预订时间冲突！该时间段已被占用。");
        }
        String sql = "UPDATE Reservation SET subject = ?, description = ?, startTime = ?, endTime = ?, status = ? WHERE reservationId = ?";
        try (Connection conn = UserDAO.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, reservation.getSubject());
            pstmt.setString(2, reservation.getDescription());
            pstmt.setTimestamp(3, reservation.getStartTime());
            pstmt.setTimestamp(4, reservation.getEndTime());
            pstmt.setInt(5, reservation.getStatus());
            pstmt.setInt(6, reservation.getReservationId());
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * 根据用户ID获取该用户的所有预订记录
     * 
     * @param userId 用户ID
     * @return 预订记录列表
     * @throws SQLException 数据库访问异常
     */
    public List<Reservation> getReservationsByUserId(int userId) throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        // SQL 查询通过连接 Reservation 和 MeetingRoom 表来获取会议室名称
        String sql = "SELECT r.reservationId, r.userId, r.roomId, m.name AS roomName, r.subject, r.description, r.startTime, r.endTime, r.status "
                +
                "FROM Reservation r " +
                "JOIN MeetingRoom m ON r.roomId = m.roomId " +
                "WHERE r.userId = ? " +
                "ORDER BY r.startTime DESC";

        try (Connection conn = UserDAO.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int reservationId = rs.getInt("reservationId");
                    int roomId = rs.getInt("roomId");
                    String roomName = rs.getString("roomName");
                    String subject = rs.getString("subject");
                    String description = rs.getString("description");
                    Timestamp startTime = rs.getTimestamp("startTime");
                    Timestamp endTime = rs.getTimestamp("endTime");
                    int status = rs.getInt("status");

                    reservations.add(
                            new Reservation(reservationId, userId, roomId, roomName, subject, description, startTime,
                                    endTime, status));
                }
            }
        }
        return reservations;
    }

    /**
     * 更新预订状态（例如，取消预订）
     * 
     * @param reservationId 要更新的预订ID
     * @param newStatus     新的状态码 (e.g., 2 for "Cancelled")
     * @return 如果更新成功，返回 true
     * @throws SQLException 数据库访问异常
     */
    public boolean updateReservationStatus(int reservationId, int newStatus) throws SQLException {
        String sql = "UPDATE Reservation SET status = ? WHERE reservationId = ?";
        try (Connection conn = UserDAO.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, newStatus);
            pstmt.setInt(2, reservationId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

}