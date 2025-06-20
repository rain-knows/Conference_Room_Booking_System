import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 主页面，包含导航菜单和内容区域。
 * 用户登录后将看到此页面，可以在此页面中导航到不同的功能模块。
 */
public class MainPage extends JFrame {
    private JButton button1;
    private JButton button2;
    private JLabel label;
    private JPanel contentPanel;
    private User currentUser; // 保存当前登录的用户

    // 添加缺失的成员变量声明
    private JButton btnHome;
    private JButton btnRoomStatus;
    private JButton btnMyBookings;
    private JButton btnProfile;
    private JButton btnAdminRoomMgmt;
    private JButton btnAdminUserMgmt;
    private JButton btnAdminSettings;
    private JButton btnAdminEquipmentMgmt;
    private JButton btnLogout;
    private JLabel lblNavigationTitle;

    public MainPage(User user) {
        this.currentUser = user; // 保存用户
        // 设置窗口属性
        setTitle("会议室预订系统");
        setExtendedState(JFrame.MAXIMIZED_BOTH); // 设置为全屏
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        // 设置主背景色
        getContentPane().setBackground(new Color(240, 245, 250));

        // 初始化普通用户按钮
        btnHome = new JButton("主页");
        btnRoomStatus = new JButton("会议室状态");
        btnMyBookings = new JButton("我的预订");
        btnProfile = new JButton("个人信息");

        // 初始化管理员按钮
        btnAdminRoomMgmt = new JButton("会议室管理");
        btnAdminUserMgmt = new JButton("用户管理");
        btnAdminSettings = new JButton("系统设置");
        btnAdminEquipmentMgmt = new JButton("设备管理");
        btnLogout = new JButton("退出登录");

        // 美化所有侧边栏按钮
        JButton[] navButtons = { btnHome, btnRoomStatus, btnMyBookings, btnProfile, btnAdminRoomMgmt, btnAdminUserMgmt,
                btnAdminSettings, btnAdminEquipmentMgmt, btnLogout };
        Color mainColor = new Color(52, 152, 219);
        Color hoverColor = new Color(41, 128, 185);
        for (JButton btn : navButtons) {
            if (btn == null)
                continue;
            btn.setBackground(mainColor);
            btn.setForeground(Color.WHITE);
            btn.setFont(new Font("微软雅黑", Font.BOLD, 16));
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setHorizontalAlignment(SwingConstants.CENTER); // 横向居中
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    btn.setBackground(hoverColor);
                }

                public void mouseExited(java.awt.event.MouseEvent evt) {
                    btn.setBackground(mainColor);
                }
            });
        }
        // 退出按钮特殊色
        btnLogout.setBackground(new Color(220, 53, 69));
        btnLogout.setFont(new Font("微软雅黑", Font.BOLD, 16));
        btnLogout.setHorizontalAlignment(SwingConstants.CENTER); // 横向居中
        btnLogout.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnLogout.setBackground(new Color(200, 35, 51));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnLogout.setBackground(new Color(220, 53, 69));
            }
        });

        lblNavigationTitle = new JLabel("导航菜单");
        lblNavigationTitle.setFont(new Font("微软雅黑", Font.BOLD, 20)); // 更大更醒目
        lblNavigationTitle.setForeground(new Color(41, 128, 185));
        lblNavigationTitle.setHorizontalAlignment(SwingConstants.CENTER); // 横向居中

        contentPanel = new JPanel();
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setLayout(new BorderLayout());
        // 增加内容区内边距和圆角
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(20, 20, 20, 20),
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true)));
        // 初始显示会议室状态界面
        // showContent("会议室状态界面"); // 调用此方法来初始化界面，而不是直接添加JLabel
        // 为了演示，我们先添加一个通用的欢迎标签，实际应由showContent处理
        JLabel welcomeLabel = new JLabel("欢迎使用会议室预订系统", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("微软雅黑", Font.PLAIN, 20));
        contentPanel.add(welcomeLabel, BorderLayout.CENTER);

        // 设置布局 - 左右分栏
        setLayout(new MigLayout("", "[200px][grow]", "[grow]"));

        // "gap unrel" 表示组件间的默认间隙
        // "insets 10" 增加侧边栏的内边距
        JPanel sidebar = new JPanel(
                new MigLayout("wrap 1, fillx, insets 10", "[grow,fill]")); // Simplified layout definition
        sidebar.setBackground(new Color(230, 235, 240)); // 浅灰色背景
        sidebar.add(lblNavigationTitle, "align center, gaptop 5, gapbottom 15"); // 调整标题间
        // 添加导航按钮到侧边栏 (User Group)
        sidebar.add(btnHome, "growx, h 40!"); // 主页按钮放在最上面
        sidebar.add(btnRoomStatus, "growx, h 40!, gaptop 5");
        sidebar.add(btnMyBookings, "growx, h 40!, gaptop 5");
        sidebar.add(btnProfile, "growx, h 40!, gaptop 5");

        // This "pusher" component will create the space between the two groups.
        sidebar.add(new JLabel(), "pushy");

        // 管理员功能按钮 - 根据角色判断是否添加和显示 (Admin Group)
        if (currentUser.isAdmin()) {
            sidebar.add(btnAdminRoomMgmt, "growx, h 40!"); // No large gaptop needed now
            sidebar.add(btnAdminEquipmentMgmt, "growx, h 40!, gaptop 5");
            sidebar.add(btnAdminUserMgmt, "growx, h 40!, gaptop 5");
            sidebar.add(btnAdminSettings, "growx, h 40!, gaptop 5");
        }

        // 将退出按钮推到底部
        sidebar.add(btnLogout, "growx, h 40!, dock south"); // Dock to the absolute bottom
        // 添加组件到窗口
        add(sidebar, "grow");
        add(contentPanel, "grow");

        // 为普通用户按钮添加事件监听器
        btnHome.addActionListener(e -> showContent("主页界面"));
        btnRoomStatus.addActionListener(e -> showContent("会议室状态界面"));
        btnMyBookings.addActionListener(e -> showContent("我的预订界面"));
        btnProfile.addActionListener(e -> showContent("个人信息界面"));

        // 为管理员按钮添加事件监听器
        btnAdminRoomMgmt.addActionListener(e -> showContent("会议室管理界面 (管理员)"));
        btnAdminUserMgmt.addActionListener(e -> showContent("用户管理界面 (管理员)"));
        btnAdminEquipmentMgmt.addActionListener(e -> showContent("设备管理界面 (管理员)"));
        btnAdminSettings.addActionListener(e -> showContent("系统设置界面 (管理员)"));
        btnLogout.addActionListener(e -> {
            // 实际的退出登录逻辑
            int confirmation = JOptionPane.showConfirmDialog(this, "您确定要退出登录吗？", "退出确认", JOptionPane.YES_NO_OPTION);
            if (confirmation == JOptionPane.YES_OPTION) {
                dispose(); // 关闭当前主界面
                // 返回登录界面
                SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
            }
        });

        // 初始化时显示默认界面，例如会议室状态
        showContent("主页界面");
    }

    /**
     * 根据导航选择更新右侧内容面板的显示内容。
     * 此方法作为各个功能模块UI的入口点。
     * 
     * @param panelKey 一个用于识别要加载哪个面板的键或标识符。
     */
    private void showContent(String panelKey) {
        contentPanel.removeAll(); // 移除当前所有组件
        JPanel newPanelToShow = null;

        // 根据 panelKey 创建并加载相应的 JPanel
        switch (panelKey) {
            case "主页界面":
                newPanelToShow = new HomePanel(this::showContent);
                break;
            case "会议室状态界面":
            case "预订会议室界面":
                newPanelToShow = new RoomStatusPanel(currentUser);
                break;
            case "我的预订界面":
                newPanelToShow = new MyBookingsPanel(currentUser);
                break;
            case "个人信息界面":
                newPanelToShow = new UserProfilePanel(currentUser);
                break;
            case "会议室管理界面 (管理员)":
                newPanelToShow = new AdminRoomManagementPanel();
                break;
            case "用户管理界面 (管理员)":
                newPanelToShow = new AdminUserManagementPanel(currentUser);
                break;
            case "设备管理界面 (管理员)":
                if (currentUser.isAdmin()) {
                    newPanelToShow = new AdminEquipmentManagementPanel();
                }
                break;
            case "系统设置界面 (管理员)":
                if (currentUser.isAdmin()) {
                    newPanelToShow = new AdminSettingsPanel(currentUser);
                } else {
                    // Fallback for non-admin users, though they shouldn't see the button
                    JPanel accessDeniedPanel = new JPanel(new GridBagLayout());
                    accessDeniedPanel.add(new JLabel("您没有权限访问此页面。"));
                    newPanelToShow = accessDeniedPanel;
                }
                break;
            default:
                JLabel defaultLabel = new JLabel("未找到对应界面: " + panelKey, SwingConstants.CENTER);
                defaultLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
                defaultLabel.setForeground(Color.RED);
                newPanelToShow = new JPanel(new BorderLayout());
                newPanelToShow.add(defaultLabel, BorderLayout.CENTER);
                break;
        }

        if (newPanelToShow != null) {
            contentPanel.add(newPanelToShow, BorderLayout.CENTER);
        }

        contentPanel.revalidate(); // 使更改生效
        contentPanel.repaint(); // 重绘面板
    }

    public static void main(String[] args) {
        // 在事件调度线程中创建和显示GUI
        SwingUtilities.invokeLater(() -> {
            // For testing MainPage directly, create a dummy user
            User testUser = new User(1, "testadmin", "admin", "test@example.com", "1234567890");
            MainPage mainPageInstance = new MainPage(testUser);
            mainPageInstance.setVisible(true);
        });
    }
}
