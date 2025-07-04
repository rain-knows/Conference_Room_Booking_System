import javax.swing.*;
import java.awt.*;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;

/**
 * UI样式工具类，用于全局主题管理
 * 
 * @author JUSTLIKEZYP
 */
public class UIStyleUtil {

    /**
     * 主题类型枚举
     */
    public enum ThemeType {
        INTELLIJ_LIGHT("IntelliJ Light", FlatIntelliJLaf.class),
        INTELLIJ_DARK("IntelliJ Dark", FlatDarkLaf.class),
        LIGHT("Light", FlatLightLaf.class),
        MAC_LIGHT("Mac Light", FlatMacLightLaf.class),
        MAC_DARK("Mac Dark", FlatMacDarkLaf.class);

        private final String displayName;
        private final Class<?> lafClass;

        ThemeType(String displayName, Class<?> lafClass) {
            this.displayName = displayName;
            this.lafClass = lafClass;
        }

        public String getDisplayName() {
            return displayName;
        }

        public Class<?> getLafClass() {
            return lafClass;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    /**
     * 应用指定的主题
     * 
     * @param themeType 主题类型
     * @return 是否成功应用主题
     */
    public static boolean applyTheme(ThemeType themeType) {
        try {
            LookAndFeel laf = (LookAndFeel) themeType.getLafClass().getDeclaredConstructor().newInstance();
            UIManager.setLookAndFeel(laf);

            // 更新所有已显示的窗口
            updateAllWindows();

            return true;
        } catch (Exception e) {
            System.err.println("应用主题失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 应用默认的macOS Dark主题
     */
    public static void applyDefaultTheme() {
        applyTheme(ThemeType.MAC_DARK);
    }

    /**
     * 更新所有已显示的窗口
     */
    private static void updateAllWindows() {
        SwingUtilities.invokeLater(() -> {
            for (Window window : Window.getWindows()) {
                if (window.isDisplayable()) {
                    SwingUtilities.updateComponentTreeUI(window);
                }
            }
        });
    }

    /**
     * 获取当前主题类型
     * 
     * @return 当前主题类型，如果无法确定则返回默认主题
     */
    public static ThemeType getCurrentTheme() {
        LookAndFeel currentLaf = UIManager.getLookAndFeel();
        String lafName = currentLaf.getClass().getSimpleName();

        for (ThemeType theme : ThemeType.values()) {
            if (theme.getLafClass().getSimpleName().equals(lafName)) {
                return theme;
            }
        }

        return ThemeType.MAC_DARK; // 默认返回macOS Dark
    }

    /**
     * 设置全局字体
     * 
     * @param fontName 字体名称
     * @param fontSize 字体大小
     */
    public static void setGlobalFont(String fontName, int fontSize) {
        Font font = new Font(fontName, Font.PLAIN, fontSize);
        UIManager.put("Button.font", font);
        UIManager.put("Label.font", font);
        UIManager.put("TextField.font", font);
        UIManager.put("TextArea.font", font);
        UIManager.put("Table.font", font);
        UIManager.put("TableHeader.font", font);
        UIManager.put("ComboBox.font", font);
        UIManager.put("CheckBox.font", font);
        UIManager.put("RadioButton.font", font);
        UIManager.put("Menu.font", font);
        UIManager.put("MenuItem.font", font);
        UIManager.put("MenuBar.font", font);
        UIManager.put("TabbedPane.font", font);
        UIManager.put("TitledBorder.font", font);
        UIManager.put("ToolTip.font", font);

        // 更新所有窗口
        updateAllWindows();
    }

    /**
     * 设置默认的全局字体（微软雅黑）
     */
    public static void setDefaultFont() {
        setGlobalFont("微软雅黑", 14);
    }

    /**
     * 初始化应用程序的UI样式
     * 在应用程序启动时调用
     */
    public static void initializeUI() {
        // 设置默认主题为 macOS Dark
        applyTheme(ThemeType.MAC_DARK);

        // 设置默认字体
        setDefaultFont();

        // 设置一些全局UI属性，让FlatLaf主题完全控制
        UIManager.put("Button.arc", 8);
        UIManager.put("Component.arc", 8);
        UIManager.put("TextComponent.arc", 8);

        // 设置表格行高
        UIManager.put("Table.rowHeight", 30);

        // 设置滚动条样式，使用主题默认值
        UIManager.put("ScrollBar.width", 12);
    }

    /**
     * 美化按钮样式 - 使用主题默认样式
     */
    public static void beautifyButton(JButton button) {
        button.setFont(new Font("微软雅黑", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
    }

    /**
     * 美化输入框样式 - 使用主题默认样式
     */
    public static void beautifyTextField(JTextField textField) {
        textField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
    }

    public static void beautifyPasswordField(JPasswordField passField) {
        passField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
    }

    /**
     * 美化标题标签 - 使用主题默认样式
     */
    public static void beautifyTitleLabel(JLabel label) {
        label.setFont(new Font("微软雅黑", Font.BOLD, 24));
    }

    /**
     * 美化普通标签 - 使用主题默认样式
     */
    public static void beautifyLabel(JLabel label) {
        label.setFont(new Font("微软雅黑", Font.PLAIN, 14));
    }

    /**
     * 设置主背景色 - 使用主题默认背景
     */
    public static void setMainBackground(Container container) {
        // 让主题控制背景色，不设置硬编码颜色
    }
}