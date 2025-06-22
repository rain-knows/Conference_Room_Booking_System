import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import net.miginfocom.swing.MigLayout;

/**
 * 会议室类型编辑对话框
 * 用于添加和编辑会议室类型
 */
public class RoomTypeEditDialog extends JDialog {
    private final JPanel parentPanel;
    private final RoomType originalRoomType;
    private final RoomTypeDAO roomTypeDAO;

    // UI组件
    private JTextField typeNameField;
    private JTextField typeCodeField;
    private JTextArea descriptionTextArea;
    private JButton saveButton;
    private JButton cancelButton;

    private boolean confirmed = false;

    public RoomTypeEditDialog(JPanel parent, RoomType roomType) {
        super((Frame) SwingUtilities.getWindowAncestor(parent),
                roomType == null ? "添加会议室类型" : "编辑会议室类型", true);

        this.parentPanel = parent;
        this.originalRoomType = roomType;
        this.roomTypeDAO = new RoomTypeDAO();

        initComponents();
        if (roomType != null) {
            populateFields(roomType);
        }

        setSize(540, 500);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new MigLayout("fill, insets 25", "[130px][grow,fill]", "[][][][grow][]"));

        // 标题
        JLabel titleLabel = new JLabel(originalRoomType == null ? "添加会议室类型" : "编辑会议室类型");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        add(titleLabel, "span 2, center, wrap, gapbottom 25");

        // 类型名称
        JLabel nameLabel = new JLabel("类型名称:");
        nameLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        typeNameField = new JTextField();
        typeNameField.setPreferredSize(new Dimension(300, 32));
        typeNameField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        add(nameLabel);
        add(typeNameField, "growx, wrap, gaptop 5, gapbottom 15");

        // 类型代码
        JLabel codeLabel = new JLabel("类型代码:");
        codeLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        typeCodeField = new JTextField();
        typeCodeField.setPreferredSize(new Dimension(300, 32));
        typeCodeField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        add(codeLabel);
        add(typeCodeField, "growx, wrap, gaptop 5, gapbottom 15");

        // 描述
        JLabel descLabel = new JLabel("描述:");
        descLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        add(descLabel, "wrap, gaptop 5");

        descriptionTextArea = new JTextArea(6, 35);
        descriptionTextArea.setLineWrap(true);
        descriptionTextArea.setWrapStyleWord(true);
        descriptionTextArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(descriptionTextArea);
        scrollPane.setPreferredSize(new Dimension(350, 120));
        add(scrollPane, "span 2, grow, wrap, gapbottom 25");

        // 按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));

        saveButton = new JButton("保存");
        saveButton.setPreferredSize(new Dimension(100, 35));
        saveButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        saveButton.setFocusPainted(false);

        cancelButton = new JButton("取消");
        cancelButton.setPreferredSize(new Dimension(100, 35));
        cancelButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        cancelButton.setFocusPainted(false);

        saveButton.addActionListener(e -> saveRoomType());
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        add(buttonPanel, "span 2, center, gaptop 10");
    }

    private void populateFields(RoomType roomType) {
        typeNameField.setText(roomType.getTypeName());
        typeCodeField.setText(roomType.getTypeCode());
        descriptionTextArea.setText(roomType.getDescription());
    }

    private void saveRoomType() {
        // 验证输入
        String typeName = typeNameField.getText().trim();
        String typeCode = typeCodeField.getText().trim();
        String description = descriptionTextArea.getText().trim();

        if (typeName.isEmpty() || typeCode.isEmpty()) {
            JOptionPane.showMessageDialog(this, "类型名称和类型代码不能为空。", "输入错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 创建会议室类型对象
        RoomType roomType;
        if (originalRoomType == null) {
            // 新增
            roomType = new RoomType(typeName, typeCode, description);
        } else {
            // 编辑
            roomType = new RoomType(
                    originalRoomType.getRoomTypeId(),
                    typeName, typeCode, description,
                    originalRoomType.getCreateTime(), null);
        }

        // 保存到数据库
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                if (originalRoomType == null) {
                    return roomTypeDAO.addRoomType(roomType);
                } else {
                    return roomTypeDAO.updateRoomType(roomType);
                }
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        JOptionPane.showMessageDialog(RoomTypeEditDialog.this,
                                originalRoomType == null ? "会议室类型添加成功。" : "会议室类型更新成功。",
                                "成功", JOptionPane.INFORMATION_MESSAGE);
                        confirmed = true;
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(RoomTypeEditDialog.this,
                                originalRoomType == null ? "会议室类型添加失败。" : "会议室类型更新失败。",
                                "错误", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(RoomTypeEditDialog.this,
                            "保存会议室类型时发生错误: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}