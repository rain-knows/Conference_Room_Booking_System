import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.ArrayList;

/**
 * 管理员设置面板，用于管理员配置系统设置。
 */
public class AdminSettingsPanel extends JPanel {

    private final User currentUser;
    private final UserDAO userDAO;
    private final PermissionMappingDAO permissionMappingDAO;
    private final RoomTypeDAO roomTypeDAO;

    // 密码管理组件
    private JPasswordField oldPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private JButton savePasswordButton;

    // 权限管理组件
    private JTabbedPane tabbedPane;
    private JTable permissionTable;
    private JTable roomTypeTable;
    private JButton addPermissionButton;
    private JButton editPermissionButton;
    private JButton deletePermissionButton;
    private JButton addRoomTypeButton;
    private JButton editRoomTypeButton;
    private JButton deleteRoomTypeButton;
    private JButton initializeDefaultsButton;

    /**
     * 构造函数，初始化管理员系统设置面板。
     */
    public AdminSettingsPanel(User user) {
        this.currentUser = user;
        this.userDAO = new UserDAO();
        this.permissionMappingDAO = new PermissionMappingDAO();
        this.roomTypeDAO = new RoomTypeDAO();
        initComponents();
        loadData();
    }

    private void initComponents() {
        setLayout(new MigLayout("fill, insets 20", "[grow]", "[grow]"));

        // 创建选项卡面板
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        // 密码管理选项卡
        tabbedPane.addTab("密码管理", createPasswordManagementPanel());

        // 新增：会议室管理、设备管理、用户管理
        tabbedPane.addTab("会议室管理", new AdminRoomManagementPanel());
        tabbedPane.addTab("设备管理", new AdminEquipmentManagementPanel());
        tabbedPane.addTab("用户管理", new AdminUserManagementPanel(currentUser));

        // 权限管理选项卡
        tabbedPane.addTab("权限管理", createPermissionManagementPanel());

        // 会议室类型管理选项卡
        tabbedPane.addTab("会议室类型", createRoomTypeManagementPanel());

        // 主题管理选项卡
        tabbedPane.addTab("主题设置", createThemeManagementPanel());

        add(tabbedPane, "grow");
    }

    private JPanel createPasswordManagementPanel() {
        JPanel panel = new JPanel(new MigLayout("wrap 2, fillx, insets 20", "[120px][grow,fill]"));

        JLabel titleLabel = new JLabel("更改管理员密码");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        panel.add(titleLabel, "span 2, wrap, gapbottom 20");

        JLabel userLabel = new JLabel("当前用户名:");
        userLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JLabel usernameLabel = new JLabel(currentUser.getUsername());
        usernameLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        panel.add(userLabel);
        panel.add(usernameLabel, "wrap, gapbottom 10");

        oldPasswordField = new JPasswordField();
        newPasswordField = new JPasswordField();
        confirmPasswordField = new JPasswordField();

        oldPasswordField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        newPasswordField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        confirmPasswordField.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        JLabel oldPwdLabel = new JLabel("旧密码:");
        oldPwdLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        panel.add(oldPwdLabel);
        panel.add(oldPasswordField, "growx, wrap");

        JLabel newPwdLabel = new JLabel("新密码:");
        newPwdLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        panel.add(newPwdLabel);
        panel.add(newPasswordField, "growx, wrap");

        JLabel confirmPwdLabel = new JLabel("确认新密码:");
        confirmPwdLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        panel.add(confirmPwdLabel);
        panel.add(confirmPasswordField, "growx, wrap, gapbottom 20");

        savePasswordButton = new JButton("保存更改");
        savePasswordButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        savePasswordButton.addActionListener(e -> savePasswordChanges());
        panel.add(savePasswordButton, "span 2, align right");

        return panel;
    }

    private JPanel createPermissionManagementPanel() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 20", "[grow]", "[][grow][]"));

        // 标题
        JLabel titleLabel = new JLabel("权限映射管理");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        panel.add(titleLabel, "wrap, gapbottom 20");

        // 权限表格
        String[] columnNames = { "ID", "用户角色", "会议室类型", "可预订", "可查看", "可管理", "描述", "创建时间" };
        permissionTable = new JTable(new javax.swing.table.DefaultTableModel(columnNames, 0));
        permissionTable.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        permissionTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 14));
        permissionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(permissionTable);
        panel.add(scrollPane, "grow, wrap, gapbottom 10");

        // 按钮面板
        JPanel buttonPanel = new JPanel(new MigLayout("", "[][][][]", "[]"));
        buttonPanel.setOpaque(false);

        addPermissionButton = new JButton("添加权限");
        editPermissionButton = new JButton("编辑权限");
        deletePermissionButton = new JButton("删除权限");
        initializeDefaultsButton = new JButton("初始化默认权限");

        JButton[] buttons = { addPermissionButton, editPermissionButton, deletePermissionButton,
                initializeDefaultsButton };
        for (JButton btn : buttons) {
            btn.setFont(new Font("微软雅黑", Font.BOLD, 14));
        }

        addPermissionButton.addActionListener(e -> addPermission());
        editPermissionButton.addActionListener(e -> editPermission());
        deletePermissionButton.addActionListener(e -> deletePermission());
        initializeDefaultsButton.addActionListener(e -> initializeDefaultPermissions());

        buttonPanel.add(addPermissionButton);
        buttonPanel.add(editPermissionButton);
        buttonPanel.add(deletePermissionButton);
        buttonPanel.add(initializeDefaultsButton);

        panel.add(buttonPanel, "growx");

        return panel;
    }

    private JPanel createRoomTypeManagementPanel() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 20", "[grow]", "[][grow][]"));

        // 标题
        JLabel titleLabel = new JLabel("会议室类型管理");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        panel.add(titleLabel, "wrap, gapbottom 20");

        // 会议室类型表格
        String[] columnNames = { "ID", "类型名称", "类型代码", "描述", "创建时间", "更新时间" };
        roomTypeTable = new JTable(new javax.swing.table.DefaultTableModel(columnNames, 0));
        roomTypeTable.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        roomTypeTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 14));
        roomTypeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(roomTypeTable);
        panel.add(scrollPane, "grow, wrap, gapbottom 10");

        // 按钮面板
        JPanel buttonPanel = new JPanel(new MigLayout("", "[][][]", "[]"));
        buttonPanel.setOpaque(false);

        addRoomTypeButton = new JButton("添加类型");
        editRoomTypeButton = new JButton("编辑类型");
        deleteRoomTypeButton = new JButton("删除类型");

        JButton[] buttons = { addRoomTypeButton, editRoomTypeButton, deleteRoomTypeButton };
        for (JButton btn : buttons) {
            btn.setFont(new Font("微软雅黑", Font.BOLD, 14));
        }

        addRoomTypeButton.addActionListener(e -> addRoomType());
        editRoomTypeButton.addActionListener(e -> editRoomType());
        deleteRoomTypeButton.addActionListener(e -> deleteRoomType());

        buttonPanel.add(addRoomTypeButton);
        buttonPanel.add(editRoomTypeButton);
        buttonPanel.add(deleteRoomTypeButton);

        panel.add(buttonPanel, "growx");

        return panel;
    }

    private JPanel createThemeManagementPanel() {
        JPanel panel = new JPanel(new MigLayout("wrap 2, fillx, insets 20", "[120px][grow,fill]"));

        JLabel titleLabel = new JLabel("主题设置");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        panel.add(titleLabel, "span 2, wrap, gapbottom 20");

        JLabel currentThemeLabel = new JLabel("当前主题:");
        currentThemeLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        panel.add(currentThemeLabel);

        JLabel currentThemeValue = new JLabel(UIStyleUtil.getCurrentTheme().getDisplayName());
        currentThemeValue.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        panel.add(currentThemeValue, "wrap, gapbottom 20");

        JLabel themeLabel = new JLabel("选择主题:");
        themeLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        panel.add(themeLabel);

        JComboBox<UIStyleUtil.ThemeType> themeComboBox = new JComboBox<>(UIStyleUtil.ThemeType.values());
        themeComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        themeComboBox.setSelectedItem(UIStyleUtil.getCurrentTheme());
        themeComboBox.addActionListener(e -> {
            UIStyleUtil.ThemeType selectedTheme = (UIStyleUtil.ThemeType) themeComboBox.getSelectedItem();
            if (selectedTheme != null) {
                UIStyleUtil.applyTheme(selectedTheme);
                currentThemeValue.setText(selectedTheme.getDisplayName());
            }
        });
        panel.add(themeComboBox, "growx, wrap");

        return panel;
    }

    private void loadData() {
        // 异步加载数据
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                loadPermissionData();
                loadRoomTypeData();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (InterruptedException | ExecutionException e) {
                    JOptionPane.showMessageDialog(AdminSettingsPanel.this,
                            "加载数据失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void loadPermissionData() throws SQLException {
        List<PermissionMapping> permissions = permissionMappingDAO.getAllPermissionMappings();
        String[] columnNames = { "ID", "用户角色", "会议室类型", "可预订", "可查看", "可管理", "描述", "创建时间" };
        Object[][] data = new Object[permissions.size()][columnNames.length];

        for (int i = 0; i < permissions.size(); i++) {
            PermissionMapping perm = permissions.get(i);
            data[i][0] = perm.getMappingId();
            data[i][1] = perm.getUserRole();
            data[i][2] = perm.getRoomTypeCode();
            data[i][3] = perm.canBook() ? "是" : "否";
            data[i][4] = perm.canView() ? "是" : "否";
            data[i][5] = perm.canManage() ? "是" : "否";
            data[i][6] = perm.getDescription();
            data[i][7] = perm.getCreateTime();
        }

        SwingUtilities.invokeLater(() -> {
            permissionTable.setModel(new javax.swing.table.DefaultTableModel(data, columnNames));
        });
    }

    private void loadRoomTypeData() throws SQLException {
        List<RoomType> roomTypes = roomTypeDAO.getAllRoomTypes();
        String[] columnNames = { "ID", "类型名称", "类型代码", "描述", "创建时间", "更新时间" };
        Object[][] data = new Object[roomTypes.size()][columnNames.length];

        for (int i = 0; i < roomTypes.size(); i++) {
            RoomType roomType = roomTypes.get(i);
            data[i][0] = roomType.getRoomTypeId();
            data[i][1] = roomType.getTypeName();
            data[i][2] = roomType.getTypeCode();
            data[i][3] = roomType.getDescription();
            data[i][4] = roomType.getCreateTime();
            data[i][5] = roomType.getUpdateTime();
        }

        SwingUtilities.invokeLater(() -> {
            roomTypeTable.setModel(new javax.swing.table.DefaultTableModel(data, columnNames));
        });
    }

    private void savePasswordChanges() {
        String oldPassword = new String(oldPasswordField.getPassword());
        String newPassword = new String(newPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        // Validation
        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "所有密码字段都必须填写。", "输入错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "新密码和确认密码不匹配。", "输入错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (oldPassword.equals(newPassword)) {
            JOptionPane.showMessageDialog(this, "新密码不能与旧密码相同。", "输入错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Disable button to prevent multiple clicks
        savePasswordButton.setEnabled(false);

        new SwingWorker<Boolean, Void>() {
            private String errorMessage = null;

            @Override
            protected Boolean doInBackground() {
                try {
                    return userDAO.changePassword(currentUser.getUserId(), oldPassword, newPassword);
                } catch (Exception e) {
                    errorMessage = e.getMessage();
                    return false;
                }
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        JOptionPane.showMessageDialog(AdminSettingsPanel.this,
                                "密码修改成功！请在下次登录时使用新密码。",
                                "成功", JOptionPane.INFORMATION_MESSAGE);
                        // Clear fields
                        oldPasswordField.setText("");
                        newPasswordField.setText("");
                        confirmPasswordField.setText("");
                    } else {
                        String message = "密码修改失败。";
                        if (errorMessage != null) {
                            message += "\n原因: " + errorMessage;
                        } else {
                            // This case implies old password was incorrect
                            message += "\n请确认您的旧密码是否正确。";
                        }
                        JOptionPane.showMessageDialog(AdminSettingsPanel.this, message, "操作失败",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(AdminSettingsPanel.this,
                            "处理密码修改时发生未知错误: " + e.getMessage(), "严重错误", JOptionPane.ERROR_MESSAGE);
                } finally {
                    // Re-enable button
                    savePasswordButton.setEnabled(true);
                }
            }
        }.execute();
    }

    private void addPermission() {
        // 创建权限编辑对话框
        PermissionEditDialog dialog = new PermissionEditDialog(this, null);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            loadData(); // 重新加载数据
        }
    }

    private void editPermission() {
        int selectedRow = permissionTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请选择要编辑的权限映射。", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int mappingId = (Integer) permissionTable.getValueAt(selectedRow, 0);
        try {
            List<PermissionMapping> permissions = permissionMappingDAO.getAllPermissionMappings();
            PermissionMapping selectedPermission = null;
            for (PermissionMapping perm : permissions) {
                if (perm.getMappingId() == mappingId) {
                    selectedPermission = perm;
                    break;
                }
            }

            if (selectedPermission != null) {
                PermissionEditDialog dialog = new PermissionEditDialog(this, selectedPermission);
                dialog.setVisible(true);
                if (dialog.isConfirmed()) {
                    loadData(); // 重新加载数据
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "获取权限信息失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deletePermission() {
        int selectedRow = permissionTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请选择要删除的权限映射。", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int mappingId = (Integer) permissionTable.getValueAt(selectedRow, 0);
        int result = JOptionPane.showConfirmDialog(this,
                "确定要删除这个权限映射吗？", "确认删除", JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return permissionMappingDAO.deletePermissionMapping(mappingId);
                }

                @Override
                protected void done() {
                    try {
                        boolean success = get();
                        if (success) {
                            JOptionPane.showMessageDialog(AdminSettingsPanel.this,
                                    "权限映射删除成功。", "成功", JOptionPane.INFORMATION_MESSAGE);
                            loadData(); // 重新加载数据
                        } else {
                            JOptionPane.showMessageDialog(AdminSettingsPanel.this,
                                    "权限映射删除失败。", "错误", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(AdminSettingsPanel.this,
                                "删除权限映射时发生错误: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        }
    }

    private void initializeDefaultPermissions() {
        int result = JOptionPane.showConfirmDialog(this,
                "确定要初始化默认权限映射吗？这将覆盖现有的权限设置。", "确认初始化", JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    permissionMappingDAO.initializeDefaultPermissions();
                    roomTypeDAO.initializeDefaultRoomTypes();
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        JOptionPane.showMessageDialog(AdminSettingsPanel.this,
                                "默认权限映射初始化成功。", "成功", JOptionPane.INFORMATION_MESSAGE);
                        loadData(); // 重新加载数据
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(AdminSettingsPanel.this,
                                "初始化默认权限映射时发生错误: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        }
    }

    private void addRoomType() {
        RoomTypeEditDialog dialog = new RoomTypeEditDialog(this, null);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            loadData(); // 重新加载数据
        }
    }

    private void editRoomType() {
        int selectedRow = roomTypeTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请选择要编辑的会议室类型。", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int roomTypeId = (Integer) roomTypeTable.getValueAt(selectedRow, 0);
        try {
            RoomType selectedRoomType = roomTypeDAO.getRoomTypeById(roomTypeId);
            if (selectedRoomType != null) {
                RoomTypeEditDialog dialog = new RoomTypeEditDialog(this, selectedRoomType);
                dialog.setVisible(true);
                if (dialog.isConfirmed()) {
                    loadData(); // 重新加载数据
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "获取会议室类型信息失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteRoomType() {
        int selectedRow = roomTypeTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请选择要删除的会议室类型。", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int roomTypeId = (Integer) roomTypeTable.getValueAt(selectedRow, 0);
        int result = JOptionPane.showConfirmDialog(this,
                "确定要删除这个会议室类型吗？", "确认删除", JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return roomTypeDAO.deleteRoomType(roomTypeId);
                }

                @Override
                protected void done() {
                    try {
                        boolean success = get();
                        if (success) {
                            JOptionPane.showMessageDialog(AdminSettingsPanel.this,
                                    "会议室类型删除成功。", "成功", JOptionPane.INFORMATION_MESSAGE);
                            loadData(); // 重新加载数据
                        } else {
                            JOptionPane.showMessageDialog(AdminSettingsPanel.this,
                                    "会议室类型删除失败。", "错误", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(AdminSettingsPanel.this,
                                "删除会议室类型时发生错误: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        }
    }
}