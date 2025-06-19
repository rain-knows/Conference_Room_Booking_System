import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import com.intellij.uiDesigner.core.*;
import net.miginfocom.swing.*;
import java.sql.SQLException;
/*
 * Created by JFormDesigner on Thu May 15 10:30:24 CST 2025
 */



/**
 * @author JUSTLIKEZYP
 */
public class LoginForm extends JFrame {
    public LoginForm() {
        initComponents();
        // 设置窗口初始大小
        setSize(600, 450);

        // 加载JDBC驱动程序1
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "无法加载数据库驱动：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void button1(ActionEvent e) {
        // 获取输入的用户名和密码
        String username = nameControl.getText();
        String password = new String(passControl.getPassword());

        if (username.trim().isEmpty() || password.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "用户名和密码不能为空！", "警告", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // 调用DAO进行验证
            UserDAO userDAO = new UserDAO();
            boolean success = userDAO.checkLogin(username, password);
            if (success) {
                JOptionPane.showMessageDialog(this, "登录成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                // TODO: 登录成功后的操作，例如打开主界面等
            } else {
                JOptionPane.showMessageDialog(this, "用户名或密码错误", "登录失败", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "登录过程中发生错误：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        label1 = new JLabel();
        label2 = new JLabel();
        nameControl = new JTextField();
        passControl = new JPasswordField();
        loginbutton = new JButton();
        label4 = new JLabel();

        //======== this ========
        setFont(new Font("Microsoft JhengHei UI", Font.PLAIN, 16));
        var contentPane = getContentPane();
        contentPane.setLayout(null);

        //---- label1 ----
        label1.setText("\u8bf7\u767b\u5f55");
        label1.setFont(new Font("\u534e\u6587\u4e2d\u5b8b", Font.PLAIN, 24));
        label1.setHorizontalAlignment(SwingConstants.CENTER);
        label1.setHorizontalTextPosition(SwingConstants.CENTER);
        contentPane.add(label1);
        label1.setBounds(185, 5, 150, 128);

        //---- label2 ----
        label2.setText("\u8d26\u53f7");
        label2.setHorizontalTextPosition(SwingConstants.CENTER);
        label2.setHorizontalAlignment(SwingConstants.CENTER);
        label2.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 14));
        contentPane.add(label2);
        label2.setBounds(65, 145, 100, 50);

        //---- nameControl ----
        nameControl.setColumns(20);
        nameControl.setAction(null);
        contentPane.add(nameControl);
        nameControl.setBounds(220, 150, 200, 40);

        //---- passControl ----
        passControl.setColumns(20);
        contentPane.add(passControl);
        passControl.setBounds(220, 220, 200, 40);

        //---- loginbutton ----
        loginbutton.setText("\u767b\u5f55");
        loginbutton.addActionListener(e -> button1(e));
        contentPane.add(loginbutton);
        loginbutton.setBounds(200, 310, 140, 59);

        //---- label4 ----
        label4.setText("\u5bc6\u7801");
        label4.setHorizontalTextPosition(SwingConstants.CENTER);
        label4.setHorizontalAlignment(SwingConstants.CENTER);
        label4.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 14));
        contentPane.add(label4);
        label4.setBounds(70, 220, 100, 50);

        {
            // compute preferred size
            Dimension preferredSize = new Dimension();
            for(int i = 0; i < contentPane.getComponentCount(); i++) {
                Rectangle bounds = contentPane.getComponent(i).getBounds();
                preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
            }
            Insets insets = contentPane.getInsets();
            preferredSize.width += insets.right;
            preferredSize.height += insets.bottom;
            contentPane.setMinimumSize(preferredSize);
            contentPane.setPreferredSize(preferredSize);
        }
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    private JLabel label1;
    private JLabel label2;
    private JTextField nameControl;
    private JPasswordField passControl;
    private JButton loginbutton;
    private JLabel label4;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on


    public static void main(String[] args) {
        SwingUtilities.invokeLater(()->{
            LoginForm loginForm=new LoginForm();
            loginForm.setVisible(true);
        });
    }
}
