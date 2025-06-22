image.png# 会议室预约管理系统数据库设计文档

## 1. 系统概述

本文档描述了一个用于管理会议室预约和用户权限的综合数据库系统。该系统允许用户创建和管理会议室预约、跟踪设备状态和管理用户权限。系统采用基于字段映射的权限管理机制，通过用户角色和会议室类型的组合来控制访问权限。

## 2. 数据库架构

该数据库由简化的几个核心表组成，可分为四个主要功能模块：

1.  **用户管理模块**：User
2.  **会议室管理模块**：MeetingRoom, Equipment, RoomType
3.  **预约管理模块**：Reservation
4.  **权限管理模块**：PermissionMapping

## 3. 表结构详情

### 3.1 用户管理模块

#### User 表
存储系统用户信息。用户的角色直接在此表中定义。
```
CREATE TABLE `User` (
    userId INT PRIMARY KEY AUTO_INCREMENT,            -- 用户唯一标识符
    username VARCHAR(50) NOT NULL UNIQUE,     -- 用户名
    password VARCHAR(255) NOT NULL,    -- 密码（应加密存储）
    fullName VARCHAR(100),             -- 用户全名
    email VARCHAR(100) UNIQUE,                -- 电子邮件
    phone VARCHAR(20),                 -- 联系电话
    role ENUM('NORMAL_EMPLOYEE', 'LEADER', 'SYSTEM_ADMIN') NOT NULL, -- 用户角色
    active BOOLEAN NOT NULL DEFAULT TRUE            -- 账户是否激活
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
```

### 3.2 会议室管理模块

#### MeetingRoom 表
存储会议室相关信息。
```
CREATE TABLE MeetingRoom (
    roomId INT PRIMARY KEY AUTO_INCREMENT,            -- 会议室唯一标识符
    name VARCHAR(100) NOT NULL,        -- 会议室名称
    location VARCHAR(255),             -- 会议室位置
    capacity INT,                      -- 容纳人数
    status INT,                        -- 会议室状态
    description VARCHAR(255),          -- 会议室描述
    roomTypeId INT,                    -- 会议室类型ID
    FOREIGN KEY (roomTypeId) REFERENCES RoomType(roomTypeId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会议室表';
```

#### Equipment 表
存储会议室内的设备信息。
```
CREATE TABLE Equipment (
    equipmentId INT PRIMARY KEY AUTO_INCREMENT,       -- 设备唯一标识符
    roomId INT,                        -- 所属会议室ID
    name VARCHAR(100) NOT NULL,        -- 设备名称
    model VARCHAR(100),                -- 设备型号
    status INT,                        -- 设备状态
    purchaseDate DATE,                 -- 购买日期
    FOREIGN KEY (roomId) REFERENCES MeetingRoom(roomId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备表';
```

#### RoomType 表
定义会议室类型。支持基础、高级、VIP三种类型。
```
CREATE TABLE RoomType (
    roomTypeId INT PRIMARY KEY AUTO_INCREMENT,
    typeName VARCHAR(50) NOT NULL,     -- 类型名称
    typeCode VARCHAR(20) NOT NULL,     -- 类型代码
    description VARCHAR(255),          -- 类型描述
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP,
    updateTime DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_room_type_code` (typeCode)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会议室类型表';
```

### 3.3 预约管理模块

#### Reservation 表
存储会议室预约信息。
```
CREATE TABLE Reservation (
    reservationId INT PRIMARY KEY AUTO_INCREMENT,     -- 预约唯一标识符
    userId INT,                        -- 预约用户ID
    roomId INT,                        -- 预约会议室ID
    startTime DATETIME NOT NULL,       -- 预约开始时间
    endTime DATETIME NOT NULL,         -- 预约结束时间
    subject VARCHAR(255),              -- 会议主题
    description TEXT,                  -- 会议描述
    status INT,                        -- 预约状态
    createdTime DATETIME DEFAULT CURRENT_TIMESTAMP,              -- 创建时间
    FOREIGN KEY (userId) REFERENCES `User`(userId),
    FOREIGN KEY (roomId) REFERENCES MeetingRoom(roomId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约表';
```

### 3.4 权限管理模块

#### PermissionMapping 表
存储用户角色和会议室类型的权限映射关系。
```
CREATE TABLE PermissionMapping (
    mappingId INT PRIMARY KEY AUTO_INCREMENT,
    userRole VARCHAR(50) NOT NULL,           -- 用户角色 (NORMAL_EMPLOYEE, LEADER, SYSTEM_ADMIN)
    roomTypeCode VARCHAR(20) NOT NULL,       -- 会议室类型代码
    canBook BOOLEAN NOT NULL DEFAULT FALSE,  -- 是否可以预订
    canView BOOLEAN NOT NULL DEFAULT FALSE,  -- 是否可以查看
    canManage BOOLEAN NOT NULL DEFAULT FALSE, -- 是否可以管理
    description VARCHAR(255),                -- 权限描述
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP,
    updateTime DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_user_role_room_type` (userRole, roomTypeCode),
    INDEX `idx_user_role` (userRole),
    INDEX `idx_room_type_code` (roomTypeCode)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限映射表';
```

### 3.5 实体关系说明

#### 3.5.1 用户授权关系
- 每个**用户**被分配一个**角色**（NORMAL_EMPLOYEE, LEADER, SYSTEM_ADMIN）
- 通过**PermissionMapping**表定义角色对会议室类型的权限
- 支持查看、预订、管理三种权限类型

#### 3.5.2 会议室与设备
- 每个**会议室**属于一个**会议室类型**
- 每个**会议室**包含多个**设备**

#### 3.5.3 预约关系
- **用户**可以创建多个会议室**预约**
- 一个**预约**涉及一个特定的**会议室**

#### 3.5.4 权限映射关系
- **用户角色** ↔ **会议室类型** → **权限配置**
- 支持细粒度的权限控制
- 可以动态配置和修改权限

### 3.6 默认权限配置

| 用户角色 | 基础会议室 | 高级会议室 | VIP会议室 |
|---------|-----------|-----------|-----------|
| SYSTEM_ADMIN | 完全管理 | 完全管理 | 完全管理 |
| LEADER | 可预订查看 | 可预订查看 | 可预订查看 |
| NORMAL_EMPLOYEE | 可预订查看 | 仅查看 | 无权限 |

## 4. 数据库优化建议

1.  **索引优化**：
    -   为频繁查询的字段添加索引，如User表的username、MeetingRoom表的status等
    -   为外键关系添加索引，提高连接查询效率
    -   为PermissionMapping表的userRole和roomTypeCode字段添加索引

2.  **查询优化**：
    -   对于复杂的预约查询，考虑使用视图简化查询
    -   预约冲突检测应使用适当的SQL查询确保高效
    -   权限检查查询应优化，避免频繁的数据库访问

3.  **数据安全**：
    -   用户密码应当使用强哈希算法存储
    -   考虑敏感数据加密存储
    -   权限数据应定期备份

4.  **扩展性考虑**：
    -   添加软删除功能，避免重要数据被直接删除
    -   考虑为各主表添加创建时间和更新时间字段
    -   权限系统支持动态扩展新的用户角色和会议室类型

## 5. 总结

该数据库设计提供了一个完整的会议室预约管理系统框架，包含用户管理、会议室管理、预约管理和权限管理等功能模块。系统采用基于字段映射的权限模型，能够灵活地控制不同用户对不同类型会议室的访问权限。数据库使用InnoDB存储引擎，支持事务处理和外键约束，确保数据一致性。

该设计满足了企业对会议室资源管理的基本需求，支持会议预约、权限控制和参与者管理等核心功能，并具有良好的扩展性和维护性。
