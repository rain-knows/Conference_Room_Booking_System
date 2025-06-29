# 会议室预约管理系统界面设计文档

## 1. 文档概述

本文档描述了基于简化后数据库结构的会议室预约管理系统的界面设计。界面设计遵循直观、易用的原则，旨在为不同角色的用户提供高效、清晰的操作体验。系统采用基于字段映射的权限管理机制，通过用户角色和会议室类型的组合来控制访问权限。

## 2. 用户界面架构

系统界面基于用户角色和核心功能进行设计，主要包括以下部分：

- **登录界面**：所有用户的统一入口。
- **主控制台**：一个集成的仪表板，根据用户角色（普通员工、领导、系统管理员）展示不同的功能和视图。
- **个人中心**：允许用户管理个人信息和查看自己的预约。
- **权限管理界面**：系统管理员可以配置用户角色与会议室类型的权限映射关系。

### 主要功能模块界面
1.  **用户管理模块** (仅系统管理员可见)
2.  **会议室管理模块** (系统管理员可编辑，其他用户可查看)
3.  **预约管理模块** (所有用户可用，但权限不同)
4.  **权限管理模块** (仅系统管理员可见)
5.  **会议室类型管理模块** (仅系统管理员可见)

## 3. 界面设计详情

### 3.1 登录界面

![登录界面示意图](.\image\img.png)

**界面元素**：
- 系统标题
- 用户名输入框
- 密码输入框
- 登录按钮

**数据关联**：
- `User` 表：用于验证用户凭据。

**功能描述**：
- 用户输入用户名和密码进行登录。
- 成功登录后，根据 `User` 表中的 `role` 字段跳转到相应的主控制台。
- 登录失败则显示错误提示。

### 3.2 主控制台

**界面元素**：
- 顶部导航栏：显示当前登录的用户名和登出按钮。
- 侧边菜单：根据用户角色动态显示功能模块（如"用户管理"、"会议室管理"、"我的预约"、"系统设置"）。
- 内容区域：展示当前选定功能模块的详细内容。

**功能描述**：
- **系统管理员**：可以看到所有管理模块，包括用户管理、会议室管理、权限管理和所有预约。
- **领导**：可以看到会议室和预约管理，可以预约所有类型的会议室。
- **普通员工**：可以看到会议室和预约管理，但可能在预约高级会议室时受限。

### 3.3 用户管理模块 (仅系统管理员)

#### 3.3.1 用户列表与编辑
**界面元素**：
- 用户搜索框。
- 用户列表表格，列出：`userId`, `username`, `fullName`, `email`, `phone`, `role`, `active`。
- "添加用户"按钮。
- 每行用户旁有"编辑"和"激活/禁用"按钮。

**功能描述**：
- 管理员可以查看、搜索所有用户。
- 点击"添加用户"或"编辑"会弹出一个表单，用于创建或修改用户信息，包括分配角色 (`role`)。

### 3.4 会议室管理模块

#### 3.4.1 会议室列表界面
**界面元素**：
- 会议室列表，以卡片或表格形式展示，包含：`name`, `location`, `capacity`, `status`, `description`, `roomType`。
- "添加会议室"、"编辑"、"删除"按钮 (仅管理员可见)。
- 按会议室类型 (`RoomType`) 筛选的选项。

**数据关联**：
- `MeetingRoom` 表：提供会议室基本信息。
- `RoomType` 表：用于筛选和分类。

#### 3.4.2 会议室详情界面
**界面元素**：
- 会议室的详细信息。
- 该会议室内的设备列表 (`Equipment` 表)。
- 一个显示该会议室未来预约情况的日历视图。
- "添加设备"按钮 (仅管理员可见)。

### 3.5 预约管理模块

#### 3.5.1 预约日历/列表视图
**界面元素**：
- 一个日历（或列表），显示所有会议室的预约情况。
- 按会议室、日期进行筛选的功能。
- "新建预约"按钮。
- 点击某个预约可查看详情，包括会议主题、描述和参与者。

**数据关联**：
- `Reservation` 表：提供预约信息。
- `MeetingRoom` 表：提供会议室信息。

#### 3.5.2 新建/编辑预约界面
**界面元素**：
- 会议主题和描述输入框。
- 会议室选择下拉菜单（根据用户权限过滤显示）。
- 开始和结束时间选择器。
- 参与者选择器（从 `User` 表中搜索并添加）。
- "提交"按钮。

**功能描述**：
- 用户填写预约信息并提交。
- 系统会检查预约时间是否冲突。
- 根据用户权限动态显示可预订的会议室。
- `LEADER` 角色的用户可以预约所有会议室，`NORMAL_EMPLOYEE` 可能会被限制预约某些 `RoomType` 的会议室。

### 3.6 权限管理模块 (仅系统管理员)

#### 3.6.1 权限映射管理界面
**界面元素**：
- 权限映射表格，显示用户角色与会议室类型的权限配置。
- 表格列：用户角色、会议室类型、可查看、可预订、可管理、描述。
- "添加权限映射"、"编辑"、"删除"按钮。
- 批量权限设置功能。

**数据关联**：
- `PermissionMapping` 表：存储权限映射关系。
- `User` 表：提供用户角色信息。
- `RoomType` 表：提供会议室类型信息。

**功能描述**：
- 系统管理员可以配置用户角色对会议室类型的权限。
- 支持查看、预订、管理三种权限类型。
- 可以批量设置权限，提高配置效率。

#### 3.6.2 权限编辑对话框
**界面元素**：
- 用户角色选择下拉框。
- 会议室类型选择下拉框。
- 权限复选框：可查看、可预订、可管理。
- 权限描述输入框。
- "保存"和"取消"按钮。

**功能描述**：
- 提供直观的权限配置界面。
- 实时预览权限配置效果。
- 支持权限配置的验证和提示。

### 3.7 会议室类型管理模块 (仅系统管理员)

#### 3.7.1 会议室类型列表界面
**界面元素**：
- 会议室类型列表表格，显示：类型ID、类型名称、类型代码、描述、创建时间。
- "添加类型"、"编辑"、"删除"按钮。
- 类型搜索和筛选功能。

**数据关联**：
- `RoomType` 表：提供会议室类型信息。

**功能描述**：
- 系统管理员可以管理会议室类型。
- 支持添加、修改、删除会议室类型。
- 类型代码用于权限映射和程序内部识别。

#### 3.7.2 会议室类型编辑对话框
**界面元素**：
- 类型名称输入框。
- 类型代码输入框（唯一标识符）。
- 类型描述输入框。
- "保存"和"取消"按钮。

**功能描述**：
- 提供会议室类型的创建和编辑功能。
- 验证类型代码的唯一性。
- 支持类型信息的完整管理。

## 4. 界面交互流程

### 4.1 会议室预约流程

```
用户登录 -> 进入主控制台 -> 打开 "预约管理" -> 点击 "新建预约" -> 
选择会议室和时间（根据权限过滤） -> 填写会议详情 -> 提交预约 -> 预约成功
```

### 4.2 管理员添加新会议室流程

```
管理员登录 -> 进入 "会议室管理" -> 点击 "添加会议室" -> 
填写会议室信息 (名称、位置、容量、类型等) -> 提交 -> 
进入新会议室详情页 -> 点击 "添加设备" -> 添加设备信息 -> 完成
```

### 4.3 权限配置流程

```
系统管理员登录 -> 进入 "系统设置" -> 选择 "权限管理" -> 
查看当前权限映射 -> 点击 "添加权限映射" -> 
选择用户角色和会议室类型 -> 配置权限选项 -> 保存配置
```

### 4.4 会议室类型管理流程

```
系统管理员登录 -> 进入 "系统设置" -> 选择 "会议室类型管理" -> 
查看当前类型列表 -> 点击 "添加类型" -> 
填写类型信息 -> 保存 -> 完成类型创建
```

## 5. 权限控制界面设计

### 5.1 动态权限显示

- **会议室列表**：根据用户权限只显示可查看的会议室。
- **预订按钮**：根据用户权限和会议室状态动态启用/禁用。
- **管理按钮**：根据用户权限显示或隐藏管理功能。
- **导航菜单**：根据用户角色动态显示可访问的功能模块。

### 5.2 权限反馈机制

- **权限提示**：当用户尝试访问无权限的功能时，显示友好的提示信息。
- **状态指示**：通过颜色、图标等方式直观显示权限状态。
- **操作确认**：在关键操作前进行权限验证和确认。

## 6. 结论

本界面设计文档基于简化的数据库结构，提供了一套核心、高效的界面方案。它实现了基于字段映射的权限管理机制，通过用户角色和会议室类型的组合来控制访问权限，使系统专注于用户、会议室、预约和权限四大核心功能的管理，旨在提供清晰、直观的用户体验。
