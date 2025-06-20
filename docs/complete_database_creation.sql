-- 数据库创建脚本
-- 删除已存在的数据库（如果需要）
DROP DATABASE IF EXISTS conference_room_booking;

-- 创建新数据库
CREATE DATABASE conference_room_booking;

-- 使用新数据库
USE conference_room_booking;

-- 用户表
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

-- 会议室类型表
CREATE TABLE RoomType (
    roomTypeId INT PRIMARY KEY AUTO_INCREMENT,
    typeName VARCHAR(50) NOT NULL,     -- 类型名称
    typeCode VARCHAR(20) NOT NULL,     -- 类型代码
    description VARCHAR(255),          -- 类型描述
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP,
    updateTime DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_room_type_code` (typeCode)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会议室类型表';

-- 会议室表
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

-- 设备表
CREATE TABLE Equipment (
    equipmentId INT PRIMARY KEY AUTO_INCREMENT,       -- 设备唯一标识符
    roomId INT,                        -- 所属会议室ID
    name VARCHAR(100) NOT NULL,        -- 设备名称
    model VARCHAR(100),                -- 设备型号
    status INT,                        -- 设备状态
    purchaseDate DATE,                 -- 购买日期
    FOREIGN KEY (roomId) REFERENCES MeetingRoom(roomId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备表';

-- 预约表
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

