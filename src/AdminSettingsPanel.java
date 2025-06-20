import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

/**
 * 管理员设置面板，用于管理员配置系统设置。
 */
public class AdminSettingsPanel extends JPanel {

    private final User currentUser;
    private final UserDAO userDAO;

    private JPasswordField oldPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private JButton saveButton;

    /**
     * 构造函数，初始化管理员系统设置面板。
     */
    public AdminSettingsPanel(User user) {
        this.currentUser = user;
        this.userDAO = new UserDAO();
        initComponents();
    }

    private void initComponents() {
        setLayout(new MigLayout("wrap 2, fillx, insets 20", "[120px][grow,fill]"));

        JLabel titleLabel = new JLabel("更改管理员密码");
        UIStyleUtil.beautifyTitleLabel(titleLabel);
        add(titleLabel, "span 2, wrap, gapbottom 20");

        JLabel userLabel = new JLabel("当前用户名:");
        userLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        JLabel usernameLabel = new JLabel(currentUser.getUsername());
        usernameLabel.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        add(userLabel);
        add(usernameLabel, "wrap, gapbottom 10");

        oldPasswordField = new JPasswordField();
        newPasswordField = new JPasswordField();
        confirmPasswordField = new JPasswordField();
        UIStyleUtil.beautifyPasswordField(oldPasswordField);
        UIStyleUtil.beautifyPasswordField(newPasswordField);
        UIStyleUtil.beautifyPasswordField(confirmPasswordField);
        oldPasswordField.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        newPasswordField.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        confirmPasswordField.setFont(new Font("微软雅黑", Font.PLAIN, 18));

        JLabel oldPwdLabel = new JLabel("旧密码:");
        oldPwdLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        add(oldPwdLabel);
        add(oldPasswordField, "growx, wrap");

        JLabel newPwdLabel = new JLabel("新密码:");
        newPwdLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        add(newPwdLabel);
        add(newPasswordField, "growx, wrap");

        JLabel confirmPwdLabel = new JLabel("确认新密码:");
        confirmPwdLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        add(confirmPwdLabel);
        add(confirmPasswordField, "growx, wrap, gapbottom 20");

        saveButton = new JButton("保存更改");
        UIStyleUtil.beautifyButton(saveButton);
        saveButton.setFont(new Font("微软雅黑", Font.BOLD, 20));
        saveButton.addActionListener(e -> savePasswordChanges());
        add(saveButton, "span 2, align right");

        UIStyleUtil.setMainBackground(this);
    }

    private void savePasswordChanges() {
        String oldPassword = new String(oldPasswordField.getPassword());
        String newPassword = new String(newPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        // Validation
        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "所有密码字段都必须填写。", "输入错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "新密码和确认密码不匹配。", "输入错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (oldPassword.equals(newPassword)) {
            JOptionPane.showMessageDialog(this, "新密码不能与旧密码相同。", "输入错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Disable button to prevent multiple clicks
        saveButton.setEnabled(false);

        new SwingWorker<Boolean, Void>() {
            private String errorMessage = null;

            @Override
            protected Boolean doInBackground() {
                try {
                    return userDAO.changePassword(currentUser.getUserId(), oldPassword, newPassword);
                } catch (Exception e) {
                    errorMessage = e.getMessage();
                    return false;
                }
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        JOptionPane.showMessageDialog(AdminSettingsPanel.this,
                                "密码修改成功！请在下次登录时使用新密码。",
                                "成功", JOptionPane.INFORMATION_MESSAGE);
                        // Clear fields
                        oldPasswordField.setText("");
                        newPasswordField.setText("");
                        confirmPasswordField.setText("");
                    } else {
                        String message = "密码修改失败。";
                        if (errorMessage != null) {
                            message += "\n原因: " + errorMessage;
                        } else {
                            // This case implies old password was incorrect
                            message += "\n请确认您的旧密码是否正确。";
                        }
                        JOptionPane.showMessageDialog(AdminSettingsPanel.this, message, "操作失败",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(AdminSettingsPanel.this,
                            "处理密码修改时发生未知错误: " + e.getMessage(), "严重错误", JOptionPane.ERROR_MESSAGE);
                } finally {
                    // Re-enable button
                    saveButton.setEnabled(true);
                }
            }
        }.execute();
    }
}