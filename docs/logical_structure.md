# 会议室预约管理系统逻辑结构文档

## 1. 概述

本文档定义了会议室预约管理系统的逻辑数据模型。它描述了系统中的核心实体、它们的属性以及它们之间的关系。此逻辑结构基于 `database_design.md` 中定义的物理数据库结构。

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
    *   userTypeId (用户类型ID)
    *   active (账户是否激活)

*   **UserType (用户类型)**
    *   userTypeId (主键)
    *   typeName (类型名称, 如: 普通员工, 领导)
    *   typeCode (类型代码)
    *   description (描述)

*   **Role (角色)**
    *   roleId (主键)
    *   name (角色名称, 如: 普通用户, 管理员)
    *   description (角色描述)

*   **Permission (权限)**
    *   permissionId (主键)
    *   name (权限名称)
    *   description (权限描述)
    *   resourceType (资源类型)

### 2.2 会议室管理

*   **MeetingRoom (会议室)**
    *   roomId (主键)
    *   name (会议室名称)
    *   location (位置)
    *   capacity (容量)
    *   status (状态)
    *   description (描述)

*   **RoomType (会议室类型)**
    *   roomTypeId (主键)
    *   typeName (类型名称, 如: 普通, 高级)
    *   typeCode (类型代码)
    *   description (描述)

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

## 3. 实体关系

实体之间的主要关系如下：

*   **用户与角色 (多对多)**
    *   一个 `User` 可以拥有多个 `Role`。
    *   一个 `Role` 可以分配给多个 `User`。
    *   通过 `UserRole` 关联表实现。

*   **角色与权限 (多对多)**
    *   一个 `Role` 可以包含多个 `Permission`。
    *   一个 `Permission` 可以属于多个 `Role`。
    *   通过 `RolePermission` 关联表实现。

*   **用户与用户类型 (多对一)**
    *   一个 `User` 属于一个 `UserType`。

*   **会议室与设备 (一对多)**
    *   一个 `MeetingRoom` 可以有多台 `Equipment`。

*   **用户与预约 (一对多)**
    *   一个 `User` 可以创建多个 `Reservation`。

*   **会议室与预约 (一对多)**
    *   一个 `MeetingRoom` 可以有多个 `Reservation`。

*   **预约与参与者 (多对多)**
    *   一个 `Reservation` 可以有多个 `User` 作为参与者。
    *   一个 `User` 可以参与多个 `Reservation`。
    *   通过 `ReservationParticipant` 关联表实现。

*   **用户类型与会议室类型 (多对多)**
    *   定义了特定 `UserType` 可以访问哪些 `RoomType` 的权限。
    *   通过 `UserTypeRoomTypePermission` 关联表实现。
