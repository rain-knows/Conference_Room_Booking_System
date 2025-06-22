import java.sql.Timestamp;

public class Reservation {
    // Status Constants
    public static final int STATUS_CONFIRMED = 1;
    public static final int STATUS_CANCELLED = 2;
    public static final int STATUS_IN_PROGRESS = 3;
    public static final int STATUS_COMPLETED = 4;

    private int reservationId;
    private int userId;
    private int roomId;
    private String roomName; // 添加会议室名称字段，方便显示
    private String subject;
    private String description; // 会议描述
    private Timestamp startTime;
    private Timestamp endTime;
    private int status; // 状态：1=已确认, 2=已取消, 3=进行中, 4=已完成

    public Reservation(int reservationId, int userId, int roomId, String roomName, String subject, String description,
            Timestamp startTime, Timestamp endTime, int status) {
        this.reservationId = reservationId;
        this.userId = userId;
        this.roomId = roomId;
        this.roomName = roomName;
        this.subject = subject;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    // Getters
    public int getReservationId() {
        return reservationId;
    }

    public int getUserId() {
        return userId;
    }

    public int getRoomId() {
        return roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public String getSubject() {
        return subject;
    }

    public String getDescription() {
        return description;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public int getStatus() {
        return status;
    }

    /**
     * 根据状态码返回可读的文本。
     * 
     * @return 状态的中文描述
     */
    public String getStatusText() {
        switch (this.status) {
            case STATUS_CONFIRMED:
                return "已确认";
            case STATUS_CANCELLED:
                return "已取消";
            case STATUS_IN_PROGRESS:
                return "进行中";
            case STATUS_COMPLETED:
                return "已完成";
            default:
                return "未知状态";
        }
    }
}