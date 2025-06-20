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
        String[] columnNames = { "ID", "用户名", "角色", "邮箱", "电话" };
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
        JButton[] btns = { addButton, editButton, deleteButton, resetPasswordButton };
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

        buttonPanel.add(addButton);
        buttonPanel.add(editButton, "gaptop 10");
        buttonPanel.add(deleteButton, "gaptop 10");
        buttonPanel.add(resetPasswordButton, "gaptop 10");
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

        return new User(userId, username, role, email, phone);
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
                        row.add(user.getRole());
                        row.add(user.getEmail());
                        row.add(user.getPhone());
                        tableModel.addRow(row);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(AdminUserManagementPanel.this, "加载用户列表失败: " + e.getMessage(), "错误",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
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

    // Inner dialog for adding/editing a user
    class UserEditDialog extends JDialog {
        private JTextField usernameField, emailField, phoneField, passwordField;
        private JComboBox<String> roleComboBox;
        private boolean isNewUser;
        private boolean saved = false;
        private User currentUser;

        public UserEditDialog(Frame owner, User user) {
            super(owner, true);
            this.currentUser = user;
            this.isNewUser = (user == null);
            setTitle(isNewUser ? "添加新用户" : "编辑用户");

            setLayout(new MigLayout("wrap 2, fillx", "[100px][grow,fill]"));

            usernameField = new JTextField();
            emailField = new JTextField();
            phoneField = new JTextField();
            roleComboBox = new JComboBox<>(new String[] { "user", "admin" });

            if (!isNewUser) {
                usernameField.setText(currentUser.getUsername());
                emailField.setText(currentUser.getEmail());
                phoneField.setText(currentUser.getPhone());
                roleComboBox.setSelectedItem(currentUser.getRole());
            }

            add(new JLabel("用户名:"));
            add(usernameField, "growx");
            add(new JLabel("邮箱:"));
            add(emailField, "growx");
            add(new JLabel("电话:"));
            add(phoneField, "growx");
            add(new JLabel("角色:"));
            add(roleComboBox, "growx");

            if (isNewUser) {
                passwordField = new JPasswordField();
                add(new JLabel("初始密码:"));
                add(passwordField, "growx");
            }

            JButton saveButton = new JButton("保存");
            saveButton.addActionListener(e -> save());
            add(saveButton, "span, split 2, align right, gaptop 10");

            JButton cancelButton = new JButton("取消");
            cancelButton.addActionListener(e -> dispose());
            add(cancelButton);

            pack();
            setLocationRelativeTo(owner);
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

            String password = isNewUser ? new String(((JPasswordField) passwordField).getPassword()) : null;
            if (isNewUser && (password == null || password.trim().isEmpty())) {
                JOptionPane.showMessageDialog(this, "必须为新用户设置初始密码。", "输入错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String role = (String) roleComboBox.getSelectedItem();

            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    if (isNewUser) {
                        User newUser = new User(0, username, role, email, phone); // ID is dummy
                        return userDAO.addUser(newUser, password);
                    } else {
                        User updatedUser = new User(currentUser.getUserId(), username, role, email, phone);
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