import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.*;
import net.miginfocom.swing.MigLayout;
import java.sql.SQLException;
import javax.swing.UIManager;

/**
 * A modern, redesigned login form for the Conference Room Booking System.
 * 
 * @author JUSTLIKEZYP (Redesigned by AI Assistant)
 */
public class LoginForm extends JFrame {
    // ====== 常量定义 ======
    private static final String TITLE = "会议室预订系统";
    private static final String PLACEHOLDER_USERNAME = "请输入用户名";
    private static final String PLACEHOLDER_PASSWORD = "请输入密码";
    private static final String LABEL_WELCOME = "欢迎回来";
    private static final String LABEL_SUBTITLE = "请输入您的凭据以继续";
    private static final String LABEL_USERNAME = "用户名";
    private static final String LABEL_PASSWORD = "密码";
    private static final String LABEL_REMEMBER = "记住我";
    private static final String BTN_LOGIN = "立即登录";
    private static final String MSG_EMPTY = "用户名和密码不能为空！";
    private static final String MSG_WARNING = "警告";
    private static final String MSG_LOGIN_FAIL = "用户名或密码错误";
    private static final String MSG_LOGIN_FAIL_TITLE = "登录失败";
    private static final String MSG_DB_ERROR = "登录过程中发生数据库错误。";
    private static final String MSG_ERROR = "错误";
    private static final String LEFT_TITLE = "高效，智能，便捷";
    private static final String LEFT_SUBTITLE = "企业级会议室预订与管理平台";

    // ====== 成员变量 ======
    private JTextField nameControl;
    private JPasswordField passControl;
    private JButton loginButton;
    private JCheckBox rememberMe;

    public LoginForm() {
        initUI();
    }

    private void initUI() {
        setTitle(TITLE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());
        JPanel mainPanel = new JPanel(new MigLayout("fill, insets 0", "[40%][60%]", "[grow]"));
        mainPanel.add(createLeftPanel(), "grow");
        mainPanel.add(createRightPanel(), "grow");
        setContentPane(mainPanel);
        pack();
        setSize(900, 550);
        setLocationRelativeTo(null);
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new MigLayout(
                "wrap 1, fill, insets 40", // 1 component per row, fill the panel
                "[center]", // Center align components
                "push[center]10[center]push" // Push content to vertical center
        ));

        JLabel title = new JLabel(LEFT_TITLE);
        title.setFont(new Font("微软雅黑", Font.BOLD, 32));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel(LEFT_SUBTITLE);
        subtitle.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        subtitle.setForeground(Color.WHITE);

        panel.add(title);
        panel.add(subtitle);

        return panel;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new MigLayout(
                "wrap 1, fillx, insets 50 60 50 60",
                "[grow]",
                "[]30[]20[]10[]20[]30[]"));

        JLabel loginTitle = createLabel(LABEL_WELCOME, 28, true);
        JLabel loginSubtitle = createLabel(LABEL_SUBTITLE, 15, false);

        nameControl = createTextField();
        passControl = createPasswordField();

        // Placeholder text setup
        setupPlaceholder(nameControl, PLACEHOLDER_USERNAME);
        setupPasswordPlaceholder(passControl, PLACEHOLDER_PASSWORD);

        // 回车键响应登录
        nameControl.addActionListener(e -> onLoginButtonClick(null));
        passControl.addActionListener(e -> onLoginButtonClick(null));

        rememberMe = new JCheckBox(LABEL_REMEMBER);
        rememberMe.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        rememberMe.setOpaque(false);
        rememberMe.setFocusPainted(false);

        loginButton = createLoginButton();

        panel.add(loginTitle);
        panel.add(loginSubtitle);
        panel.add(createLabel(LABEL_USERNAME, 15, false), "gaptop 10");
        panel.add(nameControl, "h 45!, growx");
        panel.add(createLabel(LABEL_PASSWORD, 15, false), "gaptop 10");
        panel.add(passControl, "h 45!, growx");
        panel.add(rememberMe, "align left");
        panel.add(loginButton, "h 45!, growx, gaptop 10");

        return panel;
    }

    private JLabel createLabel(String text, int fontSize, boolean bold) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("微软雅黑", bold ? Font.BOLD : Font.PLAIN, fontSize));
        return label;
    }

    private JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        return field;
    }

    private JPasswordField createPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        return field;
    }

    private JButton createLoginButton() {
        JButton btn = new JButton(BTN_LOGIN);
        btn.setFont(new Font("微软雅黑", Font.BOLD, 16));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(this::onLoginButtonClick);
        return btn;
    }

    private void onLoginButtonClick(ActionEvent e) {
        String username = getUsernameInput();
        String password = getPasswordInput();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, MSG_EMPTY, MSG_WARNING, JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            UserDAO userDAO = new UserDAO();
            User user = userDAO.checkLogin(username, password);
            if (user != null) {
                this.dispose();
                new MainPage(user).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, MSG_LOGIN_FAIL, MSG_LOGIN_FAIL_TITLE, JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, MSG_DB_ERROR, MSG_ERROR, JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private String getUsernameInput() {
        String username = nameControl.getText();
        return username.equals(PLACEHOLDER_USERNAME) ? "" : username;
    }

    private String getPasswordInput() {
        String password = String.valueOf(passControl.getPassword());
        return password.equals(PLACEHOLDER_PASSWORD) ? "" : password;
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
