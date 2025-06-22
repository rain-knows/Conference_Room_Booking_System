/**
 * 权限映射实体类
 * 用于管理用户角色和会议室类型的权限关系
 */
public class PermissionMapping {
    private int mappingId;
    private String userRole; // 用户角色 (NORMAL_EMPLOYEE, LEADER, SYSTEM_ADMIN)
    private String roomTypeCode; // 会议室类型代码
    private boolean canBook; // 是否可以预订
    private boolean canView; // 是否可以查看
    private boolean canManage; // 是否可以管理
    private String description; // 权限描述
    private String createTime; // 创建时间
    private String updateTime; // 更新时间

    // 构造函数
    public PermissionMapping(int mappingId, String userRole, String roomTypeCode,
            boolean canBook, boolean canView, boolean canManage,
            String description, String createTime, String updateTime) {
        this.mappingId = mappingId;
        this.userRole = userRole;
        this.roomTypeCode = roomTypeCode;
        this.canBook = canBook;
        this.canView = canView;
        this.canManage = canManage;
        this.description = description;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    // 用于创建新权限映射的构造函数
    public PermissionMapping(String userRole, String roomTypeCode,
            boolean canBook, boolean canView, boolean canManage,
            String description) {
        this.userRole = userRole;
        this.roomTypeCode = roomTypeCode;
        this.canBook = canBook;
        this.canView = canView;
        this.canManage = canManage;
        this.description = description;
    }

    // Getters
    public int getMappingId() {
        return mappingId;
    }

    public String getUserRole() {
        return userRole;
    }

    public String getRoomTypeCode() {
        return roomTypeCode;
    }

    public boolean canBook() {
        return canBook;
    }

    public boolean canView() {
        return canView;
    }

    public boolean canManage() {
        return canManage;
    }

    public String getDescription() {
        return description;
    }

    public String getCreateTime() {
        return createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    // Setters
    public void setMappingId(int mappingId) {
        this.mappingId = mappingId;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public void setRoomTypeCode(String roomTypeCode) {
        this.roomTypeCode = roomTypeCode;
    }

    public void setCanBook(boolean canBook) {
        this.canBook = canBook;
    }

    public void setCanView(boolean canView) {
        this.canView = canView;
    }

    public void setCanManage(boolean canManage) {
        this.canManage = canManage;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return String.format("PermissionMapping{mappingId=%d, userRole='%s', roomTypeCode='%s', " +
                "canBook=%s, canView=%s, canManage=%s, description='%s'}",
                mappingId, userRole, roomTypeCode, canBook, canView, canManage, description);
    }
}