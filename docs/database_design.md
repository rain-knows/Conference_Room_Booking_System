### 🤖 Assistant

# 会议室预约管理系统数据库设计文档

## 1. 系统概述

本文档描述了一个用于管理会议室预约、设备维护和用户权限的综合数据库系统。该系统允许用户创建和管理会议室预约、跟踪设备状态、发送通知、管理用户权限和维护系统日志。

## 2. 数据库架构

该数据库由16个表组成，可分为五个主要功能模块：

1. **用户管理模块**：User、Role、Permission、UserRole、RolePermission、UserType
2. **会议室管理模块**：MeetingRoom、Equipment、RoomType、UserTypeRoomTypePermission
3. **预约管理模块**：Reservation、ReservationParticipant
4. **设备维护模块**：EquipmentMaintenance
5. **通知与日志模块**：Notification、SystemLog、systemconfig

## 3. 表结构详情

### 3.1 用户管理模块

#### User 表
存储系统用户信息。
```
CREATE TABLE `User` (
    userId INT PRIMARY KEY,            -- 用户唯一标识符
    username VARCHAR(50) NOT NULL,     -- 用户名
    password VARCHAR(255) NOT NULL,    -- 密码（应加密存储）
    fullName VARCHAR(100),             -- 用户全名
    email VARCHAR(100),                -- 电子邮件
    phone VARCHAR(20),                 -- 联系电话
    userTypeId INT,                    -- 用户类型ID
    active BOOLEAN NOT NULL            -- 账户是否激活
) ENGINE=InnoDB;
```

#### UserType 表
定义系统中的用户类型。
```
CREATE TABLE UserType (
    userTypeId INT PRIMARY KEY AUTO_INCREMENT,
    typeName VARCHAR(50) NOT NULL,     -- 类型名称
    typeCode VARCHAR(20) NOT NULL,     -- 类型代码
    description VARCHAR(255),          -- 类型描述
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP,
    updateTime DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_user_type_code` (typeCode)
) ENGINE=InnoDB;
```

#### Role 表
定义系统中的角色。
```
CREATE TABLE Role (
    roleId INT PRIMARY KEY,            -- 角色唯一标识符
    name VARCHAR(100) NOT NULL,        -- 角色名称
    description VARCHAR(255)           -- 角色描述
) ENGINE=InnoDB;
```

#### Permission 表
存储系统中的权限定义。
```
CREATE TABLE Permission (
    permissionId INT PRIMARY KEY,      -- 权限唯一标识符
    name VARCHAR(100) NOT NULL,        -- 权限名称
    description VARCHAR(255),          -- 权限描述
    resourceType VARCHAR(50)           -- 权限资源类型
) ENGINE=InnoDB;
```

#### UserRole 表
用户与角色的多对多关联表。
```
CREATE TABLE UserRole (
    userId INT,                        -- 用户ID
    roleId INT,                        -- 角色ID
    PRIMARY KEY (userId, roleId),      -- 复合主键
    FOREIGN KEY (userId) REFERENCES `User`(userId),
    FOREIGN KEY (roleId) REFERENCES Role(roleId)
) ENGINE=InnoDB;
```

#### RolePermission 表
角色与权限的多对多关联表。
```
CREATE TABLE RolePermission (
    roleId INT,                        -- 角色ID
    permissionId INT,                  -- 权限ID
    PRIMARY KEY (roleId, permissionId),-- 复合主键
    FOREIGN KEY (roleId) REFERENCES Role(roleId),
    FOREIGN KEY (permissionId) REFERENCES Permission(permissionId)
) ENGINE=InnoDB;
```

### 3.2 会议室管理模块

#### MeetingRoom 表
存储会议室相关信息。
```
CREATE TABLE MeetingRoom (
    roomId INT PRIMARY KEY,            -- 会议室唯一标识符
    name VARCHAR(100) NOT NULL,        -- 会议室名称
    location VARCHAR(255),             -- 会议室位置
    capacity INT,                      -- 容纳人数
    status INT,                        -- 会议室状态
    description VARCHAR(255)           -- 会议室描述
) ENGINE=InnoDB;
```

#### Equipment 表
存储会议室内的设备信息。
```
CREATE TABLE Equipment (
    equipmentId INT PRIMARY KEY,       -- 设备唯一标识符
    roomId INT,                        -- 所属会议室ID
    name VARCHAR(100) NOT NULL,        -- 设备名称
    model VARCHAR(100),                -- 设备型号
    status INT,                        -- 设备状态
    purchaseDate DATE,                 -- 购买日期
    FOREIGN KEY (roomId) REFERENCES MeetingRoom(roomId)
) ENGINE=InnoDB;
```

#### RoomType 表
定义会议室类型。
```
CREATE TABLE RoomType (
    roomTypeId INT PRIMARY KEY AUTO_INCREMENT,
    typeName VARCHAR(50) NOT NULL,     -- 类型名称
    typeCode VARCHAR(20) NOT NULL,     -- 类型代码
    description VARCHAR(255),          -- 类型描述
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP,
    updateTime DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_room_type_code` (typeCode)
) ENGINE=InnoDB;
```

#### UserTypeRoomTypePermission 表
用户类型与会议室类型的权限映射表。
```
CREATE TABLE UserTypeRoomTypePermission (
    id INT PRIMARY KEY AUTO_INCREMENT,
    userTypeId INT NOT NULL,           -- 用户类型ID
    roomTypeId INT NOT NULL,           -- 会议室类型ID
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP,
    createBy INT,                      -- 创建人ID
    UNIQUE KEY `uk_user_type_room_type` (userTypeId, roomTypeId),
    FOREIGN KEY (roomTypeId) REFERENCES RoomType(roomTypeId) ON DELETE CASCADE
) ENGINE=InnoDB;
```

### 3.3 预约管理模块

#### Reservation 表
存储会议室预约信息。
```
CREATE TABLE Reservation (
    reservationId INT PRIMARY KEY,     -- 预约唯一标识符
    userId INT,                        -- 预约用户ID
    roomId INT,                        -- 预约会议室ID
    startTime DATETIME NOT NULL,       -- 预约开始时间
    endTime DATETIME NOT NULL,         -- 预约结束时间
    subject VARCHAR(255),              -- 会议主题
    description TEXT,                  -- 会议描述
    status INT,                        -- 预约状态
    createdTime DATETIME,              -- 创建时间
    FOREIGN KEY (userId) REFERENCES `User`(userId),
    FOREIGN KEY (roomId) REFERENCES MeetingRoom(roomId)
) ENGINE=InnoDB;
```

#### ReservationParticipant 表
会议参与者关联表。
```
CREATE TABLE ReservationParticipant (
    reservationId INT,                 -- 预约ID
    userId INT,                        -- 参与者用户ID
    confirmed BOOLEAN,                 -- 是否确认参加
    PRIMARY KEY (reservationId, userId),-- 复合主键
    FOREIGN KEY (reservationId) REFERENCES Reservation(reservationId),
    FOREIGN KEY (userId) REFERENCES `User`(userId)
) ENGINE=InnoDB;
```

### 3.4 设备维护模块

#### EquipmentMaintenance 表
存储设备维护记录。
```
CREATE TABLE EquipmentMaintenance (
    maintenanceId INT PRIMARY KEY,     -- 维护记录唯一标识符
    equipmentId INT,                   -- 设备ID
    maintenanceTime DATETIME,          -- 维护时间
    description TEXT,                  -- 维护描述
    status INT,                        -- 维护状态
    FOREIGN KEY (equipmentId) REFERENCES Equipment(equipmentId)
) ENGINE=InnoDB;
```

### 3.5 通知与日志模块

#### Notification 表
存储系统通知信息。
```
CREATE TABLE Notification (
    notificationId INT PRIMARY KEY,    -- 通知唯一标识符
    senderId INT,                      -- 发送者ID
    receiverId INT,                    -- 接收者ID
    reservationId INT,                 -- 相关预约ID
    title VARCHAR(255),                -- 通知标题
    content TEXT,                      -- 通知内容
    sentTime DATETIME,                 -- 发送时间
    `read` BOOLEAN DEFAULT FALSE,      -- 是否已读
    FOREIGN KEY (senderId) REFERENCES `User`(userId),
    FOREIGN KEY (receiverId) REFERENCES `User`(userId),
    FOREIGN KEY (reservationId) REFERENCES Reservation(reservationId)
) ENGINE=InnoDB;
```

#### SystemLog 表
存储系统操作日志。
```
CREATE TABLE SystemLog (
    logId INT PRIMARY KEY,             -- 日志唯一标识符
    userId INT,                        -- 操作用户ID
    operation VARCHAR(50),             -- 操作类型
    operationTime DATETIME,            -- 操作时间
    ipAddress VARCHAR(50),             -- 操作IP地址
    details TEXT,                      -- 详细信息
    FOREIGN KEY (userId) REFERENCES `User`(userId)
) ENGINE=InnoDB;
```

#### SystemConfig 表
存储系统配置参数。
```
CREATE TABLE SystemConfig (
    configId INT PRIMARY KEY AUTO_INCREMENT,
    configKey VARCHAR(50) NOT NULL COMMENT '配置键',
    configValue VARCHAR(255) COMMENT '配置值',
    description VARCHAR(255) COMMENT '描述',
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updateTime DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_config_key` (configKey)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';
```

## 4. 实体关系说明

### 4.1 用户授权关系
- 每个**用户**可以被分配多个**角色**（通过UserRole表）
- 每个**角色**可以拥有多个**权限**（通过RolePermission表）
- 权限控制基于RBAC（基于角色的访问控制）模型

### 4.2 会议室与设备
- 每个**会议室**包含多个**设备**
- **设备**需要定期**维护**（通过EquipmentMaintenance表记录）

### 4.3 预约关系
- **用户**可以创建多个会议室**预约**
- 一个**预约**涉及一个特定的**会议室**
- 多个**用户**可以参与一个**预约**（通过ReservationParticipant表）

### 4.4 通知机制
- **预约**可以生成**通知**
- **通知**有发送者和接收者，都是**用户**

### 4.5 系统日志
- **系统日志**记录**用户**的所有重要操作

### 4.6 会议室类型与访问控制

系统实现了基于用户类型的会议室访问控制机制：

#### 会议室类型

| 类型代码 | 类型名称 | 描述 |
|---------|---------|------|
| NORMAL | 普通会议室 | 基础设施的会议室，适合一般会议使用 |
| ADVANCED | 高级会议室 | 配备高级设备（如视频会议系统）的会议室 |
| VIP | VIP会议室 | 为高管或特殊会议准备的高档会议室 |
| TRAINING | 培训室 | 专为培训活动设计的会议室 |
| CONFERENCE | 大型会议厅 | 可容纳大量人员的大型会议厅 |

#### 会议室类型访问控制策略

| 用户类型 | 可访问的会议室类型 |
|---------|------------------|
| 普通员工 | NORMAL, TRAINING |
| 部门经理 | NORMAL, ADVANCED, TRAINING |
| 高级管理层 | NORMAL, ADVANCED, VIP, TRAINING |
| 培训师 | NORMAL, TRAINING |
| 行政人员 | NORMAL, ADVANCED, TRAINING, CONFERENCE |
| 系统管理员 | 所有类型 |

这种设计允许系统管理员通过用户类型属性和会议室类型的映射关系，灵活控制不同用户对不同会议室的访问权限。系统会在用户尝试预约会议室时，检查用户是否有权限预约该类型的会议室。

## 5. 业务流程示例

### 5.1 会议室预约流程
1. 用户登录系统（验证User表中的凭据）
2. 系统检查用户权限（通过UserRole和RolePermission表）
3. 用户查看可用会议室（MeetingRoom表）
4. 用户创建预约（插入Reservation表）
5. 用户添加会议参与者（插入ReservationParticipant表）
6. 系统向参与者发送通知（插入Notification表）
7. 系统记录操作日志（插入SystemLog表）

### 5.2 设备维护流程
1. 管理员发现设备问题
2. 创建维护记录（插入EquipmentMaintenance表）
3. 更新设备状态（更新Equipment表）
4. 完成维护后，更新维护记录状态
5. 系统记录操作日志

## 6. 数据库优化建议

1. **索引优化**：
   - 为频繁查询的字段添加索引，如User表的username、MeetingRoom表的status等
   - 为外键关系添加索引，提高连接查询效率

2. **查询优化**：
   - 对于复杂的预约查询，考虑使用视图简化查询
   - 预约冲突检测应使用适当的SQL查询确保高效

3. **数据安全**：
   - 用户密码应当使用强哈希算法存储
   - 考虑敏感数据加密存储

4. **扩展性考虑**：
   - 添加软删除功能，避免重要数据被直接删除
   - 考虑为各主表添加创建时间和更新时间字段

## 7. 总结

该数据库设计提供了一个全面的会议室预约管理系统框架，包含用户管理、会议室管理、预约管理、设备维护和系统日志等功能模块。系统采用RBAC权限模型，能够灵活地控制不同用户的访问权限。数据库使用InnoDB存储引擎，支持事务处理和外键约束，确保数据一致性。

该设计满足了现代企业对会议室资源管理的需求，支持会议预约、参与者管理、设备维护和系统监控等核心功能。

