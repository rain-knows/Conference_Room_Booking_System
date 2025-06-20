public class MeetingRoom {
    // Status Constants
    public static final int STATUS_AVAILABLE = 1;
    public static final int STATUS_MAINTENANCE = 2;
    public static final int STATUS_DECOMMISSIONED = 3;

    private int roomId;
    private String name;
    private int capacity;
    private String location;
    private String description;
    private int status; // e.g., 0: Available, 1: Under Maintenance

    public MeetingRoom(int roomId, String name, int capacity, String location, String description, int status) {
        this.roomId = roomId;
        this.name = name;
        this.capacity = capacity;
        this.location = location;
        this.description = description;
        this.status = status;
    }

    // Overloaded constructor for creating a new room without an ID yet
    public MeetingRoom(String name, int capacity, String location, String description, int status) {
        this.name = name;
        this.capacity = capacity;
        this.location = location;
        this.description = description;
        this.status = status;
    }

    // Getters
    public int getRoomId() {
        return roomId;
    }

    public String getName() {
        return name;
    }

    public int getCapacity() {
        return capacity;
    }

    public String getLocation() {
        return location;
    }

    public String getDescription() {
        return description;
    }

    public int getStatus() {
        return status;
    }

    public String getStatusText() {
        switch (status) {
            case STATUS_AVAILABLE:
                return "可用";
            case STATUS_MAINTENANCE:
                return "维护中";
            case STATUS_DECOMMISSIONED:
                return "已停用";
            default:
                return "未知";
        }
    }
}