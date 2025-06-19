# 会议室预约管理系统

## 1. 项目简介

本项目是一个基于 Java Swing 和 MySQL 开发的桌面应用程序，旨在提供一个简洁、高效的会议室预约和管理平台。系统根据用户的角色（普通员工、领导、系统管理员）提供不同的操作权限，以满足不同用户的需求。

## 2. 核心功能

*   **用户认证**：通过登录界面验证用户身份。
*   **角色化权限管理**：
    *   **普通员工**：可以查询会议室、创建和管理自己的预约。
    *   **领导**：拥有普通员工的所有权限，并能预约所有类型的会议室。
    *   **系统管理员**：拥有最高权限，可管理用户、会议室及所有预约。
*   **会议室管理** (管理员权限)：
    *   添加、编辑和删除会议室信息。
    *   管理会议室内的设备。
*   **预约管理**：
    *   用户可以根据权限预约会议室。
    *   系统自动检查时间冲突。
    *   用户可以查看和管理自己的预约。

## 3. 技术栈

*   **后端语言**：Java
*   **用户界面**：Java Swing (使用 IntelliJ IDEA GUI Designer 和 MigLayout)
*   **数据库**：MySQL
*   **开发环境**：IntelliJ IDEA

## 4. 系统设计

项目的设计文档详细描述了系统的架构和实现细节，帮助您快速理解项目。

*   **数据库设计**: [docs/database_design.md](docs/database_design.md)
    *   描述了数据库的表结构、字段定义以及实体关系。
*   **界面设计**: [docs/interface_design.md](docs/interface_design.md)
    *   展示了系统主要界面的布局和功能描述。
*   **角色权限设计**: [docs/role_permission_design.md](docs/role_permission_design.md)
    *   定义了系统中的用户角色及其对应的操作权限。

## 5. 安装与运行

### 5.1 前提条件

1.  **Java Development Kit (JDK)**：版本 8 或更高。
2.  **MySQL 数据库**：版本 5.7 或更高。
3.  **IDE (推荐)**：IntelliJ IDEA。

### 5.2 数据库设置

1.  启动您的 MySQL 服务器。
2.  创建一个新的数据库（例如 `conference_room_booking`）。
3.  执行 `docs/complete_database_creation.sql` 脚本来创建所需的表和初始数据。
    ```sql
    -- 示例：使用 mysql 命令行工具导入
    mysql -u your_username -p conference_room_booking < docs/complete_database_creation.sql
    ```
    请将 `your_username` 替换为您的实际 MySQL 用户名。

### 5.3 项目配置

1.  使用 IntelliJ IDEA 打开项目。
2.  确保 `lib` 目录下的 `mysql-connector-j-8.0.33.jar` 已被添加为项目库。
3.  在 `src` 目录下的数据访问类（如 `UserDAO.java`）中，根据您的环境修改数据库连接参数（URL、用户名、密码）。

### 5.4 运行项目

1.  在 IntelliJ IDEA 中，找到 `src` 目录下的 `LoginForm.java` 文件。
2.  右键点击该文件并选择 "Run 'LoginForm.main()'"。
3.  系统登录界面将会启动，您可以使用在数据库脚本中预设的账户登录。

## 6. 项目结构

```
Conference_Room_Booking_System/
├── docs/                   # 设计文档和数据库脚本
│   ├── complete_database_creation.sql
│   ├── database_design.md
│   ├── interface_design.md
│   └── role_permission_design.md
├── lib/                    # 项目依赖库
│   └── mysql-connector-j-8.0.33.jar
├── src/                    # Java 源代码
│   ├── LoginForm.java      # 登录界面及主程序入口
│   └── UserDAO.java        # 数据访问对象示例
└── jform.iml               # IntelliJ IDEA 模块文件
```
