import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * 用户的预订面板，用于显示和管理用户的会议室预订。
 * 此面板将展示用户的预订列表，并提供查看、更新、取消等操作。
 */
public class MyBookingsPanel extends JPanel {

    private final User currentUser;
    private final ReservationDAO reservationDAO;
    private final MeetingRoomDAO meetingRoomDAO; // For fetching room details on edit
    private List<Reservation> userReservations;

    // Filter components
    private JPopupMenu statusFilterMenu;
    private String currentStatusFilter = "全部";
    private JButton filterButton;

    private JPanel cardPanel; // 用于放置预订卡片的面板
    private JScrollPane cardScrollPane; // 卡片面板的滚动容器

    /**
     * 构造函数，初始化用户的预订面板。
     * 设置布局，添加控件，并加载预订数据。
     */
    public MyBookingsPanel(User user) {
        this.currentUser = user;
        this.reservationDAO = new ReservationDAO();
        this.meetingRoomDAO = new MeetingRoomDAO();
        initComponents();
        loadBookings();
    }

    private void initComponents() {
        setLayout(new MigLayout("fill, insets 20", "[][grow]", "[]20[grow]"));

        // 标题
        JLabel titleLabel = new JLabel("我的预订记录");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        add(titleLabel, "align left");

        // 筛选菜单
        statusFilterMenu = new JPopupMenu();
        String[] statusOptions = { "全部", "已确认", "待确认", "已取消", "已完成" };
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

        // 卡片面板布局，固定三列，居中显示
        cardPanel = new JPanel();
        cardPanel.setLayout(new net.miginfocom.swing.MigLayout("wrap 3, gap 24 24, align center top",
                "[grow,fill][grow,fill][grow,fill]", ""));
        cardPanel.setOpaque(false);
        cardScrollPane = new JScrollPane(cardPanel);
        cardScrollPane.setBorder(null);
        add(cardScrollPane, "grow, push, span 2");
    }

    private void applyFilters() {
        if (userReservations == null) {
            return;
        }
        String selectedStatus = currentStatusFilter;
        List<Reservation> filteredList;
        if (selectedStatus == null || selectedStatus.equals("全部")) {
            filteredList = userReservations;
        } else {
            filteredList = userReservations.stream()
                    .filter(reservation -> selectedStatus.equals(reservation.getStatusText()))
                    .collect(Collectors.toList());
        }
        updateCardPanel(filteredList);
    }

    private void updateCardPanel(List<Reservation> reservations) {
        cardPanel.removeAll();
        if (reservations == null || reservations.isEmpty()) {
            JLabel emptyLabel = new JLabel("暂无预订记录");
            emptyLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
            cardPanel.add(emptyLabel, "span, center");
        } else {
            for (Reservation reservation : reservations) {
                cardPanel.add(createBookingCard(reservation), "grow");
            }
        }
        cardPanel.revalidate();
        cardPanel.repaint();
    }

    private JPanel createBookingCard(Reservation reservation) {
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
        JLabel nameLabel = new JLabel(reservation.getSubject() + " (ID: " + reservation.getReservationId() + ")");
        nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        nameLabel.setForeground(titleColor != null ? titleColor : Color.BLACK);
        card.add(nameLabel, BorderLayout.NORTH);

        // 信息区
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        infoPanel.add(makeInfoLine("会议室:", reservation.getRoomName(), infoColor != null ? infoColor : Color.DARK_GRAY));
        infoPanel
                .add(makeInfoLine("状态:", reservation.getStatusText(), infoColor != null ? infoColor : Color.DARK_GRAY));
        infoPanel.add(makeInfoLine("开始时间:", dateFormat.format(reservation.getStartTime()),
                infoColor != null ? infoColor : Color.DARK_GRAY));
        infoPanel.add(makeInfoLine("结束时间:", dateFormat.format(reservation.getEndTime()),
                infoColor != null ? infoColor : Color.DARK_GRAY));
        infoPanel.add(Box.createVerticalStrut(6));
        infoPanel.add(new JSeparator());

        // 异步加载会议室详细信息
        new SwingWorker<MeetingRoom, Void>() {
            @Override
            protected MeetingRoom doInBackground() throws Exception {
                return meetingRoomDAO.getMeetingRoomByIdWithType(reservation.getRoomId());
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
        JButton editBtn = new JButton("修改预订");
        JButton cancelBtn = new JButton("取消预订");

        detailsBtn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        editBtn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        cancelBtn.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        detailsBtn.addActionListener(e -> showBookingDetails(reservation));
        editBtn.addActionListener(e -> editBooking(reservation));
        cancelBtn.addActionListener(e -> cancelBooking(reservation));

        btnPanel.add(detailsBtn);
        btnPanel.add(editBtn);
        btnPanel.add(cancelBtn);
        card.add(btnPanel, BorderLayout.SOUTH);

        // 根据预订状态设置按钮状态
        boolean isConfirmed = reservation.getStatus() == Reservation.STATUS_CONFIRMED;
        editBtn.setEnabled(isConfirmed);
        cancelBtn.setEnabled(isConfirmed);

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

    private void showBookingDetails(Reservation reservation) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        new SwingWorker<MeetingRoom, Void>() {
            @Override
            protected MeetingRoom doInBackground() throws Exception {
                return meetingRoomDAO.getMeetingRoomById(reservation.getRoomId());
            }

            @Override
            protected void done() {
                try {
                    MeetingRoom room = get();
                    StringBuilder htmlBuilder = new StringBuilder(
                            "<html><body style='width: 350px; padding: 5px;'>");
                    htmlBuilder.append("<h2>预订详情</h2>");
                    htmlBuilder.append("<p><b>预订ID:</b> ").append(reservation.getReservationId()).append("</p>");
                    htmlBuilder.append("<p><b>会议主题:</b> ").append(reservation.getSubject()).append("</p>");
                    htmlBuilder.append("<p><b>会议室:</b> ").append(reservation.getRoomName()).append("</p>");
                    htmlBuilder.append("<p><b>状态:</b> ").append(reservation.getStatusText()).append("</p>");
                    htmlBuilder.append("<p><b>开始时间:</b> ").append(dateFormat.format(reservation.getStartTime()))
                            .append("</p>");
                    htmlBuilder.append("<p><b>结束时间:</b> ").append(dateFormat.format(reservation.getEndTime()))
                            .append("</p>");

                    if (room != null) {
                        htmlBuilder.append("<hr>");
                        htmlBuilder.append("<h3>会议室信息</h3>");
                        htmlBuilder.append("<p><b>位置:</b> ").append(room.getLocation()).append("</p>");
                        htmlBuilder.append("<p><b>容量:</b> ").append(room.getCapacity()).append("人</p>");
                        htmlBuilder.append("<p><b>状态:</b> ").append(room.getStatusText()).append("</p>");
                        htmlBuilder.append("<p><b>描述:</b> ")
                                .append(room.getDescription().isEmpty() ? "无" : room.getDescription())
                                .append("</p>");
                    }

                    htmlBuilder.append("</body></html>");
                    JEditorPane editorPane = new JEditorPane("text/html", htmlBuilder.toString());
                    editorPane.setEditable(false);
                    editorPane.setBackground(UIManager.getColor("Panel.background"));
                    JScrollPane scrollPane = new JScrollPane(editorPane);
                    scrollPane.setPreferredSize(new Dimension(450, 300));
                    JOptionPane.showMessageDialog(MyBookingsPanel.this, scrollPane, "预订详情",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(MyBookingsPanel.this, "获取详情失败: " + e.getMessage(), "错误",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void editBooking(Reservation reservation) {
        // To open the dialog, we need the full MeetingRoom object.
        new SwingWorker<MeetingRoom, Void>() {
            @Override
            protected MeetingRoom doInBackground() throws Exception {
                return meetingRoomDAO.getMeetingRoomById(reservation.getRoomId());
            }

            @Override
            protected void done() {
                try {
                    MeetingRoom room = get();
                    if (room != null) {
                        ReservationDialog dialog = new ReservationDialog(
                                (Frame) SwingUtilities.getWindowAncestor(MyBookingsPanel.this),
                                currentUser, room, reservation, (saved) -> {
                                    if (saved) {
                                        loadBookings(); // Refresh list on successful save
                                    }
                                });
                        dialog.setVisible(true);
                    } else {
                        JOptionPane.showMessageDialog(MyBookingsPanel.this, "无法获取会议室信息，无法修改预订。", "错误",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(MyBookingsPanel.this, "打开修改窗口时出错: " + e.getMessage(), "错误",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void cancelBooking(Reservation reservation) {
        int confirmation = JOptionPane.showConfirmDialog(this, "您确定要取消这个预订吗？", "确认取消", JOptionPane.YES_NO_OPTION);
        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return reservationDAO.updateReservationStatus(reservation.getReservationId(),
                        Reservation.STATUS_CANCELLED);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(MyBookingsPanel.this, "预订已成功取消。", "成功",
                                JOptionPane.INFORMATION_MESSAGE);
                        loadBookings();
                    } else {
                        JOptionPane.showMessageDialog(MyBookingsPanel.this, "取消预订失败。", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(MyBookingsPanel.this, "取消预订时发生错误: " + e.getMessage(), "错误",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    /**
     * 加载用户的预订数据并更新UI。
     */
    private void loadBookings() {
        new SwingWorker<List<Reservation>, Void>() {
            @Override
            protected List<Reservation> doInBackground() throws Exception {
                return reservationDAO.getReservationsByUserId(currentUser.getUserId());
            }

            @Override
            protected void done() {
                try {
                    userReservations = get();
                    applyFilters(); // Apply current filters to populate the cards
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(MyBookingsPanel.this, "加载预订记录失败: " + e.getMessage(), "错误",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}