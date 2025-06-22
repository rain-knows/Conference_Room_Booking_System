-- 会议室预约管理系统完整数据库创建脚本
-- 包含表结构创建和测试数据插入

-- 删除已存在的数据库（如果需要）
DROP DATABASE IF EXISTS conference_room_booking;

-- 创建新数据库
CREATE DATABASE conference_room_booking;

-- 使用新数据库
USE conference_room_booking;

-- ========================================
-- 1. 创建表结构
-- ========================================

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

-- 权限映射表
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

-- ========================================
-- 2. 插入基础数据
-- ========================================

-- 插入会议室类型数据
INSERT INTO RoomType (typeName, typeCode, description) VALUES
('基础会议室', 'BASIC', '标准配置的会议室，适合日常会议'),
('高级会议室', 'PREMIUM', '配备高级设备的会议室，适合重要会议'),
('VIP会议室', 'VIP', '豪华配置的会议室，适合高层会议');

-- 插入用户数据
-- 注意：密码应该是经过哈希处理的，这里为了演示方便使用了明文
INSERT INTO `User` (username, password, fullName, email, phone, role, active) VALUES
('tnormal', '123', '张三', 'zhang.san@example.com', '13800138001', 'NORMAL_EMPLOYEE', 1),
('tleader', '123', '李四', 'li.si@example.com', '13800138002', 'LEADER', 1),
('tadmin', '123', '系统管理员', 'admin@example.com', '13800138003', 'SYSTEM_ADMIN', 1),
('test3', '123', '王五', 'wang.wu@example.com', '13800138004', 'NORMAL_EMPLOYEE', 0); -- 这是一个未激活的账户

-- 插入会议室数据
-- status: 1 = 可用, 2 = 维修中, 3 = 已停用
INSERT INTO MeetingRoom (name, location, capacity, status, description, roomTypeId) VALUES
('101会议室', '总部A座1层', 10, 1, '小型会议室，配有白板', 1),
('205会议室', '总部B座2层', 25, 1, '中型会议室，适合团队讨论', 1),
('301行政会议室', '总部A座3层', 15, 1, 'VIP会议室，配有视频会议系统', 3);

-- 插入设备数据
-- status: 1 = 正常, 2 = 维修中, 3 = 报废
INSERT INTO Equipment (roomId, name, model, status, purchaseDate) VALUES
(1, '白板', 'Model-W100', 1, '2023-01-10'),
(1, '投影仪', 'Projector-X1', 1, '2023-01-10'),
(2, '投影仪', 'Projector-Z2', 1, '2022-08-15'),
(2, '音响系统', 'Audio-S5', 1, '2022-08-15'),
(3, '视频会议系统', 'VideoConf-Pro', 1, '2023-05-20'),
(3, '智能电视', 'SmartTV-A80', 1, '2023-05-20'),
(3, '激光笔', 'Laser-P1', 2, '2023-06-01'); -- 维修中的设备

-- 插入预约数据
-- status: 1 = 已确认, 2 = 已取消, 3 = 进行中, 4 = 已完成
INSERT INTO Reservation (userId, roomId, startTime, endTime, subject, description, status, createdTime) VALUES
(1, 1, '2025-06-20 09:00:00', '2025-06-20 10:30:00', '周常项目同步会', '讨论本周项目进展和下周计划。', 4, '2025-06-18 10:00:00'),
(2, 3, '2025-06-20 14:00:00', '2025-06-20 16:00:00', 'Q2季度战略评审会', '与各部门负责人评审第二季度战略目标完成情况。', 1, '2025-06-15 11:30:00'),
(1, 2, '2025-06-21 10:00:00', '2025-06-21 11:00:00', '新功能需求讨论', '产品、研发和设计团队共同参与。', 1, '2025-06-19 15:00:00'),
(1, 1, '2025-06-22 09:00:00', '2025-06-22 10:00:00', '客户培训安排（已取消）', '原定的客户培训会议。', 2, '2025-06-17 14:20:00');

-- 插入权限映射数据
INSERT INTO PermissionMapping (userRole, roomTypeCode, canBook, canView, canManage, description) VALUES
-- SYSTEM_ADMIN 拥有所有权限
('SYSTEM_ADMIN', 'BASIC', true, true, true, '系统管理员可完全管理基础会议室'),
('SYSTEM_ADMIN', 'PREMIUM', true, true, true, '系统管理员可完全管理高级会议室'),
('SYSTEM_ADMIN', 'VIP', true, true, true, '系统管理员可完全管理VIP会议室'),

-- LEADER 拥有大部分权限
('LEADER', 'BASIC', true, true, false, '领导可预订和查看基础会议室'),
('LEADER', 'PREMIUM', true, true, false, '领导可预订和查看高级会议室'),
('LEADER', 'VIP', true, true, false, '领导可预订和查看VIP会议室'),

-- NORMAL_EMPLOYEE 只有基础权限
('NORMAL_EMPLOYEE', 'BASIC', true, true, false, '普通员工可预订和查看基础会议室'),
('NORMAL_EMPLOYEE', 'PREMIUM', false, true, false, '普通员工只能查看高级会议室'),
('NORMAL_EMPLOYEE', 'VIP', false, false, false, '普通员工无VIP会议室权限');

-- ========================================
-- 3. 数据验证查询
-- ========================================

-- 验证数据插入结果
SELECT '用户数据' as 表名, COUNT(*) as 记录数 FROM `User`
UNION ALL
SELECT '会议室类型', COUNT(*) FROM RoomType
UNION ALL
SELECT '会议室', COUNT(*) FROM MeetingRoom
UNION ALL
SELECT '设备', COUNT(*) FROM Equipment
UNION ALL
SELECT '预约', COUNT(*) FROM Reservation
UNION ALL
SELECT '权限映射', COUNT(*) FROM PermissionMapping;

-- 显示用户信息
SELECT '用户信息' as 信息类型;
SELECT userId, username, fullName, role, active FROM `User`;

-- 显示会议室信息
SELECT '会议室信息' as 信息类型;
SELECT r.roomId, r.name, r.location, r.capacity, rt.typeName, r.status 
FROM MeetingRoom r 
JOIN RoomType rt ON r.roomTypeId = rt.roomTypeId;

-- 显示权限配置
SELECT '权限配置' as 信息类型;
SELECT userRole, roomTypeCode, canBook, canView, canManage, description 
FROM PermissionMapping 
ORDER BY userRole, roomTypeCode;

