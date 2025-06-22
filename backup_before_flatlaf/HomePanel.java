import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import net.miginfocom.swing.MigLayout;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/**
 * 主页面板，作为应用程序的欢迎页面。
 */
public class HomePanel extends JPanel {

    private JLabel totalRoomsLabel;
    private JLabel availableRoomsLabel;
    private JLabel todayBookingsLabel;
    private JPanel quickActionsPanel;
    private JPanel statsPanel;
    private final Consumer<String> navigationCallback;

    public HomePanel(Consumer<String> callback) {
        this.navigationCallback = callback;
        initComponents();
        loadHomeData();
    }

    private void initComponents() {
        setLayout(new MigLayout("fill, insets 20", "[grow]", "[][][grow]"));
        setBackground(new Color(245, 248, 252));

        // 欢迎标题
        JLabel welcomeLabel = new JLabel("欢迎使用会议室预订系统");
        welcomeLabel.setFont(new Font("微软雅黑", Font.BOLD, 28));
        welcomeLabel.setForeground(new Color(41, 128, 185));
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(welcomeLabel, "growx, wrap, gapbottom 20");

        // 统计信息面板
        createStatsPanel();
        add(statsPanel, "growx, wrap, gapbottom 20");

        // 快速操作面板
        createQuickActionsPanel();
        add(quickActionsPanel, "grow");
    }

    private void createStatsPanel() {
        statsPanel = new JPanel(new MigLayout("fillx, insets 15", "[grow][grow][grow]", "[]"));
        statsPanel.setBackground(Color.WHITE);
        statsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#E9ECEF"), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        // 初始化标签以便后续更新
        totalRoomsLabel = new JLabel("0");
        availableRoomsLabel = new JLabel("0");
        todayBookingsLabel = new JLabel("0");

        // 添加统计卡片
        addStatCard("总会议室数", totalRoomsLabel, Color.decode("#007BFF"));
        addStatCard("可用会议室", availableRoomsLabel, Color.decode("#28A745"));
        addStatCard("今日预订", todayBookingsLabel, Color.decode("#FFC107"));
    }

    private void addStatCard(String title, JLabel valueLabel, Color color) {
        JPanel card = new JPanel(new MigLayout("wrap 1, insets 10", "[grow]", "[]"));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        card.setOpaque(true);
        card.setPreferredSize(new Dimension(0, 80));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        titleLabel.setForeground(Color.GRAY);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        valueLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        valueLabel.setForeground(color);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);

        card.add(titleLabel);
        card.add(valueLabel);

        statsPanel.add(card, "grow");
    }

    private void createQuickActionsPanel() {
        quickActionsPanel = new JPanel(new MigLayout("fill, insets 15", "[grow][grow]", "[][grow]"));
        quickActionsPanel.setBackground(Color.WHITE);
        quickActionsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#E9ECEF"), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        // 标题
        JLabel titleLabel = new JLabel("快速操作");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        titleLabel.setForeground(Color.decode("#2C3E50"));
        quickActionsPanel.add(titleLabel, "span 2, wrap, gapbottom 15");

        // 快速操作按钮 (顺序调整，内容修改)
        addQuickActionButton("新建预订", "快速预订可用的会议室",
                new Color(255, 193, 7), e -> openBooking());
        addQuickActionButton("查看会议室状态", "查看所有会议室的当前状态和可用性",
                new Color(40, 167, 69), e -> openRoomStatus());
        addQuickActionButton("我的预订", "查看和管理您的会议室预订",
                new Color(0, 123, 255), e -> openMyBookings());
        addQuickActionButton("个人信息", "查看和修改您的个人信息",
                new Color(108, 117, 125), e -> openProfile());
    }

    private void addQuickActionButton(String title, String description, Color color, ActionListener action) {
        JPanel buttonPanel = new JPanel(new MigLayout("wrap 1, insets 15", "[grow]", "[]"));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        JButton button = new JButton(title);
        button.setFont(new Font("微软雅黑", Font.BOLD, 15));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.darker());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });
        button.addActionListener(action);

        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        descLabel.setForeground(Color.GRAY);
        descLabel.setHorizontalAlignment(SwingConstants.CENTER);

        buttonPanel.add(button, "growx, gapbottom 10");
        buttonPanel.add(descLabel, "growx");

        quickActionsPanel.add(buttonPanel, "grow");
    }

    private void loadHomeData() {
        // 使用SwingWorker异步加载统计数据
        SwingWorker<List<Integer>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Integer> doInBackground() throws Exception {
                MeetingRoomDAO meetingRoomDAO = new MeetingRoomDAO();
                ReservationDAO reservationDAO = new ReservationDAO();

                int totalRooms = meetingRoomDAO.getTotalRoomCount();
                int availableRooms = meetingRoomDAO.getAvailableRoomCount();
                int todayBookings = reservationDAO.getBookingCountForDate(new Date());

                return List.of(totalRooms, availableRooms, todayBookings);
            }

            @Override
            protected void done() {
                try {
                    List<Integer> stats = get();
                    totalRoomsLabel.setText(String.valueOf(stats.get(0)));
                    availableRoomsLabel.setText(String.valueOf(stats.get(1)));
                    todayBookingsLabel.setText(String.valueOf(stats.get(2)));
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    // 在UI上显示错误信息
                    totalRoomsLabel.setText("N/A");
                    availableRoomsLabel.setText("N/A");
                    todayBookingsLabel.setText("N/A");
                    JOptionPane.showMessageDialog(HomePanel.this, "加载主页数据失败: " + e.getMessage(), "错误",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    // 快速操作的方法
    private void navigateTo(String panelKey) {
        if (navigationCallback != null) {
            navigationCallback.accept(panelKey);
        } else {
            // 如果回调未设置，打印到控制台以供调试
            System.out.println("Navigate to: " + panelKey);
        }
    }

    private void openRoomStatus() {
        navigateTo("会议室状态界面");
    }

    private void openMyBookings() {
        navigateTo("我的预订界面");
    }

    private void openBooking() {
        // 假设有一个专门的预订界面，如果主页的 "预订会议室" 按钮应该直接跳转到
        // 会议室状态列表让用户选择，则可以复用 openRoomStatus
        navigateTo("预订会议室界面"); // TODO: 确保MainPage中有名为 "预订会议室界面" 的case
    }

    private void openProfile() {
        navigateTo("个人信息界面");
    }
}