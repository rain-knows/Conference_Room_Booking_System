import java.sql.Date;

public class Equipment {
    // Status Constants matching shuju.sql: 1 = 正常, 2 = 维修中, 3 = 报废
    public static final int STATUS_NORMAL = 1;
    public static final int STATUS_MAINTENANCE = 2;
    public static final int STATUS_SCRAPPED = 3;

    private int equipmentId;
    private int roomId;
    private String name;
    private String model;
    private int status;
    private Date purchaseDate;
    private String roomName; // Optional, for display purposes

    // Full constructor
    public Equipment(int equipmentId, int roomId, String name, String model, int status, Date purchaseDate) {
        this.equipmentId = equipmentId;
        this.roomId = roomId;
        this.name = name;
        this.model = model;
        this.status = status;
        this.purchaseDate = purchaseDate;
    }

    // Constructor for creating new equipment
    public Equipment(int roomId, String name, String model, int status, Date purchaseDate) {
        this.roomId = roomId;
        this.name = name;
        this.model = model;
        this.status = status;
        this.purchaseDate = purchaseDate;
    }

    // Getters
    public int getEquipmentId() {
        return equipmentId;
    }

    public int getRoomId() {
        return roomId;
    }

    public String getName() {
        return name;
    }

    public String getModel() {
        return model;
    }

    public int getStatus() {
        return status;
    }

    public Date getPurchaseDate() {
        return purchaseDate;
    }

    public String getRoomName() {
        return roomName;
    }

    // Setters
    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    /**
     * Gets the status as a readable text.
     * 
     * @return The status text.
     */
    public String getStatusText() {
        switch (status) {
            case STATUS_NORMAL:
                return "正常";
            case STATUS_MAINTENANCE:
                return "维修中";
            case STATUS_SCRAPPED:
                return "报废";
            default:
                return "未知";
        }
    }
}