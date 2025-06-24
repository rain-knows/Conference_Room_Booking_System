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

        // 欢迎标题
        JLabel welcomeLabel = new JLabel("欢迎使用会议室预订系统");
        welcomeLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
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
        statsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIManager.getColor("Panel.border"), 1, true),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        // 初始化标签以便后续更新
        totalRoomsLabel = createStatValueLabel();
        availableRoomsLabel = createStatValueLabel();
        todayBookingsLabel = createStatValueLabel();

        // 添加统计卡片（带图标和颜色）
        addStatCard("总会议室数", totalRoomsLabel, UIManager.getIcon("OptionPane.informationIcon"), new Color(0x4F8EF7));
        addStatCard("可用会议室", availableRoomsLabel, UIManager.getIcon("OptionPane.questionIcon"), new Color(0x43B581));
        addStatCard("今日预订", todayBookingsLabel, UIManager.getIcon("OptionPane.warningIcon"), new Color(0xF7B84F));
    }

    // 创建更醒目的数字标签
    private JLabel createStatValueLabel() {
        JLabel label = new JLabel("加载中...");
        label.setFont(new Font("微软雅黑", Font.BOLD, 32));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }

    // 新增：带图标和颜色的统计卡片
    private void addStatCard(String title, JLabel valueLabel, Icon icon, Color bgColor) {
        JPanel card = new JPanel(new MigLayout("wrap 1, insets 10", "[grow]", "[]"));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bgColor.darker(), 2, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        card.setOpaque(true);
        card.setBackground(bgColor);
        card.setPreferredSize(new Dimension(0, 100));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(iconLabel);

        valueLabel.setForeground(Color.WHITE);
        card.add(valueLabel);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        titleLabel.setForeground(new Color(255, 255, 255, 220));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(titleLabel);

        statsPanel.add(card, "grow");
    }

    private void createQuickActionsPanel() {
        quickActionsPanel = new JPanel(new MigLayout("fill, insets 15", "[grow][grow]", "[][grow]"));
        quickActionsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIManager.getColor("Panel.border"), 1, true),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        // 标题
        JLabel titleLabel = new JLabel("快速操作");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        quickActionsPanel.add(titleLabel, "span 2, wrap, gapbottom 15");

        // 快速操作按钮（带图标）
        addQuickActionButton("新建预订", "快速预订可用的会议室", UIManager.getIcon("FileView.fileIcon"), e -> openBooking());
        addQuickActionButton("查看会议室状态", "查看所有会议室的当前状态和可用性", UIManager.getIcon("FileView.directoryIcon"),
                e -> openRoomStatus());
        addQuickActionButton("我的预订", "查看和管理您的会议室预订", UIManager.getIcon("FileView.hardDriveIcon"),
                e -> openMyBookings());
        addQuickActionButton("个人信息", "查看和修改您的个人信息", UIManager.getIcon("FileView.computerIcon"), e -> openProfile());
    }

    // 新增：带图标的快速操作按钮
    private void addQuickActionButton(String title, String description, Icon icon, ActionListener action) {
        JPanel buttonPanel = new JPanel(new MigLayout("wrap 1, insets 10", "[grow]", "[]"));
        buttonPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIManager.getColor("Panel.border"), 1, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        buttonPanel.setOpaque(false);

        JButton button = new JButton(title, icon);
        button.setFont(new Font("微软雅黑", Font.BOLD, 16));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setVerticalTextPosition(SwingConstants.BOTTOM);
        button.setIconTextGap(8);
        button.addActionListener(action);
        // 鼠标悬停高亮
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(0xE6F7FF));
                button.setOpaque(true);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(UIManager.getColor("Button.background"));
                button.setOpaque(false);
            }
        });

        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        descLabel.setHorizontalAlignment(SwingConstants.CENTER);

        buttonPanel.add(button, "growx, gapbottom 8");
        buttonPanel.add(descLabel, "growx");

        quickActionsPanel.add(buttonPanel, "grow");
    }

    private void loadHomeData() {
        // 使用SwingWorker异步加载统计数据
        totalRoomsLabel.setText("加载中...");
        availableRoomsLabel.setText("加载中...");
        todayBookingsLabel.setText("加载中...");
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
        navigateTo("预订会议室界面");
    }

    private void openProfile() {
        navigateTo("个人信息界面");
    }
}