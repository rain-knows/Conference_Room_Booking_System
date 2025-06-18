-- 会议室预约管理系统数据库创建脚本
-- 创建日期：2025-05-26


-- 删除表（如果存在）以避免冲突，按照依赖关系的逆序删除
DROP TABLE IF EXISTS SystemLog;
DROP TABLE IF EXISTS Notification;
DROP TABLE IF EXISTS EquipmentMaintenance;
DROP TABLE IF EXISTS ReservationParticipant;
DROP TABLE IF EXISTS Reservation;
DROP TABLE IF EXISTS Equipment;
DROP TABLE IF EXISTS UserTypeRoomTypePermission;
DROP TABLE IF EXISTS MeetingRoom;
DROP TABLE IF EXISTS RoomType;
DROP TABLE IF EXISTS UserRole;
DROP TABLE IF EXISTS RolePermission;
DROP TABLE IF EXISTS Permission;
DROP TABLE IF EXISTS Role;
DROP TABLE IF EXISTS User;
DROP TABLE IF EXISTS UserType;
DROP TABLE IF EXISTS SystemConfig;

-- 创建用户类型表
CREATE TABLE UserType (
    userTypeId INT PRIMARY KEY AUTO_INCREMENT,
    typeName VARCHAR(50) NOT NULL COMMENT '类型名称',
    typeCode VARCHAR(20) NOT NULL COMMENT '类型代码',
    description VARCHAR(255) COMMENT '类型描述',
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updateTime DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_user_type_code` (typeCode)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户类型表';

-- 创建用户表
CREATE TABLE User (
    userId INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（应加密存储）',
    fullName VARCHAR(100) COMMENT '用户全名',
    email VARCHAR(100) COMMENT '电子邮件',
    phone VARCHAR(20) COMMENT '联系电话',
    userTypeId INT COMMENT '用户类型ID',
    active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '账户是否激活',
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updateTime DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    lastLogin DATETIME COMMENT '最后登录时间',
    UNIQUE KEY `uk_username` (username),
    FOREIGN KEY (userTypeId) REFERENCES UserType(userTypeId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 创建角色表
CREATE TABLE Role (
    roleId INT PRIMARY KEY AUTO_INCREMENT,
    roleName VARCHAR(100) NOT NULL COMMENT '角色名称',
    roleCode VARCHAR(50) NOT NULL COMMENT '角色代码',
    description VARCHAR(255) COMMENT '角色描述',
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updateTime DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    active BOOLEAN DEFAULT TRUE COMMENT '是否激活',
    UNIQUE KEY `uk_role_code` (roleCode)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 创建权限表
CREATE TABLE Permission (
    permissionId INT PRIMARY KEY AUTO_INCREMENT,
    permissionName VARCHAR(100) NOT NULL COMMENT '权限名称',
    permissionCode VARCHAR(50) NOT NULL COMMENT '权限代码',
    description VARCHAR(255) COMMENT '权限描述',
    resourceType VARCHAR(50) COMMENT '资源类型',
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updateTime DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_permission_code` (permissionCode)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 创建角色权限关系表
CREATE TABLE RolePermission (
    roleId INT NOT NULL,
    permissionId INT NOT NULL,
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (roleId, permissionId),
    FOREIGN KEY (roleId) REFERENCES Role(roleId) ON DELETE CASCADE,
    FOREIGN KEY (permissionId) REFERENCES Permission(permissionId) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关系表';

-- 创建用户角色关系表
CREATE TABLE UserRole (
    userId INT NOT NULL,
    roleId INT NOT NULL,
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (userId, roleId),
    FOREIGN KEY (userId) REFERENCES User(userId) ON DELETE CASCADE,
    FOREIGN KEY (roleId) REFERENCES Role(roleId) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关系表';

-- 创建会议室类型表
CREATE TABLE RoomType (
    roomTypeId INT PRIMARY KEY AUTO_INCREMENT,
    typeName VARCHAR(50) NOT NULL COMMENT '类型名称',
    typeCode VARCHAR(20) NOT NULL COMMENT '类型代码',
    description VARCHAR(255) COMMENT '类型描述',
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updateTime DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_room_type_code` (typeCode)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会议室类型表';

-- 创建会议室表
CREATE TABLE MeetingRoom (
    roomId INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '会议室名称',
    location VARCHAR(255) COMMENT '会议室位置',
    capacity INT COMMENT '容纳人数',
    roomTypeId INT NOT NULL COMMENT '会议室类型ID',
    status INT DEFAULT 1 COMMENT '会议室状态：1-可用，2-已预约，3-维护中',
    description VARCHAR(255) COMMENT '会议室描述',
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP,
    updateTime DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (roomTypeId) REFERENCES RoomType(roomTypeId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会议室表';

-- 创建用户类型与会议室类型权限映射表
CREATE TABLE UserTypeRoomTypePermission (
    id INT PRIMARY KEY AUTO_INCREMENT,
    userTypeId INT NOT NULL COMMENT '用户类型ID',
    roomTypeId INT NOT NULL COMMENT '会议室类型ID',
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    createBy INT COMMENT '创建人ID',
    UNIQUE KEY `uk_user_type_room_type` (userTypeId, roomTypeId),
    FOREIGN KEY (userTypeId) REFERENCES UserType(userTypeId),
    FOREIGN KEY (roomTypeId) REFERENCES RoomType(roomTypeId) ON DELETE CASCADE,
    FOREIGN KEY (createBy) REFERENCES User(userId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户类型与会议室类型权限映射表';

-- 创建设备表
CREATE TABLE Equipment (
    equipmentId INT PRIMARY KEY AUTO_INCREMENT,
    roomId INT COMMENT '所属会议室ID',
    name VARCHAR(100) NOT NULL COMMENT '设备名称',
    model VARCHAR(100) COMMENT '设备型号',
    status INT DEFAULT 1 COMMENT '设备状态：1-正常，2-维护中，3-损坏',
    purchaseDate DATE COMMENT '购买日期',
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP,
    updateTime DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (roomId) REFERENCES MeetingRoom(roomId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备表';

-- 创建预约表
CREATE TABLE Reservation (
    reservationId INT PRIMARY KEY AUTO_INCREMENT,
    userId INT COMMENT '预约用户ID',
    roomId INT COMMENT '预约会议室ID',
    startTime DATETIME NOT NULL COMMENT '预约开始时间',
    endTime DATETIME NOT NULL COMMENT '预约结束时间',
    subject VARCHAR(255) COMMENT '会议主题',
    description TEXT COMMENT '会议描述',
    status INT DEFAULT 1 COMMENT '预约状态：1-已预约，2-已取消，3-已完成，4-爽约',
    createdTime DATETIME DEFAULT CURRENT_TIMESTAMP,
    updatedTime DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (userId) REFERENCES User(userId),
    FOREIGN KEY (roomId) REFERENCES MeetingRoom(roomId),
    INDEX `idx_reservation_time` (startTime, endTime),
    INDEX `idx_reservation_room` (roomId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约表';

-- 创建预约参与者表
CREATE TABLE ReservationParticipant (
    reservationId INT,
    userId INT,
    confirmed BOOLEAN DEFAULT FALSE COMMENT '是否确认参加',
    notified BOOLEAN DEFAULT FALSE COMMENT '是否已通知',
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP,
    updateTime DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (reservationId, userId),
    FOREIGN KEY (reservationId) REFERENCES Reservation(reservationId) ON DELETE CASCADE,
    FOREIGN KEY (userId) REFERENCES User(userId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约参与者表';

-- 创建设备维护表
CREATE TABLE EquipmentMaintenance (
    maintenanceId INT PRIMARY KEY AUTO_INCREMENT,
    equipmentId INT COMMENT '设备ID',
    maintenanceTime DATETIME COMMENT '维护时间',
    completionTime DATETIME COMMENT '完成时间',
    description TEXT COMMENT '维护描述',
    status INT DEFAULT 1 COMMENT '维护状态：1-待维护，2-维护中，3-已完成',
    maintainer VARCHAR(100) COMMENT '维护人员',
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP,
    updateTime DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (equipmentId) REFERENCES Equipment(equipmentId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备维护表';

-- 创建通知表
CREATE TABLE Notification (
    notificationId INT PRIMARY KEY AUTO_INCREMENT,
    senderId INT COMMENT '发送者ID',
    receiverId INT COMMENT '接收者ID',
    reservationId INT COMMENT '相关预约ID',
    title VARCHAR(255) COMMENT '通知标题',
    content TEXT COMMENT '通知内容',
    sentTime DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    readTime DATETIME COMMENT '阅读时间',
    `read` BOOLEAN DEFAULT FALSE COMMENT '是否已读',
    notificationType INT DEFAULT 1 COMMENT '通知类型：1-预约，2-取消，3-修改，4-系统',
    FOREIGN KEY (senderId) REFERENCES User(userId),
    FOREIGN KEY (receiverId) REFERENCES User(userId),
    FOREIGN KEY (reservationId) REFERENCES Reservation(reservationId) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

-- 创建系统日志表
CREATE TABLE SystemLog (
    logId INT PRIMARY KEY AUTO_INCREMENT,
    userId INT COMMENT '操作用户ID',
    operation VARCHAR(50) COMMENT '操作类型',
    operationTime DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    ipAddress VARCHAR(50) COMMENT '操作IP地址',
    details TEXT COMMENT '详细信息',
    module VARCHAR(50) COMMENT '操作模块',
    FOREIGN KEY (userId) REFERENCES User(userId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统日志表';

-- 创建系统配置表
CREATE TABLE SystemConfig (
    configId INT PRIMARY KEY AUTO_INCREMENT,
    configKey VARCHAR(50) NOT NULL COMMENT '配置键',
    configValue VARCHAR(255) COMMENT '配置值',
    description VARCHAR(255) COMMENT '描述',
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP,
    updateTime DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_config_key` (configKey)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 插入基础数据

-- 1. 插入用户类型数据
INSERT INTO UserType (typeName, typeCode, description) VALUES
('普通员工', 'NORMAL_EMPLOYEE', '公司普通员工'),
('部门经理', 'DEPARTMENT_MANAGER', '部门管理人员'),
('高级管理层', 'SENIOR_MANAGEMENT', '公司高管'),
('培训师', 'TRAINER', '负责培训的人员'),
('行政人员', 'ADMIN_STAFF', '负责行政工作的人员'),
('系统管理员', 'SYS_ADMIN', '系统管理人员');

-- 2. 插入角色数据
INSERT INTO Role (roleName, roleCode, description) VALUES
('普通用户', 'USER', '普通会议组织者'),
('会议室管理员', 'ROOM_ADMIN', '负责管理会议室资源'),
('系统管理员', 'SYS_ADMIN', '负责系统整体配置和管理');

-- 3. 插入权限数据
-- 普通用户权限
INSERT INTO Permission (permissionName, permissionCode, description, resourceType) VALUES
('查询会议室', 'ROOM_QUERY', '查看会议室列表、筛选和搜索会议室', 'ROOM'),
('查看会议室空闲状态', 'ROOM_AVAILABILITY_QUERY', '查看特定时间段内会议室的可用状态', 'ROOM'),
('创建会议预约', 'RESERVATION_CREATE', '为可用会议室创建新的预约', 'RESERVATION'),
('管理个人预约', 'RESERVATION_MANAGE_OWN', '查看、修改和取消自己创建的会议预约', 'RESERVATION'),
('发送会议通知', 'MEETING_NOTIFICATION_SEND', '向与会者发送会议通知和提醒', 'NOTIFICATION'),
('接收会议提醒', 'MEETING_REMINDER_RECEIVE', '接收关于即将到来的会议的提醒', 'NOTIFICATION'),
('管理与会者', 'PARTICIPANT_MANAGE', '添加、删除和更新会议参与者', 'PARTICIPANT'),
('请求会议设备服务', 'EQUIPMENT_SERVICE_REQUEST', '为会议请求特定设备和服务', 'EQUIPMENT');

-- 会议室管理员权限
INSERT INTO Permission (permissionName, permissionCode, description, resourceType) VALUES
('会议室管理', 'ROOM_MANAGE', '添加、修改、删除会议室信息', 'ROOM'),
('会议室状态更新', 'ROOM_STATUS_UPDATE', '手动更新会议室状态（可用、维护中等）', 'ROOM'),
('查看所有预约', 'RESERVATION_VIEW_ALL', '查看系统中所有会议预约', 'RESERVATION'),
('管理所有预约', 'RESERVATION_MANAGE_ALL', '修改或取消任何会议预约', 'RESERVATION'),
('设备管理', 'EQUIPMENT_MANAGE', '添加、修改、删除会议室设备信息', 'EQUIPMENT'),
('设备维护记录', 'EQUIPMENT_MAINTENANCE_MANAGE', '记录和管理设备维护情况', 'EQUIPMENT'),
('会议室使用统计', 'ROOM_USAGE_STATISTICS', '查看会议室使用情况统计数据', 'STATISTICS'),
('爽约记录管理', 'NO_SHOW_RECORD_MANAGE', '记录和管理用户爽约情况', 'RECORD'),
('现场服务管理', 'ONSITE_SERVICE_MANAGE', '管理会议现场所需的服务和资源', 'SERVICE');

-- 系统管理员权限
INSERT INTO Permission (permissionName, permissionCode, description, resourceType) VALUES
('用户管理', 'USER_MANAGE', '创建、修改、删除用户账户', 'USER'),
('角色分配', 'ROLE_ASSIGN', '为用户分配系统角色', 'USER'),
('系统配置', 'SYSTEM_CONFIG', '配置系统参数和设置', 'SYSTEM'),
('系统监控', 'SYSTEM_MONITOR', '监控系统状态和性能', 'SYSTEM'),
('权限管理', 'PERMISSION_MANAGE', '创建和管理自定义权限规则', 'PERMISSION'),
('会议室权限配置', 'ROOM_PERMISSION_CONFIG', '配置不同用户角色可访问的会议室类型', 'PERMISSION'),
('系统日志查看', 'SYSTEM_LOG_VIEW', '查看完整的系统操作日志', 'LOG'),
('数据备份与恢复', 'DATA_BACKUP_RESTORE', '执行系统数据的备份和恢复操作', 'SYSTEM');

-- 4. 插入角色权限关联数据
-- 普通用户权限
INSERT INTO RolePermission (roleId, permissionId)
SELECT r.roleId, p.permissionId FROM Role r, Permission p
WHERE r.roleCode = 'USER' AND p.permissionCode IN (
    'ROOM_QUERY', 'ROOM_AVAILABILITY_QUERY', 'RESERVATION_CREATE', 'RESERVATION_MANAGE_OWN',
    'MEETING_NOTIFICATION_SEND', 'MEETING_REMINDER_RECEIVE', 'PARTICIPANT_MANAGE', 'EQUIPMENT_SERVICE_REQUEST'
);

-- 会议室管理员权限
INSERT INTO RolePermission (roleId, permissionId)
SELECT r.roleId, p.permissionId FROM Role r, Permission p
WHERE r.roleCode = 'ROOM_ADMIN' AND p.permissionCode IN (
    'ROOM_QUERY', 'ROOM_AVAILABILITY_QUERY', 'RESERVATION_CREATE', 'RESERVATION_MANAGE_OWN',
    'MEETING_NOTIFICATION_SEND', 'MEETING_REMINDER_RECEIVE', 'PARTICIPANT_MANAGE', 'EQUIPMENT_SERVICE_REQUEST',
    'ROOM_MANAGE', 'ROOM_STATUS_UPDATE', 'RESERVATION_VIEW_ALL', 'RESERVATION_MANAGE_ALL',
    'EQUIPMENT_MANAGE', 'EQUIPMENT_MAINTENANCE_MANAGE', 'ROOM_USAGE_STATISTICS', 'NO_SHOW_RECORD_MANAGE',
    'ONSITE_SERVICE_MANAGE'
);

-- 系统管理员权限
INSERT INTO RolePermission (roleId, permissionId)
SELECT r.roleId, p.permissionId FROM Role r, Permission p
WHERE r.roleCode = 'SYS_ADMIN';

-- 5. 插入会议室类型数据
INSERT INTO RoomType (typeName, typeCode, description) VALUES
('普通会议室', 'NORMAL', '基础设施的会议室，适合一般会议使用'),
('高级会议室', 'ADVANCED', '配备高级设备（如视频会议系统）的会议室'),
('VIP会议室', 'VIP', '为高管或特殊会议准备的高档会议室'),
('培训室', 'TRAINING', '专为培训活动设计的会议室'),
('大型会议厅', 'CONFERENCE', '可容纳大量人员的大型会议厅');

-- 6. 设置用户类型与会议室类型的权限映射
INSERT INTO UserTypeRoomTypePermission (userTypeId, roomTypeId)
SELECT ut.userTypeId, rt.roomTypeId
FROM UserType ut, RoomType rt
WHERE (ut.typeCode = 'NORMAL_EMPLOYEE' AND rt.typeCode IN ('NORMAL', 'TRAINING'))
   OR (ut.typeCode = 'DEPARTMENT_MANAGER' AND rt.typeCode IN ('NORMAL', 'ADVANCED', 'TRAINING'))
   OR (ut.typeCode = 'SENIOR_MANAGEMENT' AND rt.typeCode IN ('NORMAL', 'ADVANCED', 'VIP', 'TRAINING'))
   OR (ut.typeCode = 'TRAINER' AND rt.typeCode IN ('NORMAL', 'TRAINING'))
   OR (ut.typeCode = 'ADMIN_STAFF' AND rt.typeCode IN ('NORMAL', 'ADVANCED', 'TRAINING', 'CONFERENCE'))
   OR (ut.typeCode = 'SYS_ADMIN');

-- 7. 插入系统配置数据
INSERT INTO SystemConfig (configKey, configValue, description) VALUES
('ENABLE_ROOM_TYPE_PERMISSION', 'true', '启用会议室类型权限控制'),
('MAX_RESERVATION_DAYS_AHEAD', '30', '允许提前预约的最大天数'),
('ENABLE_NO_SHOW_PENALTY', 'true', '启用爽约惩罚机制'),
('PASSWORD_EXPIRY_DAYS', '90', '密码过期天数'),
('SYSTEM_NAME', '会议室预约管理系统', '系统名称'),
('ADMIN_EMAIL', 'admin@example.com', '管理员邮箱'),
('NOTIFICATION_ADVANCE_MINUTES', '15', '会议开始前多少分钟发送提醒');

-- 8. 创建管理员用户
INSERT INTO User (username, password, fullName, email, userTypeId, active)
SELECT 'admin', '$2a$10$XMdF.owZ0UqYnKRwBUc8u.xPmVUll2QL/g.Fr6gJ9MVpQFZIk8qTS', '系统管理员', 'admin@example.com', ut.userTypeId, 1
FROM UserType ut WHERE ut.typeCode = 'SYS_ADMIN';

-- 9. 为管理员分配系统管理员角色
INSERT INTO UserRole (userId, roleId)
SELECT u.userId, r.roleId
FROM User u, Role r
WHERE u.username = 'admin' AND r.roleCode = 'SYS_ADMIN';

-- 创建视图：用户权限汇总视图
CREATE OR REPLACE VIEW UserPermissionView AS
SELECT
    u.userId,
    u.username,
    u.fullName,
    r.roleId,
    r.roleName,
    p.permissionId,
    p.permissionName,
    p.permissionCode
FROM
    User u
JOIN
    UserRole ur ON u.userId = ur.userId
JOIN
    Role r ON ur.roleId = r.roleId
JOIN
    RolePermission rp ON r.roleId = rp.roleId
JOIN
    Permission p ON rp.permissionId = p.permissionId
WHERE
    u.active = true AND r.active = true;

-- 创建视图：用户可访问会议室类型视图
CREATE OR REPLACE VIEW UserAccessibleRoomTypeView AS
SELECT
    u.userId,
    u.username,
    u.userTypeId,
    ut.typeName AS userTypeName,
    rt.roomTypeId,
    rt.typeName AS roomTypeName,
    rt.typeCode AS roomTypeCode
FROM
    User u
JOIN
    UserType ut ON u.userTypeId = ut.userTypeId
JOIN
    UserTypeRoomTypePermission utrp ON ut.userTypeId = utrp.userTypeId
JOIN
    RoomType rt ON utrp.roomTypeId = rt.roomTypeId
WHERE
    u.active = true;
