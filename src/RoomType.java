/**
 * 会议室类型实体类
 */
public class RoomType {
    private int roomTypeId;
    private String typeName;        // 类型名称
    private String typeCode;        // 类型代码
    private String description;     // 类型描述
    private String createTime;      // 创建时间
    private String updateTime;      // 更新时间

    // 构造函数
    public RoomType(int roomTypeId, String typeName, String typeCode, String description, 
                   String createTime, String updateTime) {
        this.roomTypeId = roomTypeId;
        this.typeName = typeName;
        this.typeCode = typeCode;
        this.description = description;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    // 用于创建新会议室类型的构造函数
    public RoomType(String typeName, String typeCode, String description) {
        this.typeName = typeName;
        this.typeCode = typeCode;
        this.description = description;
    }

    // Getters
    public int getRoomTypeId() {
        return roomTypeId;
    }

    public String getTypeName() {
        return typeName;
    }

    public String getTypeCode() {
        return typeCode;
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
    public void setRoomTypeId(int roomTypeId) {
        this.roomTypeId = roomTypeId;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
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
        return String.format("RoomType{roomTypeId=%d, typeName='%s', typeCode='%s', description='%s'}", 
                           roomTypeId, typeName, typeCode, description);
    }
} 