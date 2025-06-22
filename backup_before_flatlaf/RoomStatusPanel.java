import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * 会议室状态面板，用于显示所有会议室的当前状态。
 * 此面板将展示会议室列表，包括会议室名称、当前状态、当前预订信息等。
 */
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.util.Vector;
import java.text.SimpleDateFormat;
import javax.swing.table.JTableHeader;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

/**
 * 会议室状态面板，用于显示会议室的当前状态。
 */
public class RoomStatusPanel extends JPanel {

    private final User currentUser;
    private JTable roomTable;
    private DefaultTableModel tableModel;
    private final MeetingRoomDAO meetingRoomDAO;
    private final EquipmentDAO equipmentDAO;
    private List<MeetingRoomDAO.MeetingRoomStatusDTO> roomStatusList; // Full list from DB

    private JButton bookButton;
    private JButton detailsButton;

    // Filter components
    private JPopupMenu statusFilterMenu;
    private String currentStatusFilter = "全部";

    public RoomStatusPanel(User user) {
        this.currentUser = user;
        this.meetingRoomDAO = new MeetingRoomDAO();
        this.equipmentDAO = new EquipmentDAO();
        initComponents();
        loadRoomStatus();
    }

    private void initComponents() {
        setLayout(new MigLayout("fill, insets 20", "[grow][]", "[][grow]"));

        JLabel titleLabel = new JLabel("会议室状态");
        UIStyleUtil.beautifyTitleLabel(titleLabel);
        add(titleLabel, "span, wrap, gapbottom 15");

        // Table
        String[] columnNames = { "ID", "会议室名称", "状态", "当前会议", "预订时间" };
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
        roomTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        roomTable.getColumn("状态").setCellRenderer(new StatusCellRenderer());
        roomTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtonState();
            }
        });
        // 表头筛选弹出菜单
        statusFilterMenu = new JPopupMenu();
        String[] statusOptions = { "全部", "空闲", "使用中", "维护中", "已停用" };
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
        // 自定义表头渲染器，状态列加下拉箭头
        header.setDefaultRenderer((table, value, isSelected, hasFocus, row, column) -> {
            JLabel lbl = new JLabel();
            lbl.setFont(header.getFont());
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setOpaque(true);
            lbl.setBackground(header.getBackground());
            if (column == 2) {
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
                if (col == 2) { // 状态列
                    statusFilterMenu.show(header, e.getX(), header.getHeight());
                }
            }
        });
        add(new JScrollPane(roomTable), "grow, push");

        // Button Panel
        JPanel buttonPanel = new JPanel(new MigLayout("wrap 1, fillx", "[grow, fill]"));
        detailsButton = new JButton("查看详情");
        bookButton = new JButton("预约会议室");
        UIStyleUtil.beautifyButton(detailsButton);
        UIStyleUtil.beautifyButton(bookButton);
        detailsButton.addActionListener(e -> showSelectedRoomDetails());
        bookButton.addActionListener(e -> bookSelectedRoom());
        buttonPanel.setOpaque(false);
        buttonPanel.add(detailsButton, "gaptop 10");
        buttonPanel.add(bookButton, "gaptop 10");
        add(buttonPanel, "top");

        UIStyleUtil.setMainBackground(this);
        updateButtonState(); // Initial state
    }

    private void applyFilters() {
        if (roomStatusList == null) {
            return;
        }
        String selectedStatus = currentStatusFilter;
        if (selectedStatus == null || selectedStatus.equals("全部")) {
            updateTableModel(roomStatusList);
            return;
        }
        List<MeetingRoomDAO.MeetingRoomStatusDTO> filteredList = roomStatusList.stream()
                .filter(dto -> selectedStatus.equals(dto.getStatus()))
                .collect(Collectors.toList());
        updateTableModel(filteredList);
    }

    private void updateTableModel(List<MeetingRoomDAO.MeetingRoomStatusDTO> dtoList) {
        tableModel.setRowCount(0);
        if (dtoList == null)
            return;
        for (var dto : dtoList) {
            Vector<Object> row = new Vector<>();
            row.add(dto.getRoomId());
            row.add(dto.getRoomName());
            row.add(dto.getStatus());
            row.add(dto.getCurrentBookingSubject() != null ? dto.getCurrentBookingSubject() : "—");
            row.add(dto.getCurrentBookingTime() != null && !dto.getCurrentBookingTime().isEmpty()
                    ? dto.getCurrentBookingTime()
                    : "—");
            tableModel.addRow(row);
        }
        // After updating table, selection is lost, so buttons should be disabled.
        updateButtonState();
    }

    private void loadRoomStatus() {
        new SwingWorker<List<MeetingRoomDAO.MeetingRoomStatusDTO>, Void>() {
            @Override
            protected List<MeetingRoomDAO.MeetingRoomStatusDTO> doInBackground() throws Exception {
                // 根据用户角色获取可访问的会议室
                List<MeetingRoom> accessibleRooms = meetingRoomDAO.getAccessibleMeetingRooms(currentUser.getRole());

                // 获取所有会议室的完整状态信息
                List<MeetingRoomDAO.MeetingRoomStatusDTO> allRoomStatuses = meetingRoomDAO
                        .getAllMeetingRoomsWithStatus();

                // 过滤出用户有权限访问的会议室
                List<MeetingRoomDAO.MeetingRoomStatusDTO> accessibleRoomStatuses = new ArrayList<>();
                for (MeetingRoomDAO.MeetingRoomStatusDTO statusDTO : allRoomStatuses) {
                    for (MeetingRoom accessibleRoom : accessibleRooms) {
                        if (accessibleRoom.getRoomId() == statusDTO.getRoomId()) {
                            accessibleRoomStatuses.add(statusDTO);
                            break;
                        }
                    }
                }

                return accessibleRoomStatuses;
            }

            @Override
            protected void done() {
                try {
                    roomStatusList = get(); // Store the full list
                    applyFilters(); // Apply current filters to populate the table
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(RoomStatusPanel.this, "加载会议室状态失败: " + e.getMessage(), "错误",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void updateButtonState() {
        int selectedRow = roomTable.getSelectedRow();
        if (selectedRow == -1) {
            detailsButton.setEnabled(false);
            bookButton.setEnabled(false);
        } else {
            detailsButton.setEnabled(true);
            String status = (String) tableModel.getValueAt(selectedRow, 2);
            int roomId = (int) tableModel.getValueAt(selectedRow, 0);

            // 检查用户是否有权限预订此会议室
            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return meetingRoomDAO.canUserBookRoom(currentUser.getRole(), roomId);
                }

                @Override
                protected void done() {
                    try {
                        boolean canBook = get();
                        // 只有用户有权限预订且会议室状态为空闲时才启用预订按钮
                        bookButton.setEnabled("空闲".equals(status) && canBook);
                    } catch (Exception e) {
                        // 如果权限检查失败，默认禁用预订按钮
                        bookButton.setEnabled(false);
                    }
                }
            }.execute();
        }
    }

    private void showSelectedRoomDetails() {
        int selectedRow = roomTable.getSelectedRow();
        if (selectedRow == -1)
            return;

        int roomId = (int) tableModel.getValueAt(selectedRow, 0);

        // Define a structure to hold both room and equipment data
        class RoomDetails {
            final MeetingRoom room;
            final List<Equipment> equipmentList;

            RoomDetails(MeetingRoom room, List<Equipment> equipmentList) {
                this.room = room;
                this.equipmentList = equipmentList;
            }
        }

        new SwingWorker<RoomDetails, Void>() {
            @Override
            protected RoomDetails doInBackground() throws Exception {
                MeetingRoom room = meetingRoomDAO.getMeetingRoomById(roomId);
                if (room != null) {
                    List<Equipment> equipment = equipmentDAO.getEquipmentByRoomId(roomId);
                    return new RoomDetails(room, equipment);
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    RoomDetails details = get();
                    if (details != null && details.room != null) {
                        StringBuilder htmlBuilder = new StringBuilder(
                                "<html><body style='width: 300px; padding: 5px;'>");
                        htmlBuilder.append("<h2>").append(details.room.getName()).append("</h2>");
                        htmlBuilder.append("<p><b>位置:</b> ").append(details.room.getLocation()).append("</p>");
                        htmlBuilder.append("<p><b>容量:</b> ").append(details.room.getCapacity()).append("人</p>");
                        htmlBuilder.append("<p><b>状态:</b> ").append(details.room.getStatusText()).append("</p>");
                        htmlBuilder.append("<p><b>描述:</b> ")
                                .append(details.room.getDescription().isEmpty() ? "无" : details.room.getDescription())
                                .append("</p>");
                        htmlBuilder.append("<hr>");
                        htmlBuilder.append("<h3>设备列表</h3>");

                        if (details.equipmentList.isEmpty()) {
                            htmlBuilder.append("<p>此会议室没有配置任何设备。</p>");
                        } else {
                            htmlBuilder.append("<table width='100%' border='0' cellspacing='0' cellpadding='2'>");
                            htmlBuilder.append("<tr><th>设备名称</th><th>型号</th><th>状态</th></tr>");
                            for (Equipment eq : details.equipmentList) {
                                htmlBuilder.append("<tr>");
                                htmlBuilder.append("<td>").append(eq.getName()).append("</td>");
                                htmlBuilder.append("<td>").append(eq.getModel() != null ? eq.getModel() : "N/A")
                                        .append("</td>");
                                htmlBuilder.append("<td>").append(eq.getStatusText()).append("</td>");
                                htmlBuilder.append("</tr>");
                            }
                            htmlBuilder.append("</table>");
                        }

                        htmlBuilder.append("</body></html>");

                        JEditorPane editorPane = new JEditorPane("text/html", htmlBuilder.toString());
                        editorPane.setEditable(false);
                        editorPane.setBackground(UIManager.getColor("Panel.background"));

                        JScrollPane scrollPane = new JScrollPane(editorPane);
                        scrollPane.setPreferredSize(new Dimension(450, 300));

                        JOptionPane.showMessageDialog(RoomStatusPanel.this, scrollPane, "会议室详情",
                                JOptionPane.INFORMATION_MESSAGE);

                    } else {
                        JOptionPane.showMessageDialog(RoomStatusPanel.this, "无法获取会议室详情。", "错误",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(RoomStatusPanel.this, "获取详情失败: " + e.getMessage(), "错误",
                            JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private void bookSelectedRoom() {
        int selectedRow = roomTable.getSelectedRow();
        if (selectedRow == -1)
            return;

        int roomId = (int) tableModel.getValueAt(selectedRow, 0);

        new SwingWorker<MeetingRoom, Void>() {
            @Override
            protected MeetingRoom doInBackground() throws Exception {
                // 首先检查用户是否有权限预订此会议室
                boolean canBook = meetingRoomDAO.canUserBookRoom(currentUser.getRole(), roomId);
                if (!canBook) {
                    throw new SecurityException("您没有权限预订此类型的会议室。");
                }

                return meetingRoomDAO.getMeetingRoomByIdWithType(roomId);
            }

            @Override
            protected void done() {
                try {
                    MeetingRoom roomToBook = get();
                    if (roomToBook != null) {
                        ReservationDialog dialog = new ReservationDialog(
                                (Frame) SwingUtilities.getWindowAncestor(RoomStatusPanel.this),
                                currentUser, roomToBook, null, (saved) -> {
                                    if (saved) {
                                        loadRoomStatus(); // Refresh on successful booking
                                    }
                                });
                        dialog.setVisible(true);
                    }
                } catch (SecurityException e) {
                    JOptionPane.showMessageDialog(RoomStatusPanel.this, e.getMessage(), "权限不足",
                            JOptionPane.WARNING_MESSAGE);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(RoomStatusPanel.this, "无法打开预订窗口: " + e.getMessage(), "错误",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    // Custom renderer to color the status cell
    static class StatusCellRenderer extends JLabel implements TableCellRenderer {
        public StatusCellRenderer() {
            setOpaque(true);
            setHorizontalAlignment(CENTER);
            setFont(new Font("微软雅黑", Font.BOLD, 12));
            setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            setText(value.toString());
            setForeground(Color.WHITE);

            switch (value.toString()) {
                case "空闲":
                    setBackground(Color.decode("#28A745")); // Green
                    break;
                case "使用中":
                    setBackground(Color.decode("#DC3545")); // Red
                    break;
                case "维护中":
                case "已停用":
                    setBackground(Color.decode("#FFC107")); // Yellow
                    break;
                default:
                    setBackground(Color.GRAY);
                    break;
            }
            return this;
        }
    }
}