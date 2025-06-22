import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import net.miginfocom.swing.MigLayout;

/**
 * 权限编辑对话框
 * 用于添加和编辑权限映射
 */
public class PermissionEditDialog extends JDialog {
    private final JPanel parentPanel;
    private final PermissionMapping originalMapping;
    private final PermissionMappingDAO permissionMappingDAO;
    private final RoomTypeDAO roomTypeDAO;

    // UI组件
    private JComboBox<String> userRoleComboBox;
    private JComboBox<String> roomTypeComboBox;
    private JCheckBox canBookCheckBox;
    private JCheckBox canViewCheckBox;
    private JCheckBox canManageCheckBox;
    private JTextArea descriptionTextArea;
    private JButton saveButton;
    private JButton cancelButton;

    private boolean confirmed = false;

    public PermissionEditDialog(JPanel parent, PermissionMapping mapping) {
        super((Frame) SwingUtilities.getWindowAncestor(parent),
                mapping == null ? "添加权限映射" : "编辑权限映射", true);

        this.parentPanel = parent;
        this.originalMapping = mapping;
        this.permissionMappingDAO = new PermissionMappingDAO();
        this.roomTypeDAO = new RoomTypeDAO();

        initComponents();
        loadData();
        if (mapping != null) {
            populateFields(mapping);
        }

        setSize(640, 660);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new MigLayout("fill, insets 25", "[140px][grow,fill]", "[][][][][][][][grow][]"));

        // 设置对话框样式
        getContentPane().setBackground(new Color(248, 249, 250));

        // 标题
        JLabel titleLabel = new JLabel(originalMapping == null ? "添加权限映射" : "编辑权限映射");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        titleLabel.setForeground(new Color(52, 73, 94));
        add(titleLabel, "span 2, center, wrap, gapbottom 25");

        // 用户角色选择
        JLabel roleLabel = new JLabel("用户角色:");
        roleLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        roleLabel.setForeground(new Color(52, 73, 94));
        userRoleComboBox = new JComboBox<>(new String[] { "NORMAL_EMPLOYEE", "LEADER", "SYSTEM_ADMIN" });
        userRoleComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        userRoleComboBox.setPreferredSize(new Dimension(200, 32));
        add(roleLabel);
        add(userRoleComboBox, "growx, wrap, gaptop 5, gapbottom 15");

        // 会议室类型选择
        JLabel roomTypeLabel = new JLabel("会议室类型:");
        roomTypeLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        roomTypeLabel.setForeground(new Color(52, 73, 94));
        roomTypeComboBox = new JComboBox<>();
        roomTypeComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        roomTypeComboBox.setPreferredSize(new Dimension(200, 32));
        add(roomTypeLabel);
        add(roomTypeComboBox, "growx, wrap, gaptop 5, gapbottom 15");

        // 权限复选框
        JLabel permissionsLabel = new JLabel("权限设置:");
        permissionsLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        permissionsLabel.setForeground(new Color(52, 73, 94));
        add(permissionsLabel, "wrap, gaptop 5");

        // 权限复选框面板
        JPanel permissionsPanel = new JPanel(new MigLayout("wrap 1, fillx", "[grow]", "[][][]"));
        permissionsPanel.setOpaque(false);
        permissionsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)));

        canViewCheckBox = new JCheckBox("可查看");
        canViewCheckBox.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        canViewCheckBox.setForeground(new Color(52, 73, 94));
        permissionsPanel.add(canViewCheckBox, "wrap, gaptop 3");

        canBookCheckBox = new JCheckBox("可预订");
        canBookCheckBox.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        canBookCheckBox.setForeground(new Color(52, 73, 94));
        permissionsPanel.add(canBookCheckBox, "wrap, gaptop 3");

        canManageCheckBox = new JCheckBox("可管理");
        canManageCheckBox.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        canManageCheckBox.setForeground(new Color(52, 73, 94));
        permissionsPanel.add(canManageCheckBox, "wrap");

        add(permissionsPanel, "span 2, growx, wrap, gapbottom 15");

        // 描述
        JLabel descLabel = new JLabel("描述:");
        descLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        descLabel.setForeground(new Color(52, 73, 94));
        add(descLabel, "wrap, gaptop 5");

        descriptionTextArea = new JTextArea(4, 35);
        descriptionTextArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        descriptionTextArea.setLineWrap(true);
        descriptionTextArea.setWrapStyleWord(true);
        descriptionTextArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        JScrollPane scrollPane = new JScrollPane(descriptionTextArea);
        scrollPane.setPreferredSize(new Dimension(400, 100));
        add(scrollPane, "span 2, grow, wrap, gapbottom 25");

        // 按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);

        saveButton = new JButton("保存");
        saveButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        saveButton.setBackground(new Color(40, 167, 69));
        saveButton.setForeground(Color.WHITE);
        saveButton.setPreferredSize(new Dimension(100, 35));
        saveButton.setFocusPainted(false);

        cancelButton = new JButton("取消");
        cancelButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        cancelButton.setBackground(new Color(108, 117, 125));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setPreferredSize(new Dimension(100, 35));
        cancelButton.setFocusPainted(false);

        saveButton.addActionListener(e -> savePermission());
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        add(buttonPanel, "span 2, center, gaptop 10");

        // 设置默认值
        canViewCheckBox.setSelected(true);
        canBookCheckBox.setSelected(true);
        canManageCheckBox.setSelected(false);
    }

    private void loadData() {
        // 异步加载会议室类型数据
        new SwingWorker<List<RoomType>, Void>() {
            @Override
            protected List<RoomType> doInBackground() throws Exception {
                return roomTypeDAO.getAllRoomTypes();
            }

            @Override
            protected void done() {
                try {
                    List<RoomType> roomTypes = get();
                    SwingUtilities.invokeLater(() -> {
                        roomTypeComboBox.removeAllItems();
                        for (RoomType roomType : roomTypes) {
                            roomTypeComboBox.addItem(roomType.getTypeCode());
                        }
                    });
                } catch (InterruptedException | ExecutionException e) {
                    JOptionPane.showMessageDialog(PermissionEditDialog.this,
                            "加载会议室类型失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void populateFields(PermissionMapping mapping) {
        userRoleComboBox.setSelectedItem(mapping.getUserRole());
        roomTypeComboBox.setSelectedItem(mapping.getRoomTypeCode());
        canViewCheckBox.setSelected(mapping.canView());
        canBookCheckBox.setSelected(mapping.canBook());
        canManageCheckBox.setSelected(mapping.canManage());
        descriptionTextArea.setText(mapping.getDescription());
    }

    private void savePermission() {
        // 验证输入
        if (userRoleComboBox.getSelectedItem() == null || roomTypeComboBox.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "请选择用户角色和会议室类型。", "输入错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String userRole = (String) userRoleComboBox.getSelectedItem();
        String roomTypeCode = (String) roomTypeComboBox.getSelectedItem();
        boolean canView = canViewCheckBox.isSelected();
        boolean canBook = canBookCheckBox.isSelected();
        boolean canManage = canManageCheckBox.isSelected();
        String description = descriptionTextArea.getText().trim();

        // 创建权限映射对象
        PermissionMapping mapping;
        if (originalMapping == null) {
            // 新增
            mapping = new PermissionMapping(userRole, roomTypeCode, canBook, canView, canManage, description);
        } else {
            // 编辑
            mapping = new PermissionMapping(
                    originalMapping.getMappingId(),
                    userRole, roomTypeCode, canBook, canView, canManage,
                    description, originalMapping.getCreateTime(), null);
        }

        // 保存到数据库
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                if (originalMapping == null) {
                    return permissionMappingDAO.addPermissionMapping(mapping);
                } else {
                    return permissionMappingDAO.updatePermissionMapping(mapping);
                }
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        JOptionPane.showMessageDialog(PermissionEditDialog.this,
                                originalMapping == null ? "权限映射添加成功。" : "权限映射更新成功。",
                                "成功", JOptionPane.INFORMATION_MESSAGE);
                        confirmed = true;
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(PermissionEditDialog.this,
                                originalMapping == null ? "权限映射添加失败。" : "权限映射更新失败。",
                                "错误", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(PermissionEditDialog.this,
                            "保存权限映射时发生错误: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}