import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

/**
 * 用户个人资料面板，用于用户查看和编辑个人信息。
 * 此面板将展示用户的基本信息，并提供编辑功能。
 */
public class UserProfilePanel extends JPanel {

    private User currentUser;
    private UserDAO userDAO;

    // UI Components
    private JTextField usernameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JPasswordField oldPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;

    private JButton saveProfileButton;
    private JButton changePasswordButton;

    /**
     * 构造函数，初始化用户个人信息面板。
     */
    public UserProfilePanel(User user) {
        this.currentUser = user;
        this.userDAO = new UserDAO();
        initComponents();
        populateUserData();
    }

    private void initComponents() {
        setLayout(new MigLayout("fillx, insets 30 50 30 50", "[grow,fill]", "[]20[]20[]"));

        // 主标题
        JLabel titleLabel = new JLabel("个人信息");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        add(titleLabel, "span, center, wrap");

        JPanel mainPanel = new JPanel(new MigLayout("fillx, insets 0", "[grow,fill][grow,fill]", "[grow]"));
        mainPanel.setOpaque(false);

        // --- Profile Information Panel ---
        JPanel profilePanel = new JPanel(new MigLayout("wrap 2, fillx", "[100px][grow, fill]"));
        profilePanel.setBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(UIManager.getColor("Panel.border"), 1, true),
                        "基本信息", 0, 0, new Font("微软雅黑", Font.BOLD, 16)));

        JLabel usernameLabel = new JLabel("用户名:");
        usernameLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        usernameField = new JTextField();
        usernameField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        usernameField.setEditable(false);
        profilePanel.add(usernameLabel);
        profilePanel.add(usernameField, "growx");

        JLabel emailLabel = new JLabel("邮箱:");
        emailLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        emailField = new JTextField();
        emailField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        profilePanel.add(emailLabel);
        profilePanel.add(emailField, "growx");

        JLabel phoneLabel = new JLabel("电话:");
        phoneLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        phoneField = new JTextField();
        phoneField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        profilePanel.add(phoneLabel);
        profilePanel.add(phoneField, "growx");

        saveProfileButton = new JButton("保存更改");
        JButton resetProfileButton = new JButton("重置");
        saveProfileButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        resetProfileButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        saveProfileButton.addActionListener(e -> saveProfileChanges());
        resetProfileButton.addActionListener(e -> populateUserData());
        profilePanel.add(saveProfileButton, "align right, gaptop 10");
        profilePanel.add(resetProfileButton, "align left, gaptop 10");

        // --- Change Password Panel ---
        JPanel passwordPanel = new JPanel(new MigLayout("wrap 2, fillx", "[100px][grow, fill]"));
        passwordPanel.setBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(UIManager.getColor("Panel.border"), 1, true),
                        "修改密码", 0, 0, new Font("微软雅黑", Font.BOLD, 16)));

        JLabel oldPwdLabel = new JLabel("旧密码:");
        oldPwdLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        oldPasswordField = new JPasswordField();
        oldPasswordField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        passwordPanel.add(oldPwdLabel);
        passwordPanel.add(oldPasswordField, "growx");

        JLabel newPwdLabel = new JLabel("新密码:");
        newPwdLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        newPasswordField = new JPasswordField();
        newPasswordField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        passwordPanel.add(newPwdLabel);
        passwordPanel.add(newPasswordField, "growx");

        JLabel confirmPwdLabel = new JLabel("确认新密码:");
        confirmPwdLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        confirmPasswordField = new JPasswordField();
        confirmPasswordField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        passwordPanel.add(confirmPwdLabel);
        passwordPanel.add(confirmPasswordField, "growx");

        changePasswordButton = new JButton("确认修改密码");
        changePasswordButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        changePasswordButton.addActionListener(e -> changePassword());
        passwordPanel.add(changePasswordButton, "span 2, align right, gaptop 10");

        mainPanel.add(profilePanel, "growx, gapright 30");
        mainPanel.add(passwordPanel, "growx");
        add(mainPanel, "span, growx");
    }

    private void populateUserData() {
        usernameField.setText(currentUser.getUsername());
        emailField.setText(currentUser.getEmail());
        phoneField.setText(currentUser.getPhone());
    }

    private void saveProfileChanges() {
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();

        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "邮箱不能为空。", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return userDAO.updateUserProfile(currentUser.getUserId(), email, phone);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(UserProfilePanel.this, "个人资料更新成功！", "成功",
                                JOptionPane.INFORMATION_MESSAGE);
                        // 更新 currentUser 对象中的信息
                        currentUser = new User(currentUser.getUserId(), currentUser.getUsername(),
                                currentUser.getRole(), email, phone, currentUser.isActive());
                    } else {
                        JOptionPane.showMessageDialog(UserProfilePanel.this, "个人资料更新失败。", "错误",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(UserProfilePanel.this, "更新失败: " + e.getMessage(), "错误",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void changePassword() {
        String oldPassword = new String(oldPasswordField.getPassword());
        String newPassword = new String(newPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "所有密码字段都不能为空。", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "新密码和确认密码不匹配。", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (newPassword.length() < 6) { // 简单的密码强度检查
            JOptionPane.showMessageDialog(this, "新密码长度不能少于6位。", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return userDAO.changePassword(currentUser.getUserId(), oldPassword, newPassword);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(UserProfilePanel.this, "密码修改成功！", "成功",
                                JOptionPane.INFORMATION_MESSAGE);
                        // 清空密码字段
                        oldPasswordField.setText("");
                        newPasswordField.setText("");
                        confirmPasswordField.setText("");
                    } else {
                        JOptionPane.showMessageDialog(UserProfilePanel.this, "旧密码错误，密码修改失败。", "错误",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(UserProfilePanel.this, "修改密码失败: " + e.getMessage(), "错误",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}