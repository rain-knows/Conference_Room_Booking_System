# 会议室预订系统 - src目录文件说明文档

## 系统概述

这是一个基于Java Swing开发的会议室预订管理系统，采用MVC架构模式，支持多用户角色管理、会议室预订、权限控制等功能。

## 文件分类与说明

### 1. 核心启动类

#### `MainPage.java` (7.8KB, 206行)
- **功能**: 系统主界面，包含导航菜单和内容区域
- **主要特性**:
  - 左右分栏布局（侧边导航 + 主内容区）
  - 动态面板切换机制
  - 基于用户角色的权限控制
  - 响应式导航菜单
- **核心方法**:
  - `showContent(String panelKey)`: 根据导航选择更新内容面板
  - `initButtonPanelMap()`: 初始化按钮与面板映射关系
  - `registerButtonActions()`: 注册所有导航按钮事件

#### `LoginForm.java` (8.6KB, 239行)
- **功能**: 用户登录界面
- **主要特性**:
  - 现代化UI设计，左右分栏布局
  - 占位符文本支持
  - 回车键快速登录
  - 数据库连接验证
- **核心方法**:
  - `onLoginButtonClick()`: 处理登录逻辑
  - `setupPlaceholder()`: 设置输入框占位符

### 2. 数据模型层 (Model)

#### 用户相关模型

##### `User.java` (2.3KB, 101行)
- **功能**: 用户实体类
- **主要属性**:
  - `userId`: 用户ID
  - `username`: 用户名
  - `role`: 用户角色（NORMAL_EMPLOYEE/LEADER/SYSTEM_ADMIN）
  - `email`: 邮箱
  - `phone`: 电话
  - `active`: 账户激活状态
- **核心方法**:
  - `isAdmin()`: 检查是否为管理员
  - `isLeader()`: 检查是否为领导
  - `isNormalEmployee()`: 检查是否为普通员工

#### 会议室相关模型

##### `MeetingRoom.java` (4.2KB, 147行)
- **功能**: 会议室实体类
- **主要属性**:
  - `roomId`: 会议室ID
  - `name`: 会议室名称
  - `capacity`: 容纳人数
  - `location`: 位置
  - `status`: 状态（可用/维护中/已停用）
  - `roomTypeId`: 会议室类型ID
  - `roomTypeCode`: 会议室类型代码
- **核心方法**:
  - `getStatusText()`: 获取状态的中文描述

##### `RoomType.java` (2.3KB, 85行)
- **功能**: 会议室类型实体类
- **主要属性**:
  - `typeId`: 类型ID
  - `typeCode`: 类型代码
  - `typeName`: 类型名称
  - `description`: 类型描述

##### `Equipment.java` (2.2KB, 87行)
- **功能**: 设备实体类
- **主要属性**:
  - `equipmentId`: 设备ID
  - `name`: 设备名称
  - `type`: 设备类型
  - `status`: 设备状态
  - `roomId`: 所属会议室ID

#### 预订相关模型

##### `Reservation.java` (2.3KB, 89行)
- **功能**: 预订记录实体类
- **主要属性**:
  - `reservationId`: 预订ID
  - `userId`: 用户ID
  - `roomId`: 会议室ID
  - `roomName`: 会议室名称
  - `subject`: 会议主题
  - `description`: 会议描述
  - `startTime`: 开始时间
  - `endTime`: 结束时间
  - `status`: 预订状态
- **核心方法**:
  - `getStatusText()`: 获取状态的中文描述

#### 权限相关模型

##### `PermissionMapping.java` (3.4KB, 123行)
- **功能**: 权限映射实体类
- **主要属性**:
  - `mappingId`: 映射ID
  - `userRole`: 用户角色
  - `roomTypeCode`: 会议室类型代码
  - `canBook`: 是否可以预订
  - `canView`: 是否可以查看
  - `canManage`: 是否可以管理
  - `description`: 权限描述

### 3. 数据访问层 (DAO - Data Access Object)

#### `UserDAO.java` (11KB, 289行)
- **功能**: 用户数据访问对象
- **主要方法**:
  - `checkLogin()`: 用户登录验证
  - `updateUserProfile()`: 更新用户资料
  - `changePassword()`: 修改密码
  - `getAllUsers()`: 获取所有用户（管理员功能）
  - `addUser()`: 添加用户
  - `updateUserByAdmin()`: 管理员更新用户信息
  - `resetPasswordByAdmin()`: 管理员重置密码
  - `toggleUserStatus()`: 切换用户状态
  - `deleteUser()`: 删除用户

#### `MeetingRoomDAO.java` (21KB, 516行)
- **功能**: 会议室数据访问对象
- **主要方法**:
  - `getAllRooms()`: 获取所有会议室
  - `getRoomById()`: 根据ID获取会议室
  - `addRoom()`: 添加会议室
  - `updateRoom()`: 更新会议室信息
  - `deleteRoom()`: 删除会议室
  - `getRoomsByType()`: 根据类型获取会议室
  - `getAvailableRooms()`: 获取可用会议室

#### `ReservationDAO.java` (9.1KB, 211行)
- **功能**: 预订数据访问对象
- **主要方法**:
  - `createReservation()`: 创建预订
  - `getUserReservations()`: 获取用户预订
  - `getRoomReservations()`: 获取会议室预订
  - `updateReservation()`: 更新预订
  - `cancelReservation()`: 取消预订
  - `checkTimeConflict()`: 检查时间冲突

#### `RoomTypeDAO.java` (7.2KB, 205行)
- **功能**: 会议室类型数据访问对象
- **主要方法**:
  - `getAllRoomTypes()`: 获取所有会议室类型
  - `addRoomType()`: 添加会议室类型
  - `updateRoomType()`: 更新会议室类型
  - `deleteRoomType()`: 删除会议室类型

#### `PermissionMappingDAO.java` (9.4KB, 235行)
- **功能**: 权限映射数据访问对象
- **主要方法**:
  - `getAllPermissions()`: 获取所有权限映射
  - `getPermissionsByRole()`: 根据角色获取权限
  - `addPermission()`: 添加权限映射
  - `updatePermission()`: 更新权限映射
  - `deletePermission()`: 删除权限映射

#### `EquipmentDAO.java` (5.0KB, 120行)
- **功能**: 设备数据访问对象
- **主要方法**:
  - `getAllEquipment()`: 获取所有设备
  - `getEquipmentByRoom()`: 根据会议室获取设备
  - `addEquipment()`: 添加设备
  - `updateEquipment()`: 更新设备信息
  - `deleteEquipment()`: 删除设备

### 4. 用户界面层 (View)

#### 主要功能面板

##### `HomePanel.java` (9.6KB, 225行)
- **功能**: 系统主页面板
- **主要特性**:
  - 欢迎信息显示
  - 快速操作入口
  - 系统状态概览
  - 最近预订显示

##### `RoomStatusPanel.java` (21KB, 457行)
- **功能**: 会议室状态和预订面板
- **主要特性**:
  - 会议室列表显示
  - 实时状态更新
  - 预订功能集成
  - 权限控制显示

##### `MyBookingsPanel.java` (17KB, 361行)
- **功能**: 我的预订管理面板
- **主要特性**:
  - 个人预订列表
  - 预订详情查看
  - 预订修改/取消
  - 预订状态跟踪

##### `UserProfilePanel.java` (9.5KB, 215行)
- **功能**: 用户个人信息面板
- **主要特性**:
  - 个人信息显示
  - 资料修改功能
  - 密码修改
  - 账户状态显示

#### 管理员功能面板

##### `AdminSettingsPanel.java` (25KB, 582行)
- **功能**: 系统设置面板（管理员专用）
- **主要特性**:
  - 用户管理
  - 会议室管理
  - 设备管理
  - 权限管理
  - 系统配置

##### `AdminUserManagementPanel.java` (21KB, 497行)
- **功能**: 用户管理面板
- **主要特性**:
  - 用户列表显示
  - 用户添加/编辑/删除
  - 角色分配
  - 账户状态管理

##### `AdminRoomManagementPanel.java` (15KB, 378行)
- **功能**: 会议室管理面板
- **主要特性**:
  - 会议室列表管理
  - 会议室添加/编辑/删除
  - 会议室类型管理
  - 状态管理

##### `AdminEquipmentManagementPanel.java` (18KB, 429行)
- **功能**: 设备管理面板
- **主要特性**:
  - 设备列表管理
  - 设备添加/编辑/删除
  - 设备分配管理
  - 设备状态跟踪

#### 对话框组件

##### `ReservationDialog.java` (14KB, 307行)
- **功能**: 预订对话框
- **主要特性**:
  - 预订信息输入
  - 时间选择器
  - 会议室选择
  - 冲突检测

##### `PermissionEditDialog.java` (10KB, 241行)
- **功能**: 权限编辑对话框
- **主要特性**:
  - 权限配置界面
  - 角色权限设置
  - 权限验证

##### `RoomTypeEditDialog.java` (6.6KB, 170行)
- **功能**: 会议室类型编辑对话框
- **主要特性**:
  - 类型信息编辑
  - 类型代码管理
  - 类型描述设置

### 5. 工具类

#### `UIStyleUtil.java` (5.9KB, 206行)
- **功能**: UI样式工具类
- **主要特性**:
  - 主题管理（支持多种FlatLaf主题）
  - 全局字体设置
  - 组件样式美化
  - 主题切换功能
- **核心方法**:
  - `applyTheme()`: 应用指定主题
  - `setGlobalFont()`: 设置全局字体
  - `initializeUI()`: 初始化UI样式
  - `beautifyButton()`: 美化按钮样式

## 系统架构特点

### 1. 分层架构
- **表示层**: Swing UI组件
- **业务逻辑层**: 各种Panel和Dialog类
- **数据访问层**: DAO类
- **数据模型层**: 实体类

### 2. 权限控制
- 基于角色的访问控制（RBAC）
- 细粒度的会议室类型权限
- 动态权限验证

### 3. 数据库设计
- 使用MySQL数据库
- 规范的数据库连接管理
- 事务处理和异常处理

### 4. UI设计
- 现代化FlatLaf主题
- 响应式布局
- 统一的视觉风格
- 良好的用户体验

## 技术栈

- **开发语言**: Java 8+
- **UI框架**: Java Swing
- **布局管理器**: MigLayout
- **主题框架**: FlatLaf
- **数据库**: MySQL
- **数据库驱动**: MySQL Connector/J 8.0.33

## 文件统计

- **总文件数**: 25个
- **总代码行数**: 约4,500行
- **最大文件**: AdminSettingsPanel.java (582行)
- **最小文件**: Equipment.java (87行)

## 开发建议

1. **代码组织**: 文件按功能模块分类清晰，便于维护
2. **命名规范**: 类名和方法名遵循Java命名规范
3. **注释完整**: 大部分类和方法都有详细的中文注释
4. **异常处理**: 数据库操作有完善的异常处理机制
5. **UI一致性**: 使用统一的样式工具类确保UI一致性 