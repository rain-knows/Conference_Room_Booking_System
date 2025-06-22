import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;
import java.util.Vector;

/**
 * 管理员会议室管理面板，用于管理员管理会议室信息。
 */
public class AdminRoomManagementPanel extends JPanel {

    private JTable roomTable;
    private DefaultTableModel tableModel;
    private MeetingRoomDAO meetingRoomDAO;
    private JPopupMenu statusFilterMenu;
    private String currentStatusFilter = "全部";
    private List<MeetingRoom> allRoomsList = new Vector<>();

    /**
     * 构造函数，初始化管理员会议室管理面板。
     */
    public AdminRoomManagementPanel() {
        this.meetingRoomDAO = new MeetingRoomDAO();
        initComponents();
        loadRooms();
    }

    private void initComponents() {
        setLayout(new MigLayout("fill, insets 20", "[grow][]", "[][grow]"));

        JLabel titleLabel = new JLabel("会议室管理");
        UIStyleUtil.beautifyTitleLabel(titleLabel);
        add(titleLabel, "span, wrap, gapbottom 15");

        // Table
        String[] columnNames = { "ID", "名称", "容量", "位置", "状态", "描述" };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        roomTable = new JTable(tableModel);
        roomTable.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        roomTable.setRowHeight(28);
        roomTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 16));
        roomTable.getTableHeader().setBackground(new Color(230, 235, 245));
        roomTable.setSelectionBackground(new Color(204, 229, 255));
        // 表头筛选弹出菜单
        statusFilterMenu = new JPopupMenu();
        String[] statusOptions = { "全部", "可用", "维护中", "已停用" };
        for (String status : statusOptions) {
            JMenuItem item = new JMenuItem(status);
            item.setFont(new Font("微软雅黑", Font.PLAIN, 15));
            item.addActionListener(e -> {
                currentStatusFilter = status;
                applyFilters();
            });
            statusFilterMenu.add(item);
        }
        JTableHeader header = roomTable.getTableHeader();
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
                int col = roomTable.columnAtPoint(e.getPoint());
                if (col == 4) { // 状态列
                    statusFilterMenu.show(header, e.getX(), header.getHeight());
                }
            }
        });
        add(new JScrollPane(roomTable), "grow, push");

        // Button Panel
        JPanel buttonPanel = new JPanel(new MigLayout("wrap 1", "[grow, fill]"));
        buttonPanel.setOpaque(false);
        JButton addButton = new JButton("添加会议室");
        JButton editButton = new JButton("编辑选中项");
        JButton deleteButton = new JButton("删除选中项");
        JButton[] btns = { addButton, editButton, deleteButton };
        for (JButton btn : btns) {
            UIStyleUtil.beautifyButton(btn);
        }
        addButton.setBackground(new Color(40, 180, 99));
        addButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                addButton.setBackground(new Color(30, 160, 80));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                addButton.setBackground(new Color(40, 180, 99));
            }
        });
        addButton.addActionListener(e -> openEditDialog(null));
        editButton.addActionListener(e -> {
            MeetingRoom selected = getSelectedMeetingRoom();
            if (selected != null) {
                openEditDialog(selected);
            } else {
                JOptionPane.showMessageDialog(this, "请先选择要编辑的会议室。", "提示", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        deleteButton.addActionListener(e -> deleteSelectedRoom());
        buttonPanel.add(addButton);
        buttonPanel.add(editButton, "gaptop 10");
        buttonPanel.add(deleteButton, "gaptop 10");
        add(buttonPanel, "top");

        UIStyleUtil.setMainBackground(this);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10),
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true)));
    }

    private MeetingRoom getSelectedMeetingRoom() {
        int selectedRow = roomTable.getSelectedRow();
        if (selectedRow == -1)
            return null;

        int roomId = (int) tableModel.getValueAt(selectedRow, 0);
        String name = (String) tableModel.getValueAt(selectedRow, 1);
        int capacity = (int) tableModel.getValueAt(selectedRow, 2);
        String location = (String) tableModel.getValueAt(selectedRow, 3);
        String statusText = (String) tableModel.getValueAt(selectedRow, 4);
        String description = (String) tableModel.getValueAt(selectedRow, 5);

        int status;
        switch (statusText) {
            case "可用":
                status = MeetingRoom.STATUS_AVAILABLE;
                break;
            case "维护中":
                status = MeetingRoom.STATUS_MAINTENANCE;
                break;
            case "已停用":
                status = MeetingRoom.STATUS_DECOMMISSIONED;
                break;
            default:
                status = -1; // Unknown status
        }

        return new MeetingRoom(roomId, name, capacity, location, description, status);
    }

    private void loadRooms() {
        new SwingWorker<List<MeetingRoom>, Void>() {
            @Override
            protected List<MeetingRoom> doInBackground() throws Exception {
                return meetingRoomDAO.getAllMeetingRooms();
            }

            @Override
            protected void done() {
                try {
                    allRoomsList = get();
                    applyFilters();
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(AdminRoomManagementPanel.this, "加载会议室列表失败: " + e.getMessage(), "错误",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void openEditDialog(MeetingRoom room) {
        RoomEditDialog dialog = new RoomEditDialog((JFrame) SwingUtilities.getWindowAncestor(this), room);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadRooms(); // Refresh the table if data was saved
        }
    }

    private void deleteSelectedRoom() {
        int selectedRow = roomTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请先选择一个要删除的会议室。", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirmation = JOptionPane.showConfirmDialog(this, "确定要删除选中的会议室吗？此操作不可撤销。", "确认删除",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        int roomId = (int) tableModel.getValueAt(selectedRow, 0);

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return meetingRoomDAO.deleteMeetingRoom(roomId);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(AdminRoomManagementPanel.this, "会议室删除成功。", "成功",
                                JOptionPane.INFORMATION_MESSAGE);
                        loadRooms();
                    } else {
                        JOptionPane.showMessageDialog(AdminRoomManagementPanel.this, "删除失败。", "错误",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(AdminRoomManagementPanel.this, "删除失败: " + e.getMessage(), "错误",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void applyFilters() {
        if (allRoomsList == null)
            return;
        List<MeetingRoom> filtered;
        if (currentStatusFilter.equals("全部")) {
            filtered = allRoomsList;
        } else {
            filtered = new Vector<>();
            for (MeetingRoom room : allRoomsList) {
                if (room.getStatusText().equals(currentStatusFilter)) {
                    filtered.add(room);
                }
            }
        }
        tableModel.setRowCount(0);
        for (MeetingRoom room : filtered) {
            Vector<Object> row = new Vector<>();
            row.add(room.getRoomId());
            row.add(room.getName());
            row.add(room.getCapacity());
            row.add(room.getLocation());
            row.add(room.getStatusText());
            row.add(room.getDescription());
            tableModel.addRow(row);
        }
    }

    // Inner class for the Add/Edit dialog
    class RoomEditDialog extends JDialog {
        private JTextField nameField, capacityField, locationField;
        private JTextArea descriptionArea;
        private JComboBox<String> statusComboBox;
        private boolean saved = false;
        private MeetingRoom currentRoom;

        public RoomEditDialog(Frame owner, MeetingRoom room) {
            super(owner, true);
            this.currentRoom = room;
            setTitle(room == null ? "添加新会议室" : "编辑会议室");

            setLayout(new MigLayout("wrap 2, fillx", "[100px][grow,fill]"));

            nameField = new JTextField();
            capacityField = new JTextField();
            locationField = new JTextField();
            descriptionArea = new JTextArea(3, 20);
            statusComboBox = new JComboBox<>(new String[] { "可用", "维护中", "已停用" });

            if (room != null) {
                nameField.setText(room.getName());
                capacityField.setText(String.valueOf(room.getCapacity()));
                locationField.setText(room.getLocation());
                descriptionArea.setText(room.getDescription());
                statusComboBox.setSelectedIndex(room.getStatus() - 1);
            }

            add(new JLabel("名称:"));
            add(nameField, "growx");
            add(new JLabel("容量:"));
            add(capacityField, "growx");
            add(new JLabel("位置:"));
            add(locationField, "growx");
            add(new JLabel("状态:"));
            add(statusComboBox, "growx");
            add(new JLabel("描述:"), "top");
            add(new JScrollPane(descriptionArea), "grow");

            JButton saveButton = new JButton("保存");
            saveButton.addActionListener(e -> save());
            add(saveButton, "span, split 2, align right");

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
            // Basic validation
            if (nameField.getText().trim().isEmpty() || capacityField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "名称和容量不能为空。", "输入错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int capacity;
            try {
                capacity = Integer.parseInt(capacityField.getText().trim());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "容量必须是有效的数字。", "输入错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String name = nameField.getText().trim();
            String location = locationField.getText().trim();
            String description = descriptionArea.getText();
            int status = statusComboBox.getSelectedIndex() + 1;

            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    if (currentRoom == null) { // Add new room
                        MeetingRoom newRoom = new MeetingRoom(name, capacity, location, description, status);
                        return meetingRoomDAO.addMeetingRoom(newRoom);
                    } else { // Update existing room
                        MeetingRoom updatedRoom = new MeetingRoom(currentRoom.getRoomId(), name, capacity, location,
                                description, status);
                        return meetingRoomDAO.updateMeetingRoom(updatedRoom);
                    }
                }

                @Override
                protected void done() {
                    try {
                        if (get()) {
                            saved = true;
                            dispose();
                        } else {
                            JOptionPane.showMessageDialog(RoomEditDialog.this, "保存失败。", "错误",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(RoomEditDialog.this, "保存时发生错误: " + e.getMessage(), "数据库错误",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        }
    }
}