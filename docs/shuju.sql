-- 1. 插入会议室类型数据 (RoomType Table Data)
--
-- 定义了“普通”和“高级”两种会议室类型。
INSERT INTO `RoomType` (`roomTypeId`, `typeName`, `typeCode`, `description`) VALUES
(1, '普通会议室', 'NORMAL', '适合日常内部会议'),
(2, '高级会议室', 'VIP', '配备高级设备，适合重要会议或接待');

--
-- 2. 插入用户数据 (User Table Data)
--
-- 创建了三个不同角色的用户：普通员工、领导和系统管理员。
-- 注意：密码应该是经过哈希处理的，这里为了演示方便使用了明文。
INSERT INTO `User` (`userId`, `username`, `password`, `fullName`, `email`, `phone`, `role`, `active`) VALUES
(101, 'zhang_san', 'hashed_password_1', '张三', 'zhang.san@example.com', '13800138001', 'NORMAL_EMPLOYEE', 1),
(102, 'li_si', 'hashed_password_2', '李四', 'li.si@example.com', '13800138002', 'LEADER', 1),
(103, 'admin', 'hashed_password_3', '系统管理员', 'admin@example.com', '13800138003', 'SYSTEM_ADMIN', 1),
(104, 'wang_wu', 'hashed_password_4', '王五', 'wang.wu@example.com', '13800138004', 'NORMAL_EMPLOYEE', 0); -- 这是一个未激活的账户

--
-- 3. 插入会议室数据 (MeetingRoom Table Data)
--
-- 为每个会议室定义不同的属性。
-- status: 1 = 可用, 2 = 维修中, 3 = 已停用
INSERT INTO `MeetingRoom` (`roomId`, `name`, `location`, `capacity`, `status`, `description`, `roomTypeId`) VALUES
(1, '101会议室', '总部A座1层', 10, 1, '小型会议室，配有白板', 1),
(2, '205会议室', '总部B座2层', 25, 1, '中型会议室，适合团队讨论', 1),
(3, '301行政会议室', '总部A座3层', 15, 1, 'VIP会议室，配有视频会议系统', 2);

--
-- 4. 插入设备数据 (Equipment Table Data)
--
-- 为每个会议室添加相应的设备。
-- status: 1 = 正常, 2 = 维修中, 3 = 报废
INSERT INTO `Equipment` (`equipmentId`, `roomId`, `name`, `model`, `status`, `purchaseDate`) VALUES
(1001, 1, '白板', 'Model-W100', 1, '2023-01-10'),
(1002, 1, '投影仪', 'Projector-X1', 1, '2023-01-10'),
(1003, 2, '投影仪', 'Projector-Z2', 1, '2022-08-15'),
(1004, 2, '音响系统', 'Audio-S5', 1, '2022-08-15'),
(1005, 3, '视频会议系统', 'VideoConf-Pro', 1, '2023-05-20'),
(1006, 3, '智能电视', 'SmartTV-A80', 1, '2023-05-20'),
(1007, 3, '激光笔', 'Laser-P1', 2, '2023-06-01'); -- 维修中的设备

--
-- 5. 插入预约数据 (Reservation Table Data)
--
-- 创建一些会议预约记录。
-- status: 1 = 已确认, 2 = 已取消, 3 = 进行中, 4 = 已完成
INSERT INTO `Reservation` (`reservationId`, `userId`, `roomId`, `startTime`, `endTime`, `subject`, `description`, `status`, `createdTime`) VALUES
(1, 101, 1, '2025-06-20 09:00:00', '2025-06-20 10:30:00', '周常项目同步会', '讨论本周项目进展和下周计划。', 4, '2025-06-18 10:00:00'),
(2, 102, 3, '2025-06-20 14:00:00', '2025-06-20 16:00:00', 'Q2季度战略评审会', '与各部门负责人评审第二季度战略目标完成情况。', 1, '2025-06-15 11:30:00'),
(3, 101, 2, '2025-06-21 10:00:00', '2025-06-21 11:00:00', '新功能需求讨论', '产品、研发和设计团队共同参与。', 1, '2025-06-19 15:00:00'),
(4, 101, 1, '2025-06-22 09:00:00', '2025-06-22 10:00:00', '客户培训安排（已取消）', '原定的客户培训会议。', 2, '2025-06-17 14:20:00');
