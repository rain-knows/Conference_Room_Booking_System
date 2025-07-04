import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Vector;

/**
 * Admin panel for managing equipment.
 */
public class AdminEquipmentManagementPanel extends JPanel {

    private JTable equipmentTable;
    private DefaultTableModel tableModel;
    private EquipmentDAO equipmentDAO;
    private MeetingRoomDAO meetingRoomDAO;
    private List<Equipment> equipmentList;
    private JPopupMenu statusFilterMenu;
    private String currentStatusFilter = "全部";

    public AdminEquipmentManagementPanel() {
        this.equipmentDAO = new EquipmentDAO();
        this.meetingRoomDAO = new MeetingRoomDAO();
        initComponents();
        loadEquipment();
    }

    private void initComponents() {
        setLayout(new MigLayout("fill, insets 20", "[grow][]", "[][grow]"));

        JLabel titleLabel = new JLabel("设备管理");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        add(titleLabel, "span, wrap, gapbottom 15");

        // Table
        String[] columnNames = { "ID", "设备名称", "型号", "所属会议室", "状态", "购买日期" };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        equipmentTable = new JTable(tableModel);
        equipmentTable.setRowHeight(28);
        equipmentTable.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        equipmentTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 14));
        equipmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // 表头筛选弹出菜单
        statusFilterMenu = new JPopupMenu();
        String[] statusOptions = { "全部", "正常", "损坏", "维修中", "已报废" };
        for (String status : statusOptions) {
            JMenuItem item = new JMenuItem(status);
            item.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            item.addActionListener(e -> {
                currentStatusFilter = status;
                applyFilters();
            });
            statusFilterMenu.add(item);
        }

        JTableHeader header = equipmentTable.getTableHeader();
        header.setDefaultRenderer((table, value, isSelected, hasFocus, row, column) -> {
            JLabel lbl = new JLabel();
            lbl.setFont(header.getFont());
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setOpaque(true);
            lbl.setBackground(header.getBackground());
            if (column == 4) {
                lbl.setText("状态 ▼");
            } else {
                lbl.setText(value.toString());
            }
            return lbl;
        });

        header.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int col = equipmentTable.columnAtPoint(e.getPoint());
                if (col == 4) { // 状态列
                    statusFilterMenu.show(header, e.getX(), header.getHeight());
                }
            }
        });

        add(new JScrollPane(equipmentTable), "grow, push");

        // Button Panel
        JPanel buttonPanel = new JPanel(new MigLayout("wrap 1", "[grow, fill]"));
        JButton addButton = new JButton("添加设备");
        JButton editButton = new JButton("编辑选中项");
        JButton deleteButton = new JButton("删除选中项");

        JButton[] btns = { addButton, editButton, deleteButton };
        for (JButton btn : btns) {
            btn.setFont(new Font("微软雅黑", Font.BOLD, 14));
            btn.setFocusPainted(false);
        }

        addButton.addActionListener(e -> openEditDialog(null));
        editButton.addActionListener(e -> {
            int selectedRow = equipmentTable.getSelectedRow();
            if (selectedRow != -1) {
                Equipment selectedEquipment = equipmentList.get(selectedRow);
                openEditDialog(selectedEquipment);
            } else {
                JOptionPane.showMessageDialog(this, "请先选择一个设备进行编辑。", "提示", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        deleteButton.addActionListener(e -> deleteSelectedEquipment());

        buttonPanel.add(addButton);
        buttonPanel.add(editButton, "gaptop 10");
        buttonPanel.add(deleteButton, "gaptop 10");
        add(buttonPanel, "top");
    }

    private void loadEquipment() {
        new SwingWorker<List<Equipment>, Void>() {
            @Override
            protected List<Equipment> doInBackground() throws Exception {
                return equipmentDAO.getAllEquipment();
            }

            @Override
            protected void done() {
                try {
                    equipmentList = get();
                    tableModel.setRowCount(0);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    for (Equipment eq : equipmentList) {
                        Vector<Object> row = new Vector<>();
                        row.add(eq.getEquipmentId());
                        row.add(eq.getName());
                        row.add(eq.getModel());
                        row.add(eq.getRoomName() != null ? eq.getRoomName() : "未分配");
                        row.add(eq.getStatusText());
                        row.add(eq.getPurchaseDate() != null ? dateFormat.format(eq.getPurchaseDate()) : "");
                        tableModel.addRow(row);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(AdminEquipmentManagementPanel.this, "加载设备列表失败: " + e.getMessage(),
                            "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void openEditDialog(Equipment equipment) {
        // Fetch rooms in the background before opening the dialog
        new SwingWorker<List<MeetingRoom>, Void>() {
            @Override
            protected List<MeetingRoom> doInBackground() throws Exception {
                return meetingRoomDAO.getAllMeetingRooms();
            }

            @Override
            protected void done() {
                try {
                    List<MeetingRoom> rooms = get();
                    EquipmentEditDialog dialog = new EquipmentEditDialog(
                            (JFrame) SwingUtilities.getWindowAncestor(AdminEquipmentManagementPanel.this), equipment,
                            rooms);
                    dialog.setVisible(true);
                    if (dialog.isSaved()) {
                        loadEquipment(); // Refresh table
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(AdminEquipmentManagementPanel.this, "无法打开编辑窗口: " + e.getMessage(),
                            "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void deleteSelectedEquipment() {
        int selectedRow = equipmentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要删除的设备。", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirmation = JOptionPane.showConfirmDialog(this, "确定要删除选中的设备吗？", "确认删除", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (confirmation != JOptionPane.YES_OPTION)
            return;

        int equipmentId = (int) tableModel.getValueAt(selectedRow, 0);

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return equipmentDAO.deleteEquipment(equipmentId);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(AdminEquipmentManagementPanel.this, "设备删除成功。", "成功",
                                JOptionPane.INFORMATION_MESSAGE);
                        loadEquipment();
                    } else {
                        JOptionPane.showMessageDialog(AdminEquipmentManagementPanel.this, "删除失败。", "错误",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(AdminEquipmentManagementPanel.this, "删除失败: " + e.getMessage(), "错误",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void applyFilters() {
        if (equipmentList == null)
            return;
        List<Equipment> filtered;
        if (currentStatusFilter.equals("全部")) {
            filtered = equipmentList;
        } else {
            filtered = new Vector<>();
            for (Equipment eq : equipmentList) {
                if (eq.getStatusText().equals(currentStatusFilter)) {
                    filtered.add(eq);
                }
            }
        }
        tableModel.setRowCount(0);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        for (Equipment eq : filtered) {
            Vector<Object> row = new Vector<>();
            row.add(eq.getEquipmentId());
            row.add(eq.getName());
            row.add(eq.getModel());
            row.add(eq.getRoomName() != null ? eq.getRoomName() : "未分配");
            row.add(eq.getStatusText());
            row.add(eq.getPurchaseDate() != null ? dateFormat.format(eq.getPurchaseDate()) : "");
            tableModel.addRow(row);
        }
    }

    // Inner class for the Add/Edit dialog
    class EquipmentEditDialog extends JDialog {
        private JTextField nameField, modelField, dateField;
        private JComboBox<MeetingRoom> roomComboBox;
        private JComboBox<String> statusComboBox;
        private boolean saved = false;
        private Equipment currentEquipment;
        private final List<MeetingRoom> roomList;

        public EquipmentEditDialog(Frame owner, Equipment equipment, List<MeetingRoom> rooms) {
            super(owner, true);
            this.currentEquipment = equipment;
            this.roomList = rooms;
            setTitle(equipment == null ? "添加新设备" : "编辑设备");

            setLayout(new MigLayout("wrap 2, fillx", "[100px][grow,fill]"));

            nameField = new JTextField();
            nameField.setFont(new Font("微软雅黑", Font.PLAIN, 14));

            modelField = new JTextField();
            modelField.setFont(new Font("微软雅黑", Font.PLAIN, 14));

            dateField = new JTextField();
            dateField.setFont(new Font("微软雅黑", Font.PLAIN, 14));

            roomComboBox = new JComboBox<>(rooms.toArray(new MeetingRoom[0]));
            roomComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            roomComboBox.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                        boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof MeetingRoom) {
                        setText(((MeetingRoom) value).getName());
                    }
                    return this;
                }
            });

            statusComboBox = new JComboBox<>(new String[] { "正常", "损坏", "维修中", "已报废" });
            statusComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 14));

            if (equipment != null) {
                nameField.setText(equipment.getName());
                modelField.setText(equipment.getModel());
                dateField.setText(equipment.getPurchaseDate() != null
                        ? new SimpleDateFormat("yyyy-MM-dd").format(equipment.getPurchaseDate())
                        : "");
                statusComboBox.setSelectedItem(equipment.getStatusText());
                // Set room selection
                for (int i = 0; i < roomComboBox.getItemCount(); i++) {
                    MeetingRoom room = roomComboBox.getItemAt(i);
                    if (room.getRoomId() == equipment.getRoomId()) {
                        roomComboBox.setSelectedIndex(i);
                        break;
                    }
                }
            }

            JLabel nameLabel = new JLabel("设备名称:");
            nameLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            add(nameLabel);
            add(nameField, "growx");

            JLabel modelLabel = new JLabel("型号:");
            modelLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            add(modelLabel);
            add(modelField, "growx");

            JLabel roomLabel = new JLabel("所属会议室:");
            roomLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            add(roomLabel);
            add(roomComboBox, "growx");

            JLabel statusLabel = new JLabel("状态:");
            statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            add(statusLabel);
            add(statusComboBox, "growx");

            JLabel dateLabel = new JLabel("购买日期:");
            dateLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            add(dateLabel);
            add(dateField, "growx");

            JButton saveButton = new JButton("保存");
            saveButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
            saveButton.addActionListener(e -> save());
            add(saveButton, "span, split 2, align right");

            JButton cancelButton = new JButton("取消");
            cancelButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
            cancelButton.addActionListener(e -> dispose());
            add(cancelButton);

            pack();
            setLocationRelativeTo(owner);
        }

        public boolean isSaved() {
            return saved;
        }

        private void save() {
            // Basic validation
            if (nameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "设备名称不能为空。", "输入错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String name = nameField.getText().trim();
            String model = modelField.getText().trim();
            MeetingRoom selectedRoom = (MeetingRoom) roomComboBox.getSelectedItem();
            String statusText = (String) statusComboBox.getSelectedItem();

            // Convert status text to status code
            int status;
            switch (statusText) {
                case "正常":
                    status = Equipment.STATUS_NORMAL;
                    break;
                case "损坏":
                    status = Equipment.STATUS_NORMAL; // 暂时使用正常状态
                    break;
                case "维修中":
                    status = Equipment.STATUS_MAINTENANCE;
                    break;
                case "已报废":
                    status = Equipment.STATUS_SCRAPPED;
                    break;
                default:
                    status = Equipment.STATUS_NORMAL;
            }

            // Parse date
            final Date purchaseDate;
            if (!dateField.getText().trim().isEmpty()) {
                try {
                    purchaseDate = Date.valueOf(dateField.getText().trim());
                } catch (IllegalArgumentException e) {
                    JOptionPane.showMessageDialog(this, "购买日期格式不正确，请使用 yyyy-MM-dd 格式。", "输入错误",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else {
                purchaseDate = null;
            }

            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    if (currentEquipment == null) { // Add new equipment
                        Equipment newEquipment = new Equipment(selectedRoom.getRoomId(), name, model, status,
                                purchaseDate);
                        return equipmentDAO.addEquipment(newEquipment);
                    } else { // Update existing equipment
                        Equipment updatedEquipment = new Equipment(currentEquipment.getEquipmentId(),
                                selectedRoom.getRoomId(), name, model, status, purchaseDate);
                        return equipmentDAO.updateEquipment(updatedEquipment);
                    }
                }

                @Override
                protected void done() {
                    try {
                        if (get()) {
                            saved = true;
                            dispose();
                        } else {
                            JOptionPane.showMessageDialog(EquipmentEditDialog.this, "保存失败。", "错误",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(EquipmentEditDialog.this, "保存时发生错误: " + e.getMessage(), "数据库错误",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        }
    }
}