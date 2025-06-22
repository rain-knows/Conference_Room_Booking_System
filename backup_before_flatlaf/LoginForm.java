import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import net.miginfocom.swing.*;
import java.sql.SQLException;
import javax.swing.UIManager;

/**
 * A modern, redesigned login form for the Conference Room Booking System.
 * 
 * @author JUSTLIKEZYP (Redesigned by AI Assistant)
 */
public class LoginForm extends JFrame {
    private JTextField nameControl;
    private JPasswordField passControl;

    public LoginForm() {
        initUI();
    }

    private void initUI() {
        setTitle("会议室预订系统");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Main container with a two-column layout
        JPanel mainPanel = new JPanel(new MigLayout("fill, insets 0", "[40%][60%]", "[grow]"));

        // Left branding panel
        JPanel leftPanel = createLeftPanel();

        // Right login form panel
        JPanel rightPanel = createRightPanel();

        mainPanel.add(leftPanel, "grow");
        mainPanel.add(rightPanel, "grow");

        setContentPane(mainPanel);
        pack(); // Pack the components to their preferred sizes
        setSize(900, 550); // Set a modern, wide aspect ratio
        setLocationRelativeTo(null); // Center on screen
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new MigLayout(
                "wrap 1, fill, insets 40", // 1 component per row, fill the panel
                "[center]", // Center align components
                "push[center]10[center]push" // Push content to vertical center
        ));

        // Use a professional blue color
        panel.setBackground(new Color(36, 87, 166));

        JLabel title = new JLabel("高效，智能，便捷");
        title.setFont(new Font("微软雅黑", Font.BOLD, 32));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("企业级会议室预订与管理平台");
        subtitle.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        subtitle.setForeground(new Color(200, 220, 255));

        panel.add(title);
        panel.add(subtitle);

        return panel;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new MigLayout(
                "wrap 1, fillx, insets 50 60 50 60", // insets: top, left, bottom, right
                "[grow]", // Column
                "[]30[]20[]10[]20[]30[]" // Rows with gaps
        ));
        panel.setBackground(Color.WHITE);

        JLabel loginTitle = new JLabel("欢迎回来");
        loginTitle.setFont(new Font("微软雅黑", Font.BOLD, 28));
        loginTitle.setForeground(new Color(50, 50, 50));

        JLabel loginSubtitle = new JLabel("请输入您的凭据以继续");
        loginSubtitle.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        loginSubtitle.setForeground(Color.GRAY);

        nameControl = new JTextField();
        passControl = new JPasswordField();

        // Placeholder text setup
        setupPlaceholder(nameControl, "请输入用户名");
        setupPasswordPlaceholder(passControl, "请输入密码");

        // Set consistent height and font for input fields
        Font fieldFont = new Font("微软雅黑", Font.PLAIN, 15);
        nameControl.setFont(fieldFont);
        passControl.setFont(fieldFont);

        JCheckBox rememberMe = new JCheckBox("记住我");
        rememberMe.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        rememberMe.setOpaque(false);
        rememberMe.setFocusPainted(false);

        JButton loginButton = new JButton("立即登录");
        loginButton.setFont(new Font("微软雅黑", Font.BOLD, 16));
        loginButton.setForeground(Color.WHITE);
        loginButton.setBackground(new Color(52, 152, 219));
        loginButton.setFocusPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                loginButton.setBackground(new Color(41, 128, 185));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                loginButton.setBackground(new Color(52, 152, 219));
            }
        });
        loginButton.addActionListener(this::onLoginButtonClick);

        panel.add(loginTitle);
        panel.add(loginSubtitle);
        panel.add(new JLabel("用户名"), "gaptop 10");
        panel.add(nameControl, "h 45!, growx");
        panel.add(new JLabel("密码"), "gaptop 10");
        panel.add(passControl, "h 45!, growx");
        panel.add(rememberMe, "align left");
        panel.add(loginButton, "h 45!, growx, gaptop 10");

        return panel;
    }

    private void onLoginButtonClick(ActionEvent e) {
        String username = getUsernameInput();
        String password = getPasswordInput();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "用户名和密码不能为空！", "警告", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            UserDAO userDAO = new UserDAO();
            User user = userDAO.checkLogin(username, password);
            if (user != null) {
                this.dispose();
                new MainPage(user).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "用户名或密码错误", "登录失败", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "登录过程中发生数据库错误。", "错误", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private String getUsernameInput() {
        String username = nameControl.getText();
        return username.equals("请输入用户名") ? "" : username;
    }

    private String getPasswordInput() {
        String password = String.valueOf(passControl.getPassword());
        return password.equals("请输入密码") ? "" : password;
    }

    private void setupPlaceholder(JTextField field, String placeholder) {
        field.setText(placeholder);
        field.setForeground(Color.GRAY);
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setForeground(Color.GRAY);
                    field.setText(placeholder);
                }
            }
        });
    }

    private void setupPasswordPlaceholder(JPasswordField field, String placeholder) {
        field.setEchoChar((char) 0);
        field.setText(placeholder);
        field.setForeground(Color.GRAY);
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (String.valueOf(field.getPassword()).equals(placeholder)) {
                    field.setText("");
                    field.setEchoChar('•');
                    field.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (String.valueOf(field.getPassword()).isEmpty()) {
                    field.setEchoChar((char) 0);
                    field.setForeground(Color.GRAY);
                    field.setText(placeholder);
                }
            }
        });
    }

    public static void main(String[] args) {
        // 使用UIStyleUtil初始化全局UI样式
        SwingUtilities.invokeLater(() -> {
            // 初始化全局UI样式（包括FlatLaf IntelliJ主题）
            UIStyleUtil.initializeUI();

            LoginForm loginForm = new LoginForm();
            loginForm.setVisible(true);
        });
    }
}
