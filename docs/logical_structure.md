# 会议室预约管理系统逻辑结构文档

## 1. 概述

本文档定义了会议室预约管理系统的逻辑数据模型。它描述了系统中的核心实体、它们的属性以及它们之间的关系。此逻辑结构基于 `database_design.md` 中定义的物理数据库结构。系统采用基于字段映射的权限管理机制，通过用户角色和会议室类型的组合来控制访问权限。

## 2. 实体与属性

系统包含以下核心实体：

### 2.1 用户管理

*   **User (用户)**
    *   userId (主键)
    *   username (用户名)
    *   password (密码)
    *   fullName (全名)
    *   email (电子邮件)
    *   phone (联系电话)
    *   role (用户角色: NORMAL_EMPLOYEE, LEADER, SYSTEM_ADMIN)
    *   active (账户是否激活)

### 2.2 会议室管理

*   **MeetingRoom (会议室)**
    *   roomId (主键)
    *   name (会议室名称)
    *   location (位置)
    *   capacity (容量)
    *   status (状态)
    *   description (描述)
    *   roomTypeId (会议室类型ID)

*   **RoomType (会议室类型)**
    *   roomTypeId (主键)
    *   typeName (类型名称, 如: 基础会议室, 高级会议室, VIP会议室)
    *   typeCode (类型代码, 如: BASIC, PREMIUM, VIP)
    *   description (描述)
    *   createTime (创建时间)
    *   updateTime (更新时间)

*   **Equipment (设备)**
    *   equipmentId (主键)
    *   roomId (所属会议室ID)
    *   name (设备名称)
    *   model (型号)
    *   status (状态)
    *   purchaseDate (购买日期)

### 2.3 预约管理

*   **Reservation (预约)**
    *   reservationId (主键)
    *   userId (预约用户ID)
    *   roomId (会议室ID)
    *   startTime (开始时间)
    *   endTime (结束时间)
    *   subject (主题)
    *   description (描述)
    *   status (状态)
    *   createdTime (创建时间)

### 2.4 权限管理

*   **PermissionMapping (权限映射)**
    *   mappingId (主键)
    *   userRole (用户角色: NORMAL_EMPLOYEE, LEADER, SYSTEM_ADMIN)
    *   roomTypeCode (会议室类型代码)
    *   canBook (是否可以预订)
    *   canView (是否可以查看)
    *   canManage (是否可以管理)
    *   description (权限描述)
    *   createTime (创建时间)
    *   updateTime (更新时间)

## 3. 实体关系

实体之间的主要关系如下：

*   **用户与预约 (一对多)**
    *   一个 `User` 可以创建多个 `Reservation`。

*   **会议室与预约 (一对多)**
    *   一个 `MeetingRoom` 可以有多个 `Reservation`。

*   **会议室与设备 (一对多)**
    *   一个 `MeetingRoom` 可以有多台 `Equipment`。

*   **会议室与会议室类型 (多对一)**
    *   一个 `MeetingRoom` 属于一个 `RoomType`。

*   **用户角色与会议室类型权限映射 (多对多)**
    *   通过 `PermissionMapping` 表定义用户角色对会议室类型的权限。
    *   支持查看(canView)、预订(canBook)、管理(canManage)三种权限类型。
    *   每个角色对每种会议室类型只有一条权限记录。

## 4. 权限模型设计

### 4.1 权限维度

系统采用三维权限模型：

1. **用户角色维度**: NORMAL_EMPLOYEE, LEADER, SYSTEM_ADMIN
2. **会议室类型维度**: BASIC, PREMIUM, VIP
3. **权限类型维度**: 查看(canView), 预订(canBook), 管理(canManage)

### 4.2 默认权限配置

| 用户角色 | 基础会议室 | 高级会议室 | VIP会议室 |
|---------|-----------|-----------|-----------|
| SYSTEM_ADMIN | 完全管理 | 完全管理 | 完全管理 |
| LEADER | 可预订查看 | 可预订查看 | 可预订查看 |
| NORMAL_EMPLOYEE | 可预订查看 | 仅查看 | 无权限 |

### 4.3 权限检查逻辑

1. **会议室列表加载**: 只显示用户有权限查看的会议室
2. **预订按钮状态**: 根据用户权限和会议室状态动态启用/禁用
3. **预订操作验证**: 在预订前再次检查用户权限
4. **管理权限检查**: 在管理操作前验证用户权限

## 5. 业务逻辑流程

### 5.1 用户登录流程

1. 用户输入用户名和密码
2. 系统验证用户凭据
3. 根据用户角色加载相应的权限配置
4. 进入主界面，显示用户可访问的功能模块

### 5.2 会议室预订流程

1. 用户查看会议室列表（根据权限过滤）
2. 选择可预订的会议室
3. 填写预订信息
4. 系统检查时间冲突和用户权限
5. 创建预订记录

### 5.3 权限管理流程

1. 系统管理员进入权限管理界面
2. 查看当前权限映射配置
3. 添加、修改或删除权限映射
4. 保存权限配置
5. 权限变更立即生效

## 6. 数据完整性约束

### 6.1 外键约束

- `MeetingRoom.roomTypeId` → `RoomType.roomTypeId`
- `MeetingRoom.roomId` → `Equipment.roomId`
- `User.userId` → `Reservation.userId`
- `MeetingRoom.roomId` → `Reservation.roomId`

### 6.2 唯一性约束

- `User.username` - 用户名唯一
- `User.email` - 邮箱唯一
- `RoomType.typeCode` - 会议室类型代码唯一
- `PermissionMapping(userRole, roomTypeCode)` - 角色类型组合唯一

### 6.3 业务规则约束

- 用户只能预订有权限的会议室类型
- 预订时间不能冲突
- 会议室状态必须为可用才能预订
- 权限配置必须完整（每个角色对每种类型都有权限记录）

## 7. 扩展性考虑

### 7.1 权限系统扩展

- 支持添加新的用户角色
- 支持添加新的会议室类型
- 支持添加新的权限类型
- 支持更复杂的权限规则

### 7.2 业务功能扩展

- 支持会议室审批流程
- 支持会议室使用统计
- 支持邮件通知功能
- 支持移动端访问

## 8. 总结

本逻辑结构文档定义了一个基于字段映射的权限管理系统的核心实体和关系。系统通过用户角色和会议室类型的组合来实现细粒度的权限控制，支持查看、预订、管理三种权限类型。这种设计既保证了系统的安全性，又提供了良好的扩展性和维护性。
