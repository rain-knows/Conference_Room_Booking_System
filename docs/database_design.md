image.png# 会议室预约管理系统数据库设计文档

## 1. 系统概述

本文档描述了一个用于管理会议室预约和用户权限的综合数据库系统。该系统允许用户创建和管理会议室预约、跟踪设备状态和管理用户权限。

## 2. 数据库架构

该数据库由简化的几个核心表组成，可分为三个主要功能模块：

1.  **用户管理模块**：User
2.  **会议室管理模块**：MeetingRoom, Equipment, RoomType
3.  **预约管理模块**：Reservation

## 3. 表结构详情

### 3.1 用户管理模块

#### User 表
存储系统用户信息。用户的角色直接在此表中定义。
```
CREATE TABLE `User` (
    userId INT PRIMARY KEY,            -- 用户唯一标识符
    username VARCHAR(50) NOT NULL,     -- 用户名
    password VARCHAR(255) NOT NULL,    -- 密码（应加密存储）
    fullName VARCHAR(100),             -- 用户全名
    email VARCHAR(100),                -- 电子邮件
    phone VARCHAR(20),                 -- 联系电话
    role ENUM('NORMAL_EMPLOYEE', 'LEADER', 'SYSTEM_ADMIN') NOT NULL, -- 用户角色
    active BOOLEAN NOT NULL            -- 账户是否激活
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
    description VARCHAR(255),          -- 会议室描述
    roomTypeId INT,                    -- 会议室类型ID
    FOREIGN KEY (roomTypeId) REFERENCES RoomType(roomTypeId)
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
定义会议室类型。会议室类型简化为 **普通** 和 **高级**。
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

### 3.4 实体关系说明

#### 3.4.1 用户授权关系
- 每个**用户**被分配一个**角色**。
- 权限直接与角色关联，不再有独立的权限表。

#### 3.4.2 会议室与设备
- 每个**会议室**包含多个**设备**

#### 3.4.3 预约关系
- **用户**可以创建多个会议室**预约**
- 一个**预约**涉及一个特定的**会议室**

## 4. 数据库优化建议

1.  **索引优化**：
    -   为频繁查询的字段添加索引，如User表的username、MeetingRoom表的status等
    -   为外键关系添加索引，提高连接查询效率

2.  **查询优化**：
    -   对于复杂的预约查询，考虑使用视图简化查询
    -   预约冲突检测应使用适当的SQL查询确保高效

3.  **数据安全**：
    -   用户密码应当使用强哈希算法存储
    -   考虑敏感数据加密存储

4.  **扩展性考虑**：
    -   添加软删除功能，避免重要数据被直接删除
    -   考虑为各主表添加创建时间和更新时间字段

## 5. 总结

该数据库设计提供了一个简化的会议室预约管理系统框架，包含用户管理、会议室管理和预约管理等功能模块。系统采用简化的基于角色的权限模型，能够直接地控制不同用户的访问权限。数据库使用InnoDB存储引擎，支持事务处理和外键约束，确保数据一致性。

该设计满足了企业对会议室资源管理的基本需求，支持会议预约和参与者管理等核心功能。
