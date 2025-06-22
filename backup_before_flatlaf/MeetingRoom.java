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
    private int roomTypeId; // 会议室类型ID
    private String roomTypeCode; // 会议室类型代码（用于权限检查）

    public MeetingRoom(int roomId, String name, int capacity, String location, String description, int status) {
        this.roomId = roomId;
        this.name = name;
        this.capacity = capacity;
        this.location = location;
        this.description = description;
        this.status = status;
    }

    // 新增构造函数，包含会议室类型
    public MeetingRoom(int roomId, String name, int capacity, String location, String description, int status,
            int roomTypeId) {
        this.roomId = roomId;
        this.name = name;
        this.capacity = capacity;
        this.location = location;
        this.description = description;
        this.status = status;
        this.roomTypeId = roomTypeId;
    }

    // 新增构造函数，包含会议室类型代码
    public MeetingRoom(int roomId, String name, int capacity, String location, String description, int status,
            int roomTypeId, String roomTypeCode) {
        this.roomId = roomId;
        this.name = name;
        this.capacity = capacity;
        this.location = location;
        this.description = description;
        this.status = status;
        this.roomTypeId = roomTypeId;
        this.roomTypeCode = roomTypeCode;
    }

    // Overloaded constructor for creating a new room without an ID yet
    public MeetingRoom(String name, int capacity, String location, String description, int status) {
        this.name = name;
        this.capacity = capacity;
        this.location = location;
        this.description = description;
        this.status = status;
    }

    // 新增构造函数，用于创建新会议室
    public MeetingRoom(String name, int capacity, String location, String description, int status, int roomTypeId) {
        this.name = name;
        this.capacity = capacity;
        this.location = location;
        this.description = description;
        this.status = status;
        this.roomTypeId = roomTypeId;
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

    public int getRoomTypeId() {
        return roomTypeId;
    }

    public String getRoomTypeCode() {
        return roomTypeCode;
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

    // Setters
    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setRoomTypeId(int roomTypeId) {
        this.roomTypeId = roomTypeId;
    }

    public void setRoomTypeCode(String roomTypeCode) {
        this.roomTypeCode = roomTypeCode;
    }
}