import javax.swing.*;
import java.awt.*;

public class UIStyleUtil {
    /**
     * 美化按钮样式
     */
    public static void beautifyButton(JButton button) {
        button.setBackground(new Color(52, 152, 219));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("微软雅黑", Font.BOLD, 18));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(41, 128, 185));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(52, 152, 219));
            }
        });
    }

    /**
     * 美化输入框样式
     */
    public static void beautifyTextField(JTextField textField) {
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        textField.setFont(new Font("微软雅黑", Font.PLAIN, 16));
    }

    public static void beautifyPasswordField(JPasswordField passField) {
        passField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        passField.setFont(new Font("微软雅黑", Font.PLAIN, 16));
    }

    /**
     * 美化标题标签
     */
    public static void beautifyTitleLabel(JLabel label) {
        label.setFont(new Font("微软雅黑", Font.BOLD, 28));
        label.setForeground(new Color(52, 73, 94));
    }

    /**
     * 美化普通标签
     */
    public static void beautifyLabel(JLabel label) {
        label.setFont(new Font("微软雅黑", Font.PLAIN, 16));
    }

    /**
     * 设置主背景色
     */
    public static void setMainBackground(Container container) {
        container.setBackground(new Color(245, 248, 252));
    }
}