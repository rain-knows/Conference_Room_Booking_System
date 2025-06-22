import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

/**
 * 管理员用户管理面板，用于管理员管理用户账户。
 */
public class AdminUserManagementPanel extends JPanel {

    private JTable userTable;
    private DefaultTableModel tableModel;
    private UserDAO userDAO;
    private User currentAdmin;

    // 定义系统中支持的用户角色
    private static final String[] USER_ROLES = {
            "NORMAL_EMPLOYEE", // 普通员工
            "LEADER", // 领导
            "SYSTEM_ADMIN" // 系统管理员
    };

    /**
     * 构造函数，初始化管理员用户管理面板。
     */
    public AdminUserManagementPanel(User admin) {
        this.currentAdmin = admin;
        this.userDAO = new UserDAO();
        initComponents();
        loadUsers();
    }

    private void initComponents() {
        setLayout(new MigLayout("fill, insets 20", "[grow][]", "[][grow]"));

        JLabel titleLabel = new JLabel("用户管理");
        UIStyleUtil.beautifyTitleLabel(titleLabel);
        add(titleLabel, "span, wrap, gapbottom 15");

        // Table
        String[] columnNames = { "ID", "用户名", "角色", "邮箱", "电话", "状态" };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        userTable = new JTable(tableModel);
        userTable.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        userTable.setRowHeight(28);
        userTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 16));
        userTable.getTableHeader().setBackground(new Color(230, 235, 245));
        userTable.setSelectionBackground(new Color(204, 229, 255));
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(userTable), "grow, push");

        // Button Panel
        JPanel buttonPanel = new JPanel(new MigLayout("wrap 1", "[grow, fill]"));
        JButton addButton = new JButton("添加用户");
        JButton editButton = new JButton("编辑用户");
        JButton deleteButton = new JButton("删除用户");
        JButton resetPasswordButton = new JButton("重置密码");
        JButton toggleStatusButton = new JButton("启用/禁用");
        JButton[] btns = { addButton, editButton, deleteButton, resetPasswordButton, toggleStatusButton };
        for (JButton btn : btns) {
            UIStyleUtil.beautifyButton(btn);
        }
        addButton.addActionListener(e -> openEditDialog(null));
        editButton.addActionListener(e -> {
            User selectedUser = getSelectedUser();
            if (selectedUser != null) {
                openEditDialog(selectedUser);
            } else {
                JOptionPane.showMessageDialog(this, "请先选择一个用户进行编辑。", "提示", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        deleteButton.addActionListener(e -> deleteSelectedUser());
        resetPasswordButton.addActionListener(e -> resetSelectedUserPassword());
        toggleStatusButton.addActionListener(e -> toggleSelectedUserStatus());

        buttonPanel.add(addButton);
        buttonPanel.add(editButton, "gaptop 10");
        buttonPanel.add(deleteButton, "gaptop 10");
        buttonPanel.add(resetPasswordButton, "gaptop 10");
        buttonPanel.add(toggleStatusButton, "gaptop 10");
        add(buttonPanel, "top");

        UIStyleUtil.setMainBackground(this);
    }

    private User getSelectedUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1)
            return null;

        int userId = (int) tableModel.getValueAt(selectedRow, 0);
        String username = (String) tableModel.getValueAt(selectedRow, 1);
        String role = (String) tableModel.getValueAt(selectedRow, 2);
        String email = (String) tableModel.getValueAt(selectedRow, 3);
        String phone = (String) tableModel.getValueAt(selectedRow, 4);
        boolean active = "激活".equals(tableModel.getValueAt(selectedRow, 5));

        return new User(userId, username, role, email, phone, active);
    }

    private void loadUsers() {
        new SwingWorker<List<User>, Void>() {
            @Override
            protected List<User> doInBackground() throws Exception {
                // Exclude the current admin from the list to prevent self-modification
                return userDAO.getAllUsers(currentAdmin.getUserId());
            }

            @Override
            protected void done() {
                try {
                    List<User> users = get();
                    tableModel.setRowCount(0);
                    for (User user : users) {
                        Vector<Object> row = new Vector<>();
                        row.add(user.getUserId());
                        row.add(user.getUsername());
                        row.add(getRoleDisplayName(user.getRole()));
                        row.add(user.getEmail());
                        row.add(user.getPhone());
                        row.add(user.isActive() ? "激活" : "未激活");
                        tableModel.addRow(row);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(AdminUserManagementPanel.this, "加载用户列表失败: " + e.getMessage(), "错误",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    /**
     * 获取角色的显示名称
     */
    private String getRoleDisplayName(String role) {
        switch (role) {
            case "NORMAL_EMPLOYEE":
                return "普通员工";
            case "LEADER":
                return "领导";
            case "SYSTEM_ADMIN":
                return "系统管理员";
            default:
                return role;
        }
    }

    /**
     * 根据显示名称获取角色代码
     */
    private String getRoleCode(String displayName) {
        switch (displayName) {
            case "普通员工":
                return "NORMAL_EMPLOYEE";
            case "领导":
                return "LEADER";
            case "系统管理员":
                return "SYSTEM_ADMIN";
            default:
                return displayName;
        }
    }

    private void openEditDialog(User user) {
        UserEditDialog dialog = new UserEditDialog((JFrame) SwingUtilities.getWindowAncestor(this), user);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadUsers(); // Refresh table
        }
    }

    private void deleteSelectedUser() {
        User selectedUser = getSelectedUser();
        if (selectedUser == null) {
            JOptionPane.showMessageDialog(this, "请选择要删除的用户。", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirmation = JOptionPane.showConfirmDialog(this,
                "确定要删除用户 \"" + selectedUser.getUsername() + "\"?\n此操作不可撤销。", "确认删除",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirmation != JOptionPane.YES_OPTION)
            return;

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                // Note: Deleting a user might fail if they have existing reservations due to
                // foreign key constraints.
                // A real-world app should handle this more gracefully.
                return userDAO.deleteUser(selectedUser.getUserId());
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(AdminUserManagementPanel.this, "用户删除成功。", "成功",
                                JOptionPane.INFORMATION_MESSAGE);
                        loadUsers();
                    } else {
                        JOptionPane.showMessageDialog(AdminUserManagementPanel.this, "删除失败。", "错误",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(AdminUserManagementPanel.this,
                            "删除失败，可能是因为该用户尚有关联的预订记录。\n错误: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void resetSelectedUserPassword() {
        User selectedUser = getSelectedUser();
        if (selectedUser == null) {
            JOptionPane.showMessageDialog(this, "请选择要重置密码的用户。", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String newPassword = "password123"; // Default password
        int confirmation = JOptionPane.showConfirmDialog(this,
                "确定要将用户 \"" + selectedUser.getUsername() + "\" 的密码重置为 \"" + newPassword + "\"?\n请告知用户新密码。",
                "确认重置密码", JOptionPane.YES_NO_OPTION);
        if (confirmation != JOptionPane.YES_OPTION)
            return;

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return userDAO.resetPasswordByAdmin(selectedUser.getUserId(), newPassword);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(AdminUserManagementPanel.this, "密码已成功重置。", "成功",
                                JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(AdminUserManagementPanel.this, "密码重置失败。", "错误",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(AdminUserManagementPanel.this, "密码重置失败: " + e.getMessage(), "错误",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void toggleSelectedUserStatus() {
        User selectedUser = getSelectedUser();
        if (selectedUser == null) {
            JOptionPane.showMessageDialog(this, "请选择要更改状态的用户。", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String action = selectedUser.isActive() ? "禁用" : "启用";
        int confirmation = JOptionPane.showConfirmDialog(this,
                "确定要" + action + "用户 \"" + selectedUser.getUsername() + "\"?", "确认" + action,
                JOptionPane.YES_NO_OPTION);
        if (confirmation != JOptionPane.YES_OPTION)
            return;

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return userDAO.toggleUserStatus(selectedUser.getUserId(), !selectedUser.isActive());
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(AdminUserManagementPanel.this, "用户状态已成功更改。", "成功",
                                JOptionPane.INFORMATION_MESSAGE);
                        loadUsers();
                    } else {
                        JOptionPane.showMessageDialog(AdminUserManagementPanel.this, "状态更改失败。", "错误",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(AdminUserManagementPanel.this, "状态更改失败: " + e.getMessage(), "错误",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    // Inner dialog for adding/editing a user
    class UserEditDialog extends JDialog {
        private JTextField usernameField, emailField, phoneField;
        private JPasswordField passwordField;
        private JComboBox<String> roleComboBox;
        private JCheckBox activeCheckBox;
        private boolean isNewUser;
        private boolean saved = false;
        private User currentUser;

        public UserEditDialog(Frame owner, User user) {
            super(owner, true);
            this.currentUser = user;
            this.isNewUser = (user == null);
            setTitle(isNewUser ? "添加新用户" : "编辑用户");

            setLayout(new MigLayout("fill, insets 25", "[120px][grow,fill]", "[][][][][][grow][]"));

            // 设置对话框样式
            getContentPane().setBackground(new Color(248, 249, 250));

            // 标题
            JLabel titleLabel = new JLabel(isNewUser ? "添加新用户" : "编辑用户");
            titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
            titleLabel.setForeground(new Color(52, 73, 94));
            add(titleLabel, "span 2, center, wrap, gapbottom 25");

            // 初始化组件
            initFields();
            populateFields();

            // 布局组件
            layoutComponents();

            pack();
            setMinimumSize(new Dimension(450, 400));
            setResizable(true);
            setLocationRelativeTo(owner);
        }

        private void initFields() {
            usernameField = new JTextField();
            usernameField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            usernameField.setPreferredSize(new Dimension(250, 32));
            usernameField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10)));

            emailField = new JTextField();
            emailField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            emailField.setPreferredSize(new Dimension(250, 32));
            emailField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10)));

            phoneField = new JTextField();
            phoneField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            phoneField.setPreferredSize(new Dimension(250, 32));
            phoneField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10)));

            passwordField = new JPasswordField();
            passwordField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            passwordField.setPreferredSize(new Dimension(250, 32));
            passwordField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10)));

            // 使用显示名称的角色选项
            String[] roleDisplayNames = { "普通员工", "领导", "系统管理员" };
            roleComboBox = new JComboBox<>(roleDisplayNames);
            roleComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            roleComboBox.setPreferredSize(new Dimension(250, 32));

            activeCheckBox = new JCheckBox("账户激活", true);
            activeCheckBox.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            activeCheckBox.setForeground(new Color(52, 73, 94));
        }

        private void populateFields() {
            if (!isNewUser) {
                usernameField.setText(currentUser.getUsername());
                emailField.setText(currentUser.getEmail());
                phoneField.setText(currentUser.getPhone());
                roleComboBox.setSelectedItem(getRoleDisplayName(currentUser.getRole()));
                activeCheckBox.setSelected(currentUser.isActive());
            }
        }

        private void layoutComponents() {
            // 用户名
            JLabel usernameLabel = new JLabel("用户名:");
            usernameLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
            usernameLabel.setForeground(new Color(52, 73, 94));
            add(usernameLabel);
            add(usernameField, "growx, wrap, gaptop 5, gapbottom 15");

            // 邮箱
            JLabel emailLabel = new JLabel("邮箱:");
            emailLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
            emailLabel.setForeground(new Color(52, 73, 94));
            add(emailLabel);
            add(emailField, "growx, wrap, gaptop 5, gapbottom 15");

            // 电话
            JLabel phoneLabel = new JLabel("电话:");
            phoneLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
            phoneLabel.setForeground(new Color(52, 73, 94));
            add(phoneLabel);
            add(phoneField, "growx, wrap, gaptop 5, gapbottom 15");

            // 角色
            JLabel roleLabel = new JLabel("角色:");
            roleLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
            roleLabel.setForeground(new Color(52, 73, 94));
            add(roleLabel);
            add(roleComboBox, "growx, wrap, gaptop 5, gapbottom 15");

            // 状态
            JLabel statusLabel = new JLabel("状态:");
            statusLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
            statusLabel.setForeground(new Color(52, 73, 94));
            add(statusLabel);
            add(activeCheckBox, "growx, wrap, gaptop 5, gapbottom 15");

            // 密码（仅新用户）
            if (isNewUser) {
                JLabel passwordLabel = new JLabel("初始密码:");
                passwordLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
                passwordLabel.setForeground(new Color(52, 73, 94));
                add(passwordLabel);
                add(passwordField, "growx, wrap, gaptop 5, gapbottom 20");
            } else {
                add(new JLabel(), "span 2, wrap, gapbottom 20");
            }

            // 按钮
            JButton saveButton = new JButton("保存");
            saveButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
            saveButton.setBackground(new Color(40, 167, 69));
            saveButton.setForeground(Color.WHITE);
            saveButton.setPreferredSize(new Dimension(100, 35));
            saveButton.setFocusPainted(false);
            saveButton.addActionListener(e -> save());

            JButton cancelButton = new JButton("取消");
            cancelButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
            cancelButton.setBackground(new Color(108, 117, 125));
            cancelButton.setForeground(Color.WHITE);
            cancelButton.setPreferredSize(new Dimension(100, 35));
            cancelButton.setFocusPainted(false);
            cancelButton.addActionListener(e -> dispose());

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
            buttonPanel.setOpaque(false);
            buttonPanel.add(saveButton);
            buttonPanel.add(cancelButton);
            add(buttonPanel, "span 2, center, gaptop 10");
        }

        public boolean isSaved() {
            return saved;
        }

        private void save() {
            String username = usernameField.getText().trim();
            if (username.isEmpty()) {
                JOptionPane.showMessageDialog(this, "用户名不能为空。", "输入错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String password = isNewUser ? new String(passwordField.getPassword()) : null;
            if (isNewUser && (password == null || password.trim().isEmpty())) {
                JOptionPane.showMessageDialog(this, "必须为新用户设置初始密码。", "输入错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String roleDisplayName = (String) roleComboBox.getSelectedItem();
            String role = getRoleCode(roleDisplayName);
            boolean active = activeCheckBox.isSelected();

            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    if (isNewUser) {
                        User newUser = new User(0, username, role, email, phone, active);
                        return userDAO.addUser(newUser, password);
                    } else {
                        User updatedUser = new User(currentUser.getUserId(), username, role, email, phone, active);
                        return userDAO.updateUserByAdmin(updatedUser);
                    }
                }

                @Override
                protected void done() {
                    try {
                        if (get()) {
                            saved = true;
                            dispose();
                        } else {
                            JOptionPane.showMessageDialog(UserEditDialog.this, "保存失败，可能是用户名已存在。", "错误",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(UserEditDialog.this, "保存时发生错误: " + e.getMessage(), "数据库错误",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        }
    }
}