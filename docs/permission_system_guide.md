# 会议室预订系统权限管理指南

## 概述

本系统实现了基于字段映射的权限管理系统，通过用户角色（`user.role`）和会议室类型代码（`roomtype.typecode`）来控制用户对不同类型会议室的访问权限。

## 权限模型

### 用户角色
- **NORMAL_EMPLOYEE**: 普通员工
- **LEADER**: 领导
- **SYSTEM_ADMIN**: 系统管理员

### 会议室类型
- **BASIC**: 基础会议室
- **PREMIUM**: 高级会议室  
- **VIP**: VIP会议室

### 权限类型
- **可查看 (canView)**: 用户可以查看会议室信息
- **可预订 (canBook)**: 用户可以预订会议室
- **可管理 (canManage)**: 用户可以管理会议室（增删改）

## 默认权限配置

| 用户角色 | 会议室类型 | 可查看 | 可预订 | 可管理 | 说明 |
|---------|-----------|--------|--------|--------|------|
| SYSTEM_ADMIN | BASIC | ✓ | ✓ | ✓ | 系统管理员可完全管理基础会议室 |
| SYSTEM_ADMIN | PREMIUM | ✓ | ✓ | ✓ | 系统管理员可完全管理高级会议室 |
| SYSTEM_ADMIN | VIP | ✓ | ✓ | ✓ | 系统管理员可完全管理VIP会议室 |
| LEADER | BASIC | ✓ | ✓ | ✗ | 领导可预订和查看基础会议室 |
| LEADER | PREMIUM | ✓ | ✓ | ✗ | 领导可预订和查看高级会议室 |
| LEADER | VIP | ✓ | ✓ | ✗ | 领导可预订和查看VIP会议室 |
| NORMAL_EMPLOYEE | BASIC | ✓ | ✓ | ✗ | 普通员工可预订和查看基础会议室 |
| NORMAL_EMPLOYEE | PREMIUM | ✓ | ✗ | ✗ | 普通员工只能查看高级会议室 |
| NORMAL_EMPLOYEE | VIP | ✗ | ✗ | ✗ | 普通员工无VIP会议室权限 |

## 系统设置页面功能

### 1. 密码管理
- 管理员可以修改自己的登录密码

### 2. 权限管理
- **查看权限映射**: 显示所有用户角色和会议室类型的权限配置
- **添加权限映射**: 为新的用户角色或会议室类型创建权限配置
- **编辑权限映射**: 修改现有权限配置
- **删除权限映射**: 删除不需要的权限配置
- **初始化默认权限**: 恢复系统默认的权限配置

### 3. 会议室类型管理
- **查看会议室类型**: 显示所有会议室类型
- **添加会议室类型**: 创建新的会议室类型
- **编辑会议室类型**: 修改现有会议室类型信息
- **删除会议室类型**: 删除不需要的会议室类型

## 权限检查逻辑

### 会议室状态页面
1. **加载会议室列表**: 只显示用户有权限查看的会议室
2. **预订按钮状态**: 只有用户有权限预订且会议室状态为空闲时才启用
3. **预订操作**: 在预订前再次检查用户权限

### 权限检查方法
```java
// 检查用户是否有权限预订指定会议室
boolean canBook = meetingRoomDAO.canUserBookRoom(userRole, roomId);

// 检查用户是否有权限管理指定会议室
boolean canManage = meetingRoomDAO.canUserManageRoom(userRole, roomId);

// 获取用户可访问的会议室列表
List<MeetingRoom> accessibleRooms = meetingRoomDAO.getAccessibleMeetingRooms(userRole);

// 获取用户可预订的会议室列表
List<MeetingRoom> bookableRooms = meetingRoomDAO.getBookableMeetingRooms(userRole);
```

## 数据库表结构

### PermissionMapping 表
```sql
CREATE TABLE PermissionMapping (
    mappingId INT PRIMARY KEY AUTO_INCREMENT,
    userRole VARCHAR(50) NOT NULL,           -- 用户角色
    roomTypeCode VARCHAR(20) NOT NULL,       -- 会议室类型代码
    canBook BOOLEAN NOT NULL DEFAULT FALSE,  -- 是否可以预订
    canView BOOLEAN NOT NULL DEFAULT FALSE,  -- 是否可以查看
    canManage BOOLEAN NOT NULL DEFAULT FALSE, -- 是否可以管理
    description VARCHAR(255),                -- 权限描述
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP,
    updateTime DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_user_role_room_type` (userRole, roomTypeCode)
);
```

### RoomType 表
```sql
CREATE TABLE RoomType (
    roomTypeId INT PRIMARY KEY AUTO_INCREMENT,
    typeName VARCHAR(50) NOT NULL,     -- 类型名称
    typeCode VARCHAR(20) NOT NULL,     -- 类型代码
    description VARCHAR(255),          -- 类型描述
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP,
    updateTime DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_room_type_code` (typeCode)
);
```

## 初始化步骤

1. **执行数据库初始化脚本**:
   ```sql
   -- 运行 docs/initialize_permission_system.sql
   ```

2. **启动应用程序**:
   - 系统会自动检查并初始化默认权限配置

3. **配置权限**:
   - 以系统管理员身份登录
   - 进入"系统设置"页面
   - 在"权限管理"选项卡中配置权限映射
   - 在"会议室类型"选项卡中管理会议室类型

## 扩展指南

### 添加新的用户角色
1. 在数据库的 `User` 表中添加新角色
2. 在系统设置中添加对应的权限映射
3. 更新 `User.java` 中的 `isAdmin()` 方法（如需要）

### 添加新的会议室类型
1. 在系统设置的"会议室类型"选项卡中添加新类型
2. 为各用户角色配置新类型的权限
3. 更新现有会议室，分配新的类型

### 自定义权限检查
可以在需要的地方添加权限检查：
```java
// 示例：检查用户是否有权限执行某个操作
if (!meetingRoomDAO.canUserManageRoom(currentUser.getRole(), roomId)) {
    throw new SecurityException("您没有权限执行此操作");
}
```

## 注意事项

1. **权限缓存**: 当前实现每次都会查询数据库检查权限，如需优化可添加缓存机制
2. **权限继承**: 当前没有实现权限继承，每个角色需要单独配置
3. **动态权限**: 权限配置在运行时可以修改，立即生效
4. **数据一致性**: 删除会议室类型前会检查是否有会议室使用该类型
5. **默认权限**: 系统提供默认权限配置，可以通过"初始化默认权限"按钮恢复 