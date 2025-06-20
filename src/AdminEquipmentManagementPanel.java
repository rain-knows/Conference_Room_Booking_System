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
        UIStyleUtil.beautifyTitleLabel(titleLabel);
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
        equipmentTable.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        equipmentTable.setRowHeight(28);
        equipmentTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 16));
        equipmentTable.getTableHeader().setBackground(new Color(230, 235, 245));
        equipmentTable.setSelectionBackground(new Color(204, 229, 255));
        equipmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // 表头筛选弹出菜单
        statusFilterMenu = new JPopupMenu();
        String[] statusOptions = { "全部", "正常", "损坏", "维修中", "已报废" };
        for (String status : statusOptions) {
            JMenuItem item = new JMenuItem(status);
            item.setFont(new Font("微软雅黑", Font.PLAIN, 15));
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
            UIStyleUtil.beautifyButton(btn);
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

        UIStyleUtil.setMainBackground(this);
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
        if (tableModel == null)
            return;
        List<Vector> allRows = new Vector<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Vector row = new Vector();
            for (int j = 0; j < tableModel.getColumnCount(); j++) {
                row.add(tableModel.getValueAt(i, j));
            }
            allRows.add(row);
        }
        tableModel.setRowCount(0);
        if (currentStatusFilter.equals("全部")) {
            for (Vector row : allRows) {
                tableModel.addRow(row);
            }
        } else {
            for (Vector row : allRows) {
                if (row.get(4).equals(currentStatusFilter)) {
                    tableModel.addRow(row);
                }
            }
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
            modelField = new JTextField();
            dateField = new JTextField("yyyy-MM-dd");
            roomComboBox = new JComboBox<>(new Vector<>(rooms));
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

            statusComboBox = new JComboBox<>(new String[] { "正常", "维修中", "报废" });

            if (equipment != null) {
                nameField.setText(equipment.getName());
                modelField.setText(equipment.getModel());
                dateField.setText(new SimpleDateFormat("yyyy-MM-dd").format(equipment.getPurchaseDate()));
                statusComboBox.setSelectedIndex(equipment.getStatus() - 1);
                for (MeetingRoom room : rooms) {
                    if (room.getRoomId() == equipment.getRoomId()) {
                        roomComboBox.setSelectedItem(room);
                        break;
                    }
                }
            }

            add(new JLabel("设备名称:"));
            add(nameField, "growx");
            add(new JLabel("型号:"));
            add(modelField, "growx");
            add(new JLabel("所属会议室:"));
            add(roomComboBox, "growx");
            add(new JLabel("状态:"));
            add(statusComboBox, "growx");
            add(new JLabel("购买日期:"));
            add(dateField, "growx");

            JButton saveButton = new JButton("保存");
            saveButton.addActionListener(e -> save());
            add(saveButton, "span, split 2, align right, gaptop 15");

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
            if (nameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "设备名称不能为空。", "输入错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (roomComboBox.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "必须为设备选择一个会议室。", "输入错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Date purchaseDate;
            try {
                java.util.Date utilDate = new SimpleDateFormat("yyyy-MM-dd").parse(dateField.getText().trim());
                purchaseDate = new Date(utilDate.getTime());
            } catch (ParseException e) {
                JOptionPane.showMessageDialog(this, "日期格式无效，请输入 yyyy-MM-dd 格式。", "输入错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String name = nameField.getText().trim();
            String model = modelField.getText().trim();
            int status = statusComboBox.getSelectedIndex() + 1;
            MeetingRoom selectedRoom = (MeetingRoom) roomComboBox.getSelectedItem();
            int roomId = selectedRoom.getRoomId();

            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    if (currentEquipment == null) {
                        Equipment newEquipment = new Equipment(roomId, name, model, status, purchaseDate);
                        return equipmentDAO.addEquipment(newEquipment);
                    } else {
                        Equipment updatedEquipment = new Equipment(currentEquipment.getEquipmentId(), roomId, name,
                                model, status, purchaseDate);
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