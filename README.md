# 会议室预订管理系统

## 项目简介

本项目是一个基于 Java Swing 和 MySQL 开发的桌面应用程序，旨在提供一个简单易用的会议室预订和管理平台。用户可以通过该系统查看会议室状态、预订会议室、管理自己的预订等。系统管理员可以管理会议室信息、用户信息和所有预订记录。

## 主要功能

* **用户认证：**
    * 用户登录
    * （可能包含用户注册，根据 `UserDAO.java` 和数据库设计推断）
* **会议室浏览：**
    * 查看所有会议室列表及其当前状态（可用/已预订）。
    * 查看会议室详细信息（如容量、设备等）。
* **会议室预订：**
    * 用户选择日期和时间段预订可用会议室。
    * 查看自己的预订记录。
    * 取消自己的预订。
* **用户管理（管理员）：**
    * 添加、编辑、删除用户信息。
    * 管理用户角色和权限。
* **会议室管理（管理员）：**
    * 添加、编辑、删除会议室信息。
* **预订管理（管理员）：**
    * 查看所有用户的预订记录。
    * （可能包含批准/拒绝预订的功能）

## 技术栈

* **后端语言：** Java
* **用户界面：** Java Swing (使用 IntelliJ IDEA GUI Designer, MigLayout, GridLayoutManager)
* **数据库：** MySQL (使用 `mysql-connector-j-8.0.33.jar`)
* **开发环境：** IntelliJ IDEA

## 系统设计文档

项目包含详细的设计文档，帮助理解系统架构和实现细节：

* **数据库设计：** [docs/database_design.md](docs/database_design.md)
    * 描述了数据库中的表结构、字段定义以及表之间的关系。
* **界面设计：** [docs/interface_design.md](docs/interface_design.md)
    * 展示了系统主要界面的原型或描述。
* **角色权限设计：** [docs/role_permission_design.md](docs/role_permission_design.md)
    * 定义了系统中的不同用户角色及其对应的操作权限。

## 安装与运行

### 前提条件

1.  **Java Development Kit (JDK)：** 版本 8 或更高。
2.  **MySQL数据库：** 版本 5.7 或更高 (推荐 8.0)。
3.  **IDE (可选但推荐)：** IntelliJ IDEA。
4.  **MySQL Connector/J：** `mysql-connector-j-8.0.33.jar` (已包含在 `lib` 目录下，或需要手动添加到项目库中)。

### 数据库设置

1.  启动您的 MySQL 服务器。
2.  创建一个新的数据库，例如 `conference_booking_db`。
3.  执行 `docs/complete_database_creation.sql` 文件中的 SQL 脚本来创建所需的表和初始数据。
    ```sql
    -- 示例：如何使用 mysql 命令行工具导入
    -- mysql -u your_username -p your_database_name < docs/complete_database_creation.sql
    ```
    请确保将 `your_username` 和 `your_database_name` 替换为您的实际 MySQL 用户名和数据库名。

### 项目配置

1.  **克隆或下载项目：**
    ```bash
    # 如果项目在版本控制中
    # git clone <repository_url>
    # cd Conference_Room_Booking_System
    ```
2.  **IDE 配置 (IntelliJ IDEA):**
    * 打开 IntelliJ IDEA，选择 "Open" 并导航到项目根目录。
    * 确保 `lib` 目录下的 `mysql-connector-j-8.0.33.jar` 已被识别为项目库。如果未识别，可以右键点击 jar 文件选择 "Add as Library..."。
    * **数据库连接配置：** 在 `UserDAO.java` (或其他数据库连接相关类) 中，检查并修改数据库连接参数，以匹配您的 MySQL 配置：
        ```java
        // 示例 UserDAO.java 中的连接部分 (具体代码可能不同)
        // private static final String DB_URL = "jdbc:mysql://localhost:3306/your_database_name";
        // private static final String DB_USER = "your_username";
        // private static final String DB_PASSWORD = "your_password";
        ```
        请将 `your_database_name`, `your_username`, `your_password` 替换为您的实际配置。

### 运行项目

1.  在 IntelliJ IDEA 中，找到 `src` 目录下的 `LoginForm.java` 文件。
2.  右键点击 `LoginForm.java` 并选择 "Run 'LoginForm.main()'"。
3.  系统登录界面将会启动。

## 项目结构


Conference_Room_Booking_System/
├── .idea/                  # IntelliJ IDEA 项目配置文件
├── docs/                   # 设计文档和数据库脚本
│   ├── complete_database_creation.sql  # 完整的数据库创建脚本
│   ├── database_design.md            # 数据库设计文档
│   ├── interface_design.md           # 界面设计文档
│   └── role_permission_design.md     # 角色权限设计文档
├── lib/                    # 项目依赖库 (注意：此列表中的 .xml 文件为 IntelliJ IDEA 库配置文件，实际的 .jar 文件需确保已正确配置或存放于此目录)
│   ├── IntelliJ_IDEA_GridLayout.xml # IntelliJ IDEA 项目库配置文件，定义了 GUI 设计器 GridLayoutManager 所需的运行时库 (通常是 IDEA 自带的 forms_rt.jar 或类似名称的库)
│   ├── MigLayout.xml                # IntelliJ IDEA 项目库配置文件，定义了 MigLayout 布局管理器库 (通常指向 miglayout-swing.jar，需确保该 JAR 文件实际存在于项目中或由 IDE 管理)
│   ├── mysql_connector_j_8_0_33.xml # MySQL JDBC驱动定义 (指向 mysql-connector-j-8.0.33.jar)
│   ├── intellij_forms_rt_src.zip    # IntelliJ IDEA GUI 设计器运行时库的源码压缩包，主要用于查阅参考，并非直接编译或运行所必需的依赖
│   └── mysql-connector-j-8.0.33.jar # MySQL JDBC 驱动 (实际的jar文件应在此或由 .xml 文件引用)
├── src/                    # Java 源代码
│   ├── LoginForm.java      # 用户登录界面及主程序入口
│   ├── UserDAO.java        # 用户数据访问对象，处理用户相关的数据库操作
│   └── ...                 # 其他核心 Java 类或包 (例如：会议室管理界面 AdminDashboard.java、预订处理逻辑 BookingManager.java、工具类 Utils.java 等，建议具体列出主要的包名或类名以增强项目结构清晰度)
└── Conference_Room_Booking_System.iml # IntelliJ IDEA 模块文件


## 使用说明

1.  启动应用程序后，将显示登录界面。
2.  输入您的用户名和密码进行登录。
    * 默认管理员账户和密码（如果已在 `complete_database_creation.sql` 中设置）。
3.  根据您的用户角色，您将看到不同的操作界面和功能。
    * **普通用户：** 可以浏览会议室、创建新预订、查看和管理自己的预订。
    * **管理员：** 除了普通用户功能外，还可以管理用户信息、会议室信息和所有预订记录。

## 贡献指南

如果您想为本项目做出贡献，请遵循以下步骤：

1.  Fork 本仓库。
2.  创建一个新的分支 (`git checkout -b feature/your-feature-name`)。
3.  提交您的更改 (`git commit -am 'Add some feature'`)。
4.  将您的分支推送到远程仓库 (`git push origin feature/your-feature-name`)。
5.  创建一个新的 Pull Request。

## 许可证

本项目可以采用 [MIT许可证](LICENSE) (请根据实际情况添加许可证文件和链接)。
