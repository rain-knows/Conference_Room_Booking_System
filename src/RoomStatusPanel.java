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
    private final MeetingRoomDAO meetingRoomDAO;
    private final EquipmentDAO equipmentDAO;
    private List<MeetingRoomDAO.MeetingRoomStatusDTO> roomStatusList; // Full list from DB

    // Filter components
    private JPopupMenu statusFilterMenu;
    private String currentStatusFilter = "全部";
    private JButton filterButton;

    private JPanel cardPanel; // 新增：用于放置会议室卡片的面板
    private JScrollPane cardScrollPane; // 新增：卡片面板的滚动容器

    public RoomStatusPanel(User user) {
        this.currentUser = user;
        this.meetingRoomDAO = new MeetingRoomDAO();
        this.equipmentDAO = new EquipmentDAO();
        initComponents();
        loadRoomStatus();
    }

    private void initComponents() {
        setLayout(new MigLayout("fill, insets 20", "[][grow]", "[]20[grow]"));

        JLabel titleLabel = new JLabel("会议室状态");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        add(titleLabel, "align left");

        // 筛选菜单
        statusFilterMenu = new JPopupMenu();
        String[] statusOptions = { "全部", "空闲", "使用中", "维护中", "已停用" };
        for (String status : statusOptions) {
            JMenuItem item = new JMenuItem(status);
            item.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            item.addActionListener(e -> {
                currentStatusFilter = status;
                applyFilters();
            });
            statusFilterMenu.add(item);
        }
        filterButton = new JButton("筛选: " + currentStatusFilter + " ▼");
        filterButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        filterButton.addActionListener(e -> statusFilterMenu.show(filterButton, 0, filterButton.getHeight()));
        add(filterButton, "align right, wrap");

        // 卡片面板布局优化，固定三列，居中显示
        cardPanel = new JPanel();
        cardPanel.setLayout(new net.miginfocom.swing.MigLayout("wrap 3, gap 24 24, align center top",
                "[grow,fill][grow,fill][grow,fill]", ""));
        cardPanel.setOpaque(false);
        cardScrollPane = new JScrollPane(cardPanel);
        cardScrollPane.setBorder(null);
        add(cardScrollPane, "grow, push, span 2");
    }

    private void applyFilters() {
        if (roomStatusList == null) {
            return;
        }
        String selectedStatus = currentStatusFilter;
        List<MeetingRoomDAO.MeetingRoomStatusDTO> filteredList;
        if (selectedStatus == null || selectedStatus.equals("全部")) {
            filteredList = roomStatusList;
        } else {
            filteredList = roomStatusList.stream()
                    .filter(dto -> selectedStatus.equals(dto.getStatus()))
                    .collect(Collectors.toList());
        }
        updateCardPanel(filteredList);
    }

    private void updateCardPanel(List<MeetingRoomDAO.MeetingRoomStatusDTO> dtoList) {
        cardPanel.removeAll();
        if (dtoList == null || dtoList.isEmpty()) {
            JLabel emptyLabel = new JLabel("暂无会议室");
            emptyLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
            cardPanel.add(emptyLabel, "span, center");
        } else {
            for (MeetingRoomDAO.MeetingRoomStatusDTO dto : dtoList) {
                cardPanel.add(createRoomCard(dto), "grow");
            }
        }
        cardPanel.revalidate();
        cardPanel.repaint();
    }

    private JPanel createRoomCard(MeetingRoomDAO.MeetingRoomStatusDTO dto) {
        JPanel card = new JPanel(new BorderLayout());
        // 使用UIManager获取主题色
        final Color borderColor = UIManager.getColor("Separator.foreground");
        final Color bgColor = UIManager.getColor("Panel.background");
        final Color titleColor = UIManager.getColor("Label.foreground");
        final Color infoColor = UIManager.getColor("Label.foreground");
        final Color subInfoColor = UIManager.getColor("Label.disabledForeground");
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor != null ? borderColor : Color.GRAY, 2, true),
                new EmptyBorder(16, 20, 16, 20)));
        card.setBackground(bgColor != null ? bgColor : Color.WHITE);

        // 标题区
        JLabel nameLabel = new JLabel(dto.getRoomName() + " (ID: " + dto.getRoomId() + ")");
        nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        nameLabel.setForeground(titleColor != null ? titleColor : Color.BLACK);
        card.add(nameLabel, BorderLayout.NORTH);

        // 信息区（异步加载更多详情）
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        // 先展示基础信息
        infoPanel.add(makeInfoLine("状态:", dto.getStatus(), infoColor != null ? infoColor : Color.DARK_GRAY));
        infoPanel.add(
                makeInfoLine("当前会议:", dto.getCurrentBookingSubject() != null ? dto.getCurrentBookingSubject() : "—",
                        infoColor != null ? infoColor : Color.DARK_GRAY));
        infoPanel.add(makeInfoLine("预订时间:",
                dto.getCurrentBookingTime() != null && !dto.getCurrentBookingTime().isEmpty()
                        ? dto.getCurrentBookingTime()
                        : "—",
                infoColor != null ? infoColor : Color.DARK_GRAY));
        infoPanel.add(Box.createVerticalStrut(6));
        infoPanel.add(new JSeparator());

        // 异步加载详细信息
        new SwingWorker<MeetingRoom, Void>() {
            @Override
            protected MeetingRoom doInBackground() throws Exception {
                return meetingRoomDAO.getMeetingRoomByIdWithType(dto.getRoomId());
            }

            @Override
            protected void done() {
                try {
                    MeetingRoom room = get();
                    if (room != null) {
                        infoPanel.add(makeInfoLine("容量:", room.getCapacity() + "人",
                                subInfoColor != null ? subInfoColor : Color.GRAY));
                        infoPanel.add(makeInfoLine("位置:", room.getLocation(),
                                subInfoColor != null ? subInfoColor : Color.GRAY));
                        infoPanel.add(makeInfoLine("类型:", room.getRoomTypeCode() != null ? room.getRoomTypeCode() : "—",
                                subInfoColor != null ? subInfoColor : Color.GRAY));
                        infoPanel.add(makeInfoLine("描述:",
                                room.getDescription() != null && !room.getDescription().isEmpty()
                                        ? room.getDescription()
                                        : "—",
                                subInfoColor != null ? subInfoColor : Color.GRAY));
                        // 异步加载设备
                        new SwingWorker<List<Equipment>, Void>() {
                            @Override
                            protected List<Equipment> doInBackground() throws Exception {
                                return equipmentDAO.getEquipmentByRoomId(dto.getRoomId());
                            }

                            @Override
                            protected void done() {
                                try {
                                    List<Equipment> eqs = get();
                                    if (eqs != null && !eqs.isEmpty()) {
                                        StringBuilder eqStr = new StringBuilder();
                                        for (int i = 0; i < Math.min(3, eqs.size()); i++) {
                                            eqStr.append(eqs.get(i).getName());
                                            if (i < Math.min(3, eqs.size()) - 1)
                                                eqStr.append("，");
                                        }
                                        if (eqs.size() > 3)
                                            eqStr.append("...");
                                        infoPanel.add(makeInfoLine("主要设备:", eqStr.toString(),
                                                subInfoColor != null ? subInfoColor : Color.GRAY));
                                    } else {
                                        infoPanel.add(makeInfoLine("主要设备:", "无",
                                                subInfoColor != null ? subInfoColor : Color.GRAY));
                                    }
                                    infoPanel.revalidate();
                                    infoPanel.repaint();
                                } catch (Exception ignore) {
                                }
                            }
                        }.execute();
                    }
                    infoPanel.revalidate();
                    infoPanel.repaint();
                } catch (Exception ignore) {
                }
            }
        }.execute();

        card.add(infoPanel, BorderLayout.CENTER);

        // 按钮区
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);
        JButton detailsBtn = new JButton("查看详情");
        JButton bookBtn = new JButton("预约会议室");
        detailsBtn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        bookBtn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        detailsBtn.addActionListener(e -> showRoomDetailsById(dto.getRoomId()));
        bookBtn.addActionListener(e -> bookRoomById(dto.getRoomId(), dto.getStatus()));
        btnPanel.add(detailsBtn);
        btnPanel.add(bookBtn);
        card.add(btnPanel, BorderLayout.SOUTH);

        updateBookButtonState(bookBtn, dto.getRoomId(), dto.getStatus());
        return card;
    }

    private JPanel makeInfoLine(String label, String value, Color fg) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.setOpaque(false);
        JLabel l1 = new JLabel(label);
        l1.setForeground(fg);
        l1.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JLabel l2 = new JLabel(value);
        l2.setForeground(fg);
        l2.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        p.add(l1);
        p.add(Box.createHorizontalStrut(6));
        p.add(l2);
        return p;
    }

    private void showRoomDetailsById(int roomId) {
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

    private void bookRoomById(int roomId, String status) {
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return meetingRoomDAO.canUserBookRoom(currentUser.getRole(), roomId);
            }

            @Override
            protected void done() {
                try {
                    boolean canBook = get();
                    if (!canBook) {
                        JOptionPane.showMessageDialog(RoomStatusPanel.this, "您没有权限预订此会议室。", "无法预约",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    new SwingWorker<MeetingRoom, Void>() {
                        @Override
                        protected MeetingRoom doInBackground() throws Exception {
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
                                                    loadRoomStatus();
                                                }
                                            });
                                    dialog.setVisible(true);
                                }
                            } catch (Exception e) {
                                JOptionPane.showMessageDialog(RoomStatusPanel.this, "无法打开预订窗口: " + e.getMessage(), "错误",
                                        JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }.execute();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(RoomStatusPanel.this, "权限检查失败: " + e.getMessage(), "错误",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void updateBookButtonState(JButton bookBtn, int roomId, String status) {
        bookBtn.setEnabled(false);
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return meetingRoomDAO.canUserBookRoom(currentUser.getRole(), roomId);
            }

            @Override
            protected void done() {
                try {
                    boolean canBook = get();
                    bookBtn.setEnabled(canBook);
                } catch (Exception e) {
                    bookBtn.setEnabled(false);
                }
            }
        }.execute();
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

    // Custom renderer to color the status cell
    static class StatusCellRenderer extends JLabel implements TableCellRenderer {
        public StatusCellRenderer() {
            setOpaque(true);
            setHorizontalAlignment(CENTER);
            setFont(new Font("微软雅黑", Font.BOLD, 12));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            setText(value.toString());

            // 使用主题默认颜色，不设置硬编码颜色
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                setBackground(table.getBackground());
                setForeground(table.getForeground());
            }

            return this;
        }
    }
}