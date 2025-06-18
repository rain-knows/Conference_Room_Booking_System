import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class MigLayoutDemo extends JFrame {

    public MigLayoutDemo() {
        setTitle("MigLayout 属性演示");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 650);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("基本定位约束", createPositioningPanel());
        tabbedPane.addTab("大小调整约束", createSizingPanel());
        tabbedPane.addTab("对齐与间距", createAlignmentGapPanel());
        tabbedPane.addTab("列行约束", createGridConstraintsPanel());
        tabbedPane.addTab("复杂布局实例", createComplexExamplePanel());

        add(tabbedPane);
        setLocationRelativeTo(null);
    }

    private JPanel createPositioningPanel() {
        JPanel mainPanel = createMainPanel();
        
        // Cell约束示例
        addCellConstraintsExample(mainPanel);
        
        // Wrap约束示例 
        addWrapConstraintsExample(mainPanel);
        
        // Split约束示例
        addSplitConstraintsExample(mainPanel);
        
        // Skip约束示例
        addSkipConstraintsExample(mainPanel);
        
        return mainPanel;
    }

    private JPanel createMainPanel() {
        // 主面板布局约束: 
        // "wrap 2" - 每行2个组件后自动换行
        // "insets 20" - 容器内边距20像素 
        // "gap 20" - 组件间默认间距20像素
        // 列约束: "[grow,fill][grow,fill]" - 定义两列，都允许增长(grow)并填充(fill)可用空间
        // 行约束: "[][]" - 定义两行，默认行为 (高度由内容决定)
        return new JPanel(new MigLayout("wrap 2, insets 20, gap 20", "[grow,fill][grow,fill]", "[][]"));
    }

    private void addCellConstraintsExample(JPanel mainPanel) {
        JPanel panel = new JPanel(new MigLayout("debug"));
        
        // 单元格定位示例
        panel.add(createColorButton("cell 0 0", Color.RED), "cell 0 0");
        panel.add(createColorButton("cell 1 0", Color.GREEN), "cell 1 0");
        panel.add(createColorButton("cell 0 1", Color.BLUE), "cell 0 1");
        panel.add(createColorButton("cell 1 1", Color.YELLOW), "cell 1 1");
        
        // 单元格跨越示例
        panel.add(createColorButton("cell 0 2, span 2 1", Color.ORANGE), "cell 0 2, span 2 1");
        
        addBorderedPanel(mainPanel, panel, "Cell 定位约束 - 指定组件在网格中的位置");
    }

    private void addWrapConstraintsExample(JPanel mainPanel) {
        JPanel panel = new JPanel(new MigLayout("debug"));
        
        // 默认添加
        panel.add(createColorButton("Button 1", Color.RED));
        panel.add(createColorButton("Button 2", Color.GREEN));
        
        // 使用wrap换行
        panel.add(createColorButton("Button 3 (wrap)", Color.BLUE), "wrap");
        panel.add(createColorButton("Button 4", Color.YELLOW));
        
        // 使用wrap 2换行并跳转列
        panel.add(createColorButton("Button 5 (wrap 2)", Color.ORANGE), "wrap 2");
        panel.add(createColorButton("Button 6", Color.PINK));
        
        addBorderedPanel(mainPanel, panel, "Wrap 约束 - 控制组件换行");
    }

    private void addSplitConstraintsExample(JPanel mainPanel) {
        JPanel panel = new JPanel(new MigLayout("debug"));
        
        // 使用split分割单元格
        panel.add(createColorButton("Split 1", Color.RED), "split 3");
        panel.add(createColorButton("Split 2", Color.GREEN));
        panel.add(createColorButton("Split 3", Color.BLUE), "wrap");
        
        // 正常添加
        panel.add(createColorButton("No Split", Color.YELLOW));
        
        addBorderedPanel(mainPanel, panel, "Split 约束 - 多个组件共享单元格");
    }

    private void addSkipConstraintsExample(JPanel mainPanel) {
        JPanel panel = new JPanel(new MigLayout("debug"));
        
        // 正常添加
        panel.add(createColorButton("Button 1", Color.RED));
        panel.add(createColorButton("Button 2", Color.GREEN), "wrap");
        
        // 使用skip跳过单元格
        panel.add(createColorButton("Button 3", Color.BLUE));
        panel.add(createColorButton("Skip 1", Color.YELLOW), "skip 1");
        panel.add(createColorButton("Button 4", Color.ORANGE));
        
        addBorderedPanel(mainPanel, panel, "Skip 约束 - 跳过单元格");
    }

    private JPanel createSizingPanel() {
        // 布局约束与 createPositioningPanel 中的 mainPanel 类似
        JPanel mainPanel = new JPanel(new MigLayout("wrap 2, insets 20, gap 20", "[grow,fill][grow,fill]", "[][]"));

        // 示例1: 固定大小约束
        JPanel sizePanel = new JPanel(new MigLayout("debug"));
        // 组件约束: "width 100" 设置组件宽度为100像素
        sizePanel.add(createColorButton("width 100", Color.RED), "width 100");
        // 组件约束: "height 80" 设置组件高度为80像素, "wrap" 换行
        sizePanel.add(createColorButton("height 80", Color.GREEN), "height 80, wrap");
        // 组件约束: "w 120" (width的缩写) 设置宽度120, "h 50" (height的缩写) 设置高度50
        sizePanel.add(createColorButton("w 120, h 50", Color.BLUE), "w 120, h 50");
        addBorderedPanel(mainPanel, sizePanel, "固定大小约束 - 指定组件的像素宽高");

        // 示例2: Min/Max大小约束
        // 布局约束: "fill" 使面板填充其在父容器中的分配空间
        JPanel minMaxPanel = new JPanel(new MigLayout("debug, fill"));
        // 组件约束: "wmin 100" 最小宽度100, "grow" 允许组件水平和垂直增长以填充单元格
        minMaxPanel.add(createColorButton("wmin 100", Color.RED), "wmin 100, grow");
        // 组件约束: "wmax 150" 最大宽度150, "grow" 允许增长, "wrap" 换行
        minMaxPanel.add(createColorButton("wmax 150", Color.GREEN), "wmax 150, grow, wrap");
        // 组件约束: "hmin 50" 最小高度50, "hmax 80" 最大高度80, "growx" 只允许水平增长
        minMaxPanel.add(createColorButton("hmin 50, hmax 80", Color.BLUE), "hmin 50, hmax 80, growx");
        addBorderedPanel(mainPanel, minMaxPanel, "最小/最大约束 - 限制组件的尺寸范围");

        // 示例3: Grow约束
        JPanel growPanel = new JPanel(new MigLayout("debug, fill"));
        growPanel.add(createColorButton("没有grow", Color.RED), ""); // 无特殊约束
        // 组件约束: "grow" 允许组件在水平和垂直方向上增长以填充其单元格
        growPanel.add(createColorButton("grow", Color.GREEN), "grow, wrap");
        // 组件约束: "growx" 只允许组件在水平方向上增长
        growPanel.add(createColorButton("growx", Color.BLUE), "growx");
        // 组件约束: "growy" 只允许组件在垂直方向上增长
        growPanel.add(createColorButton("growy", Color.YELLOW), "growy");
        addBorderedPanel(mainPanel, growPanel, "Grow约束 - 控制组件随容器变化而扩展");

        // 示例4: SizeGroup约束
        JPanel sizeGroupPanel = new JPanel(new MigLayout("debug"));
        // 组件约束: "sg btn" 将此组件加入名为 "btn" 的尺寸组。同组内的组件将具有相同的宽度和高度（取组内最大首选宽度和最大首选高度）
        sizeGroupPanel.add(createColorButton("SizeGroup btn", Color.RED), "sg btn");
        sizeGroupPanel.add(createColorButton("无SizeGroup", Color.GREEN), "wrap"); // 不在尺寸组中
        sizeGroupPanel.add(createColorButton("SizeGroup btn", Color.BLUE), "sg btn"); // 加入 "btn" 尺寸组
        sizeGroupPanel.add(createColorButton("SizeGroup btn", Color.YELLOW), "sg btn"); // 加入 "btn" 尺寸组
        addBorderedPanel(mainPanel, sizeGroupPanel, "SizeGroup约束 - 使组件保持相同尺寸");

        return mainPanel;
    }

    private JPanel createAlignmentGapPanel() {
        JPanel mainPanel = new JPanel(new MigLayout("wrap 2, insets 20, gap 20", "[grow,fill][grow,fill]", "[][]"));

        // 示例1: 对齐约束
        JPanel alignPanel = new JPanel(new MigLayout("debug, fill"));
        // 组件约束: "align left" 组件在其单元格内左对齐 (水平方向)
        alignPanel.add(createColorButton("align left", Color.RED), "align left");
        // 组件约束: "align center" 组件在其单元格内居中对齐 (水平方向), "wrap" 换行
        alignPanel.add(createColorButton("align center", Color.GREEN), "align center, wrap");
        // 组件约束: "align right" 组件在其单元格内右对齐 (水平方向)
        alignPanel.add(createColorButton("align right", Color.BLUE), "align right");
        // 组件约束: "aligny baseline" 组件在其单元格内基线对齐 (垂直方向)
        alignPanel.add(createColorButton("aligny baseline", Color.YELLOW), "aligny baseline");
        addBorderedPanel(mainPanel, alignPanel, "对齐约束 - 控制组件在单元格内的位置");

        // 示例2: Gap约束
        JPanel gapPanel = new JPanel(new MigLayout("debug"));
        gapPanel.add(createColorButton("无间距", Color.RED));
        // 组件约束: "gapright 20" 在此组件右侧添加20像素的间距, "wrap" 换行
        gapPanel.add(createColorButton("gapright 20", Color.GREEN), "gapright 20, wrap");
        // 组件约束: "gaptop 20" 在此组件上方添加20像素的间距
        gapPanel.add(createColorButton("gaptop 20", Color.BLUE), "gaptop 20");
        // 组件约束: "gapleft 20" 左侧间距20, "gapbottom 20" 底部间距20
        gapPanel.add(createColorButton("gapleft 20, gapbottom 20", Color.YELLOW), "gapleft 20, gapbottom 20");
        addBorderedPanel(mainPanel, gapPanel, "Gap约束 - 控制组件周围的间距");

        // 示例3: Pad约束
        JPanel padPanel = new JPanel(new MigLayout("debug"));
        padPanel.add(createColorButton("无内边距", Color.RED));
        // 组件约束: "pad 10" 组件所有四个方向的内边距都为10像素, "wrap" 换行
        padPanel.add(createColorButton("pad 10", Color.GREEN), "pad 10, wrap");
        // 组件约束: "pad 0 15" 上下内边距0, 左右内边距15
        padPanel.add(createColorButton("padx 15", Color.BLUE), "pad 0 15");
        // 组件约束: "pad 15 0" 上下内边距15, 左右内边距0
        padPanel.add(createColorButton("pady 15", Color.YELLOW), "pad 15 0");
        addBorderedPanel(mainPanel, padPanel, "Pad约束 - 控制组件内部的边距");

        // 示例4: Dock约束
        // Dock约束通常用于将组件停靠在容器的边缘，类似于BorderLayout
        JPanel dockPanel = new JPanel(new MigLayout("debug, fill")); // fill使中心组件能填充
        // 组件约束: "dock north" 组件停靠在顶部
        dockPanel.add(createColorButton("dock north", Color.RED), "dock north");
        // 组件约束: "dock west" 组件停靠在左侧
        dockPanel.add(createColorButton("dock west", Color.GREEN), "dock west");
        // 组件约束: "dock east" 组件停靠在右侧
        dockPanel.add(createColorButton("dock east", Color.BLUE), "dock east");
        // 组件约束: "dock south" 组件停靠在底部
        dockPanel.add(createColorButton("dock south", Color.YELLOW), "dock south");
        // 组件约束: "dock center" 组件放置在中心区域，并填充剩余空间
        dockPanel.add(createColorButton("dock center", Color.PINK), "dock center");
        addBorderedPanel(mainPanel, dockPanel, "Dock约束 - 按边框方位布局(类似BorderLayout)");

        return mainPanel;
    }

    private JPanel createGridConstraintsPanel() {
        JPanel mainPanel = new JPanel(new MigLayout("wrap 2, insets 20, gap 20", "[grow,fill][grow,fill]", "[][]"));

        // 示例1: 列宽约束
        JPanel colWidthPanel = new JPanel(new MigLayout(
                "debug", // 布局约束: 调试模式
                "[100][200][grow]", // 列约束: 第1列宽度100px, 第2列宽度200px, 第3列可增长
                "[]")); // 行约束: 默认行行为
        colWidthPanel.add(createColorButton("宽度:100", Color.RED));
        colWidthPanel.add(createColorButton("宽度:200", Color.GREEN));
        colWidthPanel.add(createColorButton("grow", Color.BLUE));
        addBorderedPanel(mainPanel, colWidthPanel, "列宽约束 - 控制列的宽度");

        // 示例2: 行高约束
        JPanel rowHeightPanel = new JPanel(new MigLayout(
                "debug", // 布局约束
                "[]", // 列约束: 默认列行为
                "[40][60][grow]")); // 行约束: 第1行高度40px, 第2行高度60px, 第3行可增长
        rowHeightPanel.add(createColorButton("高度:40", Color.RED), "wrap");
        rowHeightPanel.add(createColorButton("高度:60", Color.GREEN), "wrap");
        rowHeightPanel.add(createColorButton("grow", Color.BLUE));
        addBorderedPanel(mainPanel, rowHeightPanel, "行高约束 - 控制行的高度");

        // 示例3: 列对齐约束
        JPanel colAlignPanel = new JPanel(new MigLayout(
                "debug", // 布局约束
                "[left][center][right]", // 列约束: 第1列内容左对齐, 第2列中对齐, 第3列右对齐
                "[]")); // 行约束
        colAlignPanel.add(createColorButton("左对齐列", Color.RED));
        colAlignPanel.add(createColorButton("中对齐列", Color.GREEN));
        colAlignPanel.add(createColorButton("右对齐列", Color.BLUE));
        addBorderedPanel(mainPanel, colAlignPanel, "列对齐约束 - 控制列内组件的对齐方式");

        // 示例4: 列行尺寸组
        JPanel sizeGroupGridPanel = new JPanel(new MigLayout(
                "debug", // 布局约束
                "[sg col][sg col]", // 列约束: 两列都属于名为 "col" 的尺寸组 (宽度将一致)
                "[sg row][sg row]")); // 行约束: 两行都属于名为 "row" 的尺寸组 (高度将一致)
        sizeGroupGridPanel.add(createColorButton("SG列1行1", Color.RED));
        sizeGroupGridPanel.add(createColorButton("SG列2行1", Color.GREEN), "wrap");
        sizeGroupGridPanel.add(createColorButton("SG列1行2", Color.BLUE));
        sizeGroupGridPanel.add(createColorButton("SG列2行2", Color.YELLOW));
        addBorderedPanel(mainPanel, sizeGroupGridPanel, "尺寸组约束 - 使列/行尺寸保持一致");

        return mainPanel;
    }

    private JPanel createComplexExamplePanel() {
        // 布局约束: "wrap 1" 每行1个组件, "insets 20" 内边距, "fill" 使面板填充其分配空间
        // 列约束: "[grow,fill]" 只有一列，允许增长和填充
        // 行约束: "[][grow,fill][]"" 三行，第一行和第三行高度由内容决定，第二行允许增长和填充
        JPanel mainPanel = new JPanel(new MigLayout("wrap 1, insets 20, fill", "[grow,fill]", "[][grow,fill][]"));

        // 示例1: 登录表单
        JPanel loginPanel = new JPanel(new MigLayout(
                "fillx, insets 10", // 布局约束: "fillx" 水平填充, "insets 10" 内边距10
                "[right][grow,fill]", // 列约束: 第1列右对齐标签, 第2列输入框可增长并填充
                "[]10[]10[]")); // 行约束: 三行，行之间间距10像素
        // 组件约束: "align right" 标签右对齐
        loginPanel.add(new JLabel("用户名:"), "align right");
        // 组件约束: "growx" 输入框水平增长, "wrap" 换行
        loginPanel.add(new JTextField("user123"), "growx, wrap");
        loginPanel.add(new JLabel("密码:"), "align right");
        loginPanel.add(new JPasswordField("password"), "growx, wrap");
        // 组件约束: "skip" 跳过第一列, "split 2" 当前单元格分割成2部分, "align right" 按钮组右对齐
        loginPanel.add(new JButton("登录"), "skip, split 2, align right");
        loginPanel.add(new JButton("取消"), ""); // 占用分割的第二部分
        addBorderedPanel(mainPanel, loginPanel, "表单布局实例 - 登录表单");

        // 示例2: 仪表板布局
        JPanel dashboardPanel = new JPanel(new MigLayout(
                "fill, insets 5, gap 5", // 布局约束: "fill" 填充, "insets 5" 内边距5, "gap 5" 组件间距5
                "[grow,fill][grow,fill]", // 列约束: 两列均可增长和填充
                "[grow,fill]10[grow,fill]")); // 行约束: 两行均可增长和填充，行间距10

        JPanel statsPanel = new JPanel(); // 内部可以使用其他布局或简单添加组件
        statsPanel.setBackground(new Color(230, 240, 255));
        JLabel statsLabel = new JLabel("统计面板", JLabel.CENTER);
        statsLabel.setFont(new Font("Dialog", Font.BOLD, 14));
        statsPanel.add(statsLabel);
        // 组件约束: "grow" 使 statsPanel 在其单元格内增长填充
        dashboardPanel.add(statsPanel, "grow");

        JPanel chartPanel = new JPanel();
        chartPanel.setBackground(new Color(255, 240, 240));
        JLabel chartLabel = new JLabel("图表面板", JLabel.CENTER);
        chartLabel.setFont(new Font("Dialog", Font.BOLD, 14));
        chartPanel.add(chartLabel);
        // 组件约束: "grow" 增长填充, "wrap" 换行
        dashboardPanel.add(chartPanel, "grow, wrap");

        // controlPanel 内部也使用 MigLayout
        JPanel controlPanel = new JPanel(new MigLayout("insets 5, wrap 2", "[][]", "[][]"));
        controlPanel.setBackground(new Color(240, 255, 240));
        controlPanel.add(new JButton("按钮1"));
        controlPanel.add(new JButton("按钮2"));
        controlPanel.add(new JCheckBox("选项1"));
        controlPanel.add(new JCheckBox("选项2"));
        dashboardPanel.add(controlPanel, "grow"); // controlPanel 在 dashboardPanel 中增长

        JPanel statusPanel = new JPanel(new BorderLayout()); // statusPanel 使用 BorderLayout
        statusPanel.setBackground(new Color(255, 255, 230));
        JTextArea statusArea = new JTextArea("状态信息...\n系统运行正常\n所有服务已启动");
        statusPanel.add(new JScrollPane(statusArea), BorderLayout.CENTER);
        dashboardPanel.add(statusPanel, "grow"); // statusPanel 在 dashboardPanel 中增长

        addBorderedPanel(mainPanel, dashboardPanel, "复杂布局实例 - 仪表板布局");

        // 示例3: 说明文本
        JPanel descPanel = new JPanel(new MigLayout("insets 10")); // 布局约束: 内边距10
        JTextArea descText = new JTextArea(
                "MigLayout是一个强大而灵活的布局管理器，通过三种类型的约束控制布局：\n" +
                        "1. 布局约束 - 控制整个布局的行为\n" +
                        "2. 列/行约束 - 控制列和行的行为\n" +
                        "3. 组件约束 - 控制单个组件的行为\n\n" +
                        "通过组合这些约束，可以创建出几乎任何类型的复杂界面。");
        descText.setEditable(false);
        descText.setBackground(mainPanel.getBackground());
        descPanel.add(descText); // JTextArea 默认会占据其单元格
        mainPanel.add(descPanel); // descPanel 添加到 mainPanel

        return mainPanel;
    }

    private JButton createColorButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setOpaque(true);
        return button;
    }

    private void addBorderedPanel(JPanel parent, JPanel child, String title) {
        child.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                title,
                TitledBorder.LEFT,
                TitledBorder.TOP));
        parent.add(child);
    }

    public static void main(String[] args) {
        try {
            // 设置本地系统外观
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new MigLayoutDemo().setVisible(true);
        });
    }
}
