import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EquipmentDAO {

    /**
     * Get all equipment from the database, joined with room name for display.
     * 
     * @return A list of all equipment.
     * @throws SQLException on database error.
     */
    public List<Equipment> getAllEquipment() throws SQLException {
        List<Equipment> equipmentList = new ArrayList<>();
        String sql = "SELECT e.equipmentId, e.roomId, e.name, e.model, e.status, e.purchaseDate, m.name as roomName " +
                "FROM Equipment e LEFT JOIN MeetingRoom m ON e.roomId = m.roomId " +
                "ORDER BY e.equipmentId";
        try (Connection conn = UserDAO.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Equipment equipment = new Equipment(
                        rs.getInt("equipmentId"),
                        rs.getInt("roomId"),
                        rs.getString("name"),
                        rs.getString("model"),
                        rs.getInt("status"),
                        rs.getDate("purchaseDate"));
                equipment.setRoomName(rs.getString("roomName"));
                equipmentList.add(equipment);
            }
        }
        return equipmentList;
    }

    /**
     * Get all equipment for a specific room.
     * 
     * @param roomId The ID of the room.
     * @return A list of equipment in that room.
     * @throws SQLException on database error.
     */
    public List<Equipment> getEquipmentByRoomId(int roomId) throws SQLException {
        List<Equipment> equipmentList = new ArrayList<>();
        String sql = "SELECT equipmentId, roomId, name, model, status, purchaseDate FROM Equipment WHERE roomId = ?";
        try (Connection conn = UserDAO.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, roomId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    equipmentList.add(new Equipment(
                            rs.getInt("equipmentId"),
                            rs.getInt("roomId"),
                            rs.getString("name"),
                            rs.getString("model"),
                            rs.getInt("status"),
                            rs.getDate("purchaseDate")));
                }
            }
        }
        return equipmentList;
    }

    /**
     * Add a new piece of equipment to the database.
     * 
     * @param equipment The equipment object to add.
     * @return true if successful, false otherwise.
     * @throws SQLException on database error.
     */
    public boolean addEquipment(Equipment equipment) throws SQLException {
        String sql = "INSERT INTO Equipment (roomId, name, model, status, purchaseDate) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = UserDAO.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, equipment.getRoomId());
            pstmt.setString(2, equipment.getName());
            pstmt.setString(3, equipment.getModel());
            pstmt.setInt(4, equipment.getStatus());
            pstmt.setDate(5, equipment.getPurchaseDate());
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Update an existing piece of equipment.
     * 
     * @param equipment The equipment object with updated information.
     * @return true if successful, false otherwise.
     * @throws SQLException on database error.
     */
    public boolean updateEquipment(Equipment equipment) throws SQLException {
        String sql = "UPDATE Equipment SET roomId = ?, name = ?, model = ?, status = ?, purchaseDate = ? WHERE equipmentId = ?";
        try (Connection conn = UserDAO.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, equipment.getRoomId());
            pstmt.setString(2, equipment.getName());
            pstmt.setString(3, equipment.getModel());
            pstmt.setInt(4, equipment.getStatus());
            pstmt.setDate(5, equipment.getPurchaseDate());
            pstmt.setInt(6, equipment.getEquipmentId());
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Delete a piece of equipment from the database.
     * 
     * @param equipmentId The ID of the equipment to delete.
     * @return true if successful, false otherwise.
     * @throws SQLException on database error.
     */
    public boolean deleteEquipment(int equipmentId) throws SQLException {
        String sql = "DELETE FROM Equipment WHERE equipmentId = ?";
        try (Connection conn = UserDAO.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, equipmentId);
            return pstmt.executeUpdate() > 0;
        }
    }
}