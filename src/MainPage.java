import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

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

    // 面板Key常量
    private static final String PANEL_HOME = "主页界面";
    private static final String PANEL_ROOM_STATUS = "会议室状态界面";
    private static final String PANEL_BOOK_ROOM = "预订会议室界面";
    private static final String PANEL_MY_BOOKINGS = "我的预订界面";
    private static final String PANEL_PROFILE = "个人信息界面";
    private static final String PANEL_ADMIN_ROOM_MGMT = "会议室管理界面 (管理员)";
    private static final String PANEL_ADMIN_USER_MGMT = "用户管理界面 (管理员)";
    private static final String PANEL_ADMIN_EQUIPMENT_MGMT = "设备管理界面 (管理员)";
    private static final String PANEL_ADMIN_SETTINGS = "系统设置界面 (管理员)";

    // 按钮与面板key映射
    private final Map<JButton, String> buttonPanelMap = new HashMap<>();

    public MainPage(User user) {
        this.currentUser = user; // 保存用户
        setTitle("会议室预订系统");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 初始化按钮
        initButtons();
        // 初始化按钮与面板映射
        initButtonPanelMap();
        // 设置布局 - 左右分栏
        setLayout(new MigLayout("", "[200px][grow]", "[grow]"));

        // 初始化侧边栏
        JPanel sidebar = initSidebar();
        // 初始化内容区
        contentPanel = initContentPanel();

        add(sidebar, "grow");
        add(contentPanel, "grow");

        // 注册所有按钮事件
        registerButtonActions();
        btnLogout.addActionListener(e -> {
            int confirmation = JOptionPane.showConfirmDialog(this, "您确定要退出登录吗？", "退出确认", JOptionPane.YES_NO_OPTION);
            if (confirmation == JOptionPane.YES_OPTION) {
                dispose();
                SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
            }
        });
        showContent(PANEL_HOME);
    }

    /**
     * 初始化所有按钮并设置样式
     */
    private void initButtons() {
        btnHome = createNavButton("主页");
        btnRoomStatus = createNavButton("会议室状态");
        btnMyBookings = createNavButton("我的预订");
        btnProfile = createNavButton("个人信息");
        btnAdminRoomMgmt = createNavButton("会议室管理");
        btnAdminUserMgmt = createNavButton("用户管理");
        btnAdminSettings = createNavButton("系统设置");
        btnAdminEquipmentMgmt = createNavButton("设备管理");
        btnLogout = createNavButton("退出登录");
    }

    /**
     * 创建并美化导航按钮
     */
    private JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("微软雅黑", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        return btn;
    }

    /**
     * 初始化按钮与面板key的映射
     */
    private void initButtonPanelMap() {
        buttonPanelMap.put(btnHome, PANEL_HOME);
        buttonPanelMap.put(btnRoomStatus, PANEL_ROOM_STATUS);
        buttonPanelMap.put(btnMyBookings, PANEL_MY_BOOKINGS);
        buttonPanelMap.put(btnProfile, PANEL_PROFILE);
        if (currentUser.isAdmin()) {
            buttonPanelMap.put(btnAdminRoomMgmt, PANEL_ADMIN_ROOM_MGMT);
            buttonPanelMap.put(btnAdminEquipmentMgmt, PANEL_ADMIN_EQUIPMENT_MGMT);
            buttonPanelMap.put(btnAdminUserMgmt, PANEL_ADMIN_USER_MGMT);
            buttonPanelMap.put(btnAdminSettings, PANEL_ADMIN_SETTINGS);
        }
    }

    /**
     * 注册所有导航按钮的事件
     */
    private void registerButtonActions() {
        for (Map.Entry<JButton, String> entry : buttonPanelMap.entrySet()) {
            entry.getKey().addActionListener(e -> showContent(entry.getValue()));
        }
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
            case PANEL_HOME:
                newPanelToShow = new HomePanel(this::showContent);
                break;
            case PANEL_ROOM_STATUS:
            case PANEL_BOOK_ROOM:
                newPanelToShow = new RoomStatusPanel(currentUser);
                break;
            case PANEL_MY_BOOKINGS:
                newPanelToShow = new MyBookingsPanel(currentUser);
                break;
            case PANEL_PROFILE:
                newPanelToShow = new UserProfilePanel(currentUser);
                break;
            case PANEL_ADMIN_ROOM_MGMT:
                newPanelToShow = new AdminRoomManagementPanel();
                break;
            case PANEL_ADMIN_USER_MGMT:
                newPanelToShow = new AdminUserManagementPanel(currentUser);
                break;
            case PANEL_ADMIN_EQUIPMENT_MGMT:
                if (currentUser.isAdmin()) {
                    newPanelToShow = new AdminEquipmentManagementPanel();
                }
                break;
            case PANEL_ADMIN_SETTINGS:
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
                defaultLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
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

    /**
     * 初始化侧边栏
     */
    private JPanel initSidebar() {
        JPanel sidebar = new JPanel(new MigLayout("wrap 1, fillx, insets 10", "[grow,fill]"));
        lblNavigationTitle = new JLabel("导航菜单");
        lblNavigationTitle.setFont(new Font("微软雅黑", Font.BOLD, 18));
        lblNavigationTitle.setHorizontalAlignment(SwingConstants.CENTER);
        sidebar.add(lblNavigationTitle, "align center, gaptop 5, gapbottom 15");
        sidebar.add(btnHome, "growx, h 40!");
        sidebar.add(btnRoomStatus, "growx, h 40!, gaptop 5");
        sidebar.add(btnMyBookings, "growx, h 40!, gaptop 5");
        sidebar.add(btnProfile, "growx, h 40!, gaptop 5");
        sidebar.add(new JLabel(), "pushy");
        if (currentUser.isAdmin()) {
            sidebar.add(btnAdminRoomMgmt, "growx, h 40!");
            sidebar.add(btnAdminEquipmentMgmt, "growx, h 40!, gaptop 5");
            sidebar.add(btnAdminUserMgmt, "growx, h 40!, gaptop 5");
            sidebar.add(btnAdminSettings, "growx, h 40!, gaptop 5");
        }
        sidebar.add(btnLogout, "growx, h 40!, dock south");
        return sidebar;
    }

    /**
     * 初始化内容区
     */
    private JPanel initContentPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JLabel welcomeLabel = new JLabel("欢迎使用会议室预订系统", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        panel.add(welcomeLabel, BorderLayout.CENTER);
        return panel;
    }
}
