# 会议室预订管理系统

## 项目简介

本项目是一个基于 Java Swing + MySQL 的桌面级会议室预订与管理系统，支持多角色权限、会议室与设备管理、预约冲突检测等功能，界面美观、交互友好，适用于企业内部会议资源管理。

---

## 主要功能

- **用户登录与权限分级**  
  - 普通员工：可预约会议室、管理个人预约、查看会议室状态、修改个人信息  
  - 管理员：拥有员工全部权限，并可管理会议室、设备、用户、系统设置

- **会议室管理**  
  - 增删改查会议室信息，支持状态筛选（可用、维护中、已停用）
  - 会议室描述、容量、位置、状态等信息维护

- **设备管理**  
  - 管理会议室内设备，支持设备状态筛选（正常、损坏、维修中、已报废）

- **预约管理**  
  - 预约会议室，自动检测时间冲突
  - 查看、修改、取消个人预约

- **会议室状态总览**  
  - 一览所有会议室当前状态，支持状态筛选，直观展示会议占用情况

- **用户管理与个人信息**  
  - 管理员可增删改查用户，重置密码
  - 用户可修改个人邮箱、电话、密码

- **系统设置**  
  - 管理员可修改自身密码等系统参数

---

## 技术栈

- **后端/核心语言**：Java 8+
- **桌面界面**：Java Swing（MigLayout，JFormDesigner）
- **数据库**：MySQL 5.7+
- **依赖管理**：lib 目录下 jar 包（含 mysql-connector、miglayout 等）

---

## 目录结构

```
Conference_Room_Booking_System/
├── docs/                        # 设计文档和数据库脚本
│   ├── complete_database_creation.sql   # 数据库建表及初始化脚本
│   ├── database_design.md              # 数据库结构设计说明
│   ├── database_documentation.md        # 数据库详细文档
│   ├── interface_design.md              # 系统界面设计说明
│   ├── logical_structure.md             # 系统逻辑结构说明
│   ├── role_permission_design.md        # 角色与权限设计说明
│   ├── shuju.sql                        # 额外数据脚本
│   ├── system_design_v2.md              # 系统架构与流程设计
│   └── image/                           # 设计相关图片
│       └── img.png
├── lib/                         # 依赖库（需全部加入项目依赖）
│   ├── intellij_forms_rt_src.zip         # IntelliJ表单运行时源码
│   ├── intellij_forms_rt.jar             # IntelliJ表单运行时库
│   ├── miglayout-core.jar                # MigLayout核心库
│   ├── miglayout-swing.jar               # MigLayout Swing扩展
│   ├── miglayout-core-javadoc.jar        # MigLayout核心库文档
│   ├── miglayout-core-sources.jar        # MigLayout核心源码
│   ├── miglayout-swing-javadoc.jar       # MigLayout Swing文档
│   ├── miglayout-swing-sources.jar       # MigLayout Swing源码
│   └── mysql-connector-j-8.0.33.jar      # MySQL数据库驱动
├── src/                         # Java 源代码
│   ├── AdminEquipmentManagementPanel.java    # 设备管理界面
│   ├── AdminRoomManagementPanel.java        # 会议室管理界面
│   ├── AdminSettingsPanel.java              # 系统设置界面
│   ├── AdminUserManagementPanel.java        # 用户管理界面
│   ├── Equipment.java                       # 设备实体类
│   ├── EquipmentDAO.java                    # 设备数据访问对象
│   ├── HomePanel.java                       # 首页面板
│   ├── LoginForm.java                       # 登录界面及主程序入口
│   ├── LoginForm.jfd                        # 登录界面表单设计文件
│   ├── MainPage.java                        # 主界面（导航+内容区）
│   ├── MeetingRoom.java                     # 会议室实体类
│   ├── MeetingRoomDAO.java                  # 会议室数据访问对象
│   ├── MigLayoutDemo.java                   # MigLayout布局演示
│   ├── MyBookingsPanel.java                 # 我的预订界面
│   ├── Reservation.java                     # 预约实体类
│   ├── ReservationDAO.java                  # 预约数据访问对象
│   ├── ReservationDialog.java               # 预约对话框
│   ├── RoomStatusPanel.java                 # 会议室状态总览界面
│   ├── User.java                            # 用户实体类
│   ├── UserDAO.java                         # 用户数据访问对象
│   ├── UserProfilePanel.java                # 个人信息界面
│   └── UIStyleUtil.java                     # 界面美化工具类
├── jform.iml                    # IntelliJ IDEA 模块文件
└── README.md                    # 项目说明文档
```

---

## 安装与运行

### 1. 环境准备

- JDK 8 及以上
- MySQL 5.7 及以上
- 推荐开发工具：IntelliJ IDEA

### 2. 数据库初始化

1. 启动 MySQL，创建数据库（如 `conference_room_booking`）
2. 执行 `docs/complete_database_creation.sql` 初始化表结构和部分数据  
   ```bash
   mysql -u your_username -p conference_room_booking < docs/complete_database_creation.sql
   ```

### 3. 配置数据库连接

- 打开 `src/UserDAO.java` 等 DAO 文件，修改如下参数为你的实际数据库信息：
  ```java
  private static final String URL = "jdbc:mysql://localhost:3306/conference_room_booking";
  private static final String USER = "your_mysql_user";
  private static final String PASSWORD = "your_mysql_password";
  ```

### 4. 添加依赖库

- 确保 `lib/` 下所有 jar 包已添加为项目依赖（右键 Add as Library）

### 5. 启动项目

- 以 `LoginForm.java` 为主程序入口，右键运行 `main` 方法即可启动登录界面

---

## 设计文档

- [数据库设计](docs/database_design.md)
- [界面设计](docs/interface_design.md)
- [角色权限设计](docs/role_permission_design.md)
- [系统架构与逻辑](docs/system_design_v2.md)
- [数据库脚本](docs/complete_database_creation.sql)

---

## 常见问题

- **无法连接数据库**：请检查数据库地址、用户名、密码是否正确，MySQL 服务是否启动
- **界面乱码**：请确保操作系统和 IDEA 字体设置为支持中文的字体
- **依赖缺失**：请确认 lib 目录下所有 jar 包已添加为依赖

---

## 联系与贡献

如有建议或 bug 反馈，欢迎 issue 或 pull request！

---

如需英文版或更详细的开发文档，请参考 `docs/` 目录下相关文件。
