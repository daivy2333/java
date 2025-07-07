package src;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.filechooser.FileNameExtensionFilter; 
import javax.swing.border.TitledBorder;
public class Main {
    private JButton backButton;
    // 创建一个任务管理器实例
    private static final TaskManager manager = new TaskManager();
    // 创建一个文本区域用于显示任务
    private JTextArea taskDisplayArea;
    // 创建一个背景面板
    private BackgroundPanel mainPanel; 

    

    public static void main(String[] args) {
        // 使用SwingUtilities.invokeLater方法在事件调度线程中运行GUI创建代码
        SwingUtilities.invokeLater(() -> new Main().createAndShowGUI());
    }
    
    // 创建一个背景面板类
    private static class BackgroundPanel extends JPanel {
        // 创建一个背景图片变量
        private Image backgroundImage;

        // 重写paintComponent方法，用于绘制背景图片
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, this.getWidth(), this.getHeight(), this);
            }
        }

        // 设置背景图片的方法
        public void setBackgroundImage(Image image) {
            this.backgroundImage = image;
            repaint();
        }
    }
    // 创建并显示GUI的方法
    private void createAndShowGUI() {
        // 创建一个JFrame窗口
        JFrame frame = new JFrame("工作助手");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null); 
        
        
        // 创建一个背景面板
        mainPanel = new BackgroundPanel();
        mainPanel.setLayout(new BorderLayout());
        frame.setContentPane(mainPanel); 
        try {
            // 使用 getResource 从项目的资源路径中获取图片
            // 路径前的 "/" 代表从 classpath 的根目录开始查找
            java.net.URL imageUrl = Main.class.getResource("/imgs/d9f240fa64bf2243f82b790a53778f9.jpg");
            if (imageUrl != null) {
                ImageIcon icon = new ImageIcon(imageUrl);
                // 调用我们之前写的图像处理方法，使其变暗
                Image processedImage = createProcessedImage(icon.getImage(), 0.4f);
                mainPanel.setBackgroundImage(processedImage);
            } else {
                // 如果图片没找到，给一个提示
                System.err.println("错误：无法找到默认背景图片！请检查路径和文件结构。");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "加载默认背景失败。", "错误", JOptionPane.ERROR_MESSAGE);
        }
        
        // 创建一个文本区域用于显示任务
        taskDisplayArea = new JTextArea();
        taskDisplayArea.setEditable(false);
        taskDisplayArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        taskDisplayArea.setOpaque(false); 
        taskDisplayArea.setForeground(new Color(0, 51, 102)); 
        refreshRecommendedTasks();

        // 创建一个滚动面板，用于显示任务
        JScrollPane scrollPane = new JScrollPane(taskDisplayArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(135, 206, 250), 2), 
                "推荐任务（按重要性排序）",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 16),
                new Color(0, 51, 102) 
        ));
        scrollPane.setOpaque(false); 
        scrollPane.getViewport().setOpaque(false); 

        // 将滚动面板添加到背景面板中
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // 创建一个按钮面板，用于放置按钮
        JPanel buttonPanel = new JPanel(new GridLayout(1, 7, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.setOpaque(false); 
        JPanel topButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topButtonPanel.setOpaque(false);
        
        // 设置按钮的背景色和前景色
        Color buttonColor = new Color(135, 206, 250); 
        Color buttonTextColor = new Color(0, 51, 102); 

        // 创建按钮并添加到按钮面板中
        JButton addButton = createStyledButton("添加任务", buttonColor, buttonTextColor);
        JButton completeButton = createStyledButton("完成任务", buttonColor, buttonTextColor);
        JButton viewAllButton = createStyledButton("所有任务", buttonColor, buttonTextColor);
        JButton summaryButton = createStyledButton("月度总结", buttonColor, buttonTextColor);
        JButton refreshButton = createStyledButton("刷新推荐", buttonColor, buttonTextColor);
        JButton setBackgroundButton = createStyledButton("设置背景", buttonColor, buttonTextColor); 
        JButton exitButton = createStyledButton("退出", buttonColor, buttonTextColor);
        JButton filterByCategoryButton = createStyledButton("按类别筛选", buttonColor, buttonTextColor);

        // 为按钮添加事件监听器
        addButton.addActionListener(e -> showAddTaskDialog());
        completeButton.addActionListener(e -> showCompleteTaskDialog());
        viewAllButton.addActionListener(e -> showTaskList(true));
        summaryButton.addActionListener(e -> showMonthlySummary());
        refreshButton.addActionListener(e -> refreshRecommendedTasks());
        setBackgroundButton.addActionListener(e -> chooseBackgroundImage()); 
        exitButton.addActionListener(e -> System.exit(0));
        filterByCategoryButton.addActionListener(e -> showCategoryFilterDialog());

        // 将按钮添加到按钮面板中
        // 在按钮面板创建代码部分添加
        
        topButtonPanel.add(filterByCategoryButton);
        buttonPanel.add(addButton);
        buttonPanel.add(completeButton);
        buttonPanel.add(viewAllButton);
        buttonPanel.add(summaryButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(setBackgroundButton); 
        buttonPanel.add(exitButton);

        // 将按钮面板添加到背景面板中
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        mainPanel.add(topButtonPanel, BorderLayout.NORTH);

        // 设置窗口可见
        frame.setVisible(true);
    }

    
    // 创建一个带有背景色和前景色的按钮
    private JButton createStyledButton(String text, Color bgColor, Color fgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFocusPainted(false); 
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        return button;
    }

    // 刷新推荐任务的方法
    private void refreshRecommendedTasks() {
        // 获取待办任务列表，并按重要性排序
        List<Task> list = manager.getPendingTasksSortedByImportance();
        if (list.isEmpty()) {
            // 如果没有待办任务，显示提示信息
            taskDisplayArea.setText("暂无推荐任务。");
        } else {
            // 如果有待办任务，将任务信息显示在文本区域中
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < list.size(); i++) {
                Task task = list.get(i);
                sb.append(i + 1).append(". ")
                        .append("名称：").append(task.getName()).append("\n")
                        .append("内容：").append(task.getContent()).append("\n")
                        .append("截止：").append(task.getDeadline().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append("\n")
                        .append("重要性：").append(task.getImportance()).append("\n")  
                        .append("主观权重：").append(task.getBasePriority()).append("\n")  
                        .append("类别：").append(task.getCategory()).append("\n") // 新增显示类别
                        .append("--------------\n");
            }
            taskDisplayArea.setText(sb.toString());
        }
    }

    private void refreshRecommendedTasks(String category) {
    if (backButton != null && backButton.getParent() != null) {
        mainPanel.remove(backButton.getParent());
    }
    List<Task> list;
    
    if (category == null || category.isEmpty()) {
        list = manager.getPendingTasksSortedByImportance();
    } else {
        list = manager.getTasksByCategory(category);
    }
    
    if (list.isEmpty()) {
        taskDisplayArea.setText("没有找到符合条件的任务。");
    } else {
        StringBuilder sb = new StringBuilder();
        if (category != null && !category.isEmpty()) {
            sb.append("类别: ").append(category).append("\n\n");
        }
        
        for (int i = 0; i < list.size(); i++) {
            Task task = list.get(i);
            sb.append(i + 1).append(". ")
              .append("名称：").append(task.getName()).append("\n")
              .append("内容：").append(task.getContent()).append("\n")
              .append("截止：").append(task.getDeadline().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append("\n")
              .append("重要性：").append(task.computeImportance()).append("\n")
              .append("类别：").append(task.getCategory()).append("\n")
              .append("--------------\n");
        }
        taskDisplayArea.setText(sb.toString());
        if (category != null && !category.isEmpty()) {
        // 创建返回按钮
        backButton = createStyledButton("返回主视图", 
                new Color(135, 206, 250), 
                new Color(0, 51, 102));
        
        backButton.addActionListener(e -> refreshRecommendedTasks(null));
        
        // 创建按钮容器
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        buttonPanel.add(backButton);
        
        // 将按钮添加到主面板底部
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        mainPanel.revalidate();
        mainPanel.repaint();
    }}
}

    // 显示添加任务对话框的方法
    private void showAddTaskDialog() {
        // 创建输入框
        JTextField nameField = new JTextField();
        JTextField contentField = new JTextField();
        JTextField deadlineField = new JTextField(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))); 
        JTextField priorityField = new JTextField("5");

        // 创建输入面板
        JPanel inputPanel = new JPanel(new GridLayout(0, 1));
        inputPanel.add(new JLabel("任务名称:"));
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("任务内容:"));
        inputPanel.add(contentField);
        inputPanel.add(new JLabel("截止时间 (yyyy-MM-ddTHH:mm):"));
        inputPanel.add(deadlineField);
        inputPanel.add(new JLabel("初始重要性 (1-10):"));
        inputPanel.add(priorityField);



         // 添加类别选择组件
        JComboBox<String> categoryCombo = new JComboBox<>(new Vector<>(Task.CATEGORIES));
        categoryCombo.setSelectedItem("其他"); // 默认值

        // 修改输入面板
        inputPanel.add(new JLabel("任务类别:"));
        inputPanel.add(categoryCombo);

        // 设置对话框的背景色
        UIManager.put("Panel.background", new Color(204, 229, 255));
        UIManager.put("OptionPane.background", new Color(204, 229, 255));
        UIManager.put("Label.background", new Color(204, 229, 255));

        // 显示对话框
        int result = JOptionPane.showConfirmDialog(null, inputPanel, "添加任务", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                // 获取输入的任务信息
                String name = nameField.getText();
                String content = contentField.getText();
                LocalDateTime deadline = LocalDateTime.parse(deadlineField.getText());
                int priority = Integer.parseInt(priorityField.getText());
                Map<String, Integer> tags = new HashMap<>();
                tags.put("默认", 0);
                String category = (String) categoryCombo.getSelectedItem();
            
            // 修改任务创建
                Task task = new Task(name, content, deadline, priority, tags, category);
                // 将任务添加到任务管理器中
                manager.addTask(task);
                refreshRecommendedTasks();
                JOptionPane.showMessageDialog(null, "任务添加成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "输入格式错误，请重试。", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
        
        UIManager.put("Panel.background", null);
        UIManager.put("OptionPane.background", null);
        UIManager.put("Label.background", null);
    }

    private void showTaskList(boolean showAll) {
        // 根据showAll参数，获取所有任务或待办任务列表
        List<Task> list = showAll ? manager.getAllTasks() : manager.getPendingTasksSortedByImportance();
        // 如果任务列表为空，则弹出提示框
        if (list.isEmpty()) {
            JOptionPane.showMessageDialog(null, "暂无任务", "任务列表", JOptionPane.INFORMATION_MESSAGE);
        } else {
            // 否则，将任务列表转换为字符串
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < list.size(); i++) {
                sb.append(i + 1).append(". ").append(list.get(i)).append("\n");
            }
            // 创建一个文本区域，并将任务列表字符串设置为文本区域的内容
            JTextArea textArea = new JTextArea(sb.toString());
            // 设置文本区域不可编辑
            textArea.setEditable(false);
            // 设置文本区域自动换行
            textArea.setLineWrap(true);
            // 设置文本区域换行方式为单词换行
            textArea.setWrapStyleWord(true);
            // 让文本区域也使用一个柔和的背景色
            textArea.setBackground(new Color(230, 245, 255));
            textArea.setForeground(new Color(0, 51, 102));

            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(500, 300));

            // --- 新增：为弹窗中的滚动面板设置天蓝色边框 ---
            Color skyBlue = new Color(135, 206, 250);
            scrollPane.setBorder(BorderFactory.createLineBorder(skyBlue, 2));
            // --- 新增代码结束 ---

            JOptionPane.showMessageDialog(null, scrollPane, "任务列表", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // 显示完成任务对话框
    private void showCompleteTaskDialog() {
        // 获取待办任务列表，按重要性排序
        List<Task> list = manager.getPendingTasksSortedByImportance();
        // 如果列表为空，则显示提示信息
        if (list.isEmpty()) {
            JOptionPane.showMessageDialog(null, "暂无待办任务", "完成任务", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        // 将任务列表转换为字符串数组
        String[] taskArray = list.stream().map(Task::toString).toArray(String[]::new);
        // 显示选择对话框，让用户选择已完成任务
        String selected = (String) JOptionPane.showInputDialog(null, "选择已完成任务:", "完成任务",
                JOptionPane.QUESTION_MESSAGE, null, taskArray, taskArray[0]);

        // 如果用户选择了任务
        if (selected != null) {
            // 遍历任务列表
            for (int i = 0; i < list.size(); i++) {
                // 如果用户选择的任务与列表中的任务相同
                if (selected.equals(list.get(i).toString())) {
                    // 修复：将任务对象 list.get(i) 修改为它的索引 i
                    manager.markTaskAsCompleted(i); // <-- 这里是修改后的代码

                    // 刷新推荐任务
                    refreshRecommendedTasks();
                    // 显示提示信息
                    JOptionPane.showMessageDialog(null, "任务归档成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                    break; // 找到并处理后即可退出循环
                }
            }
        }
    }

    /**
     * 创建一个带有半透明黑色遮罩的已处理图像，以提高文本可读性。
     * @param originalImage 原始图片
     * @param alpha 透明度（0.0f 完全透明，1.0f 完全不透明）。建议值为 0.3f 到 0.6f。
     * @return 处理后的新图片
     */
    private Image createProcessedImage(Image originalImage, float alpha) {
        // 确保原始图像已完全加载，以便获取其宽度和高度
        int width = originalImage.getWidth(null);
        int height = originalImage.getHeight(null);
        if (width == -1 || height == -1) {
            // 如果图像尚未加载，则无法处理，直接返回原图
            return originalImage;
        }

        // 创建一个新的缓冲图像（BufferedImage），它支持ARGB（包含透明度）颜色模型
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        // 获取此缓冲图像的Graphics2D绘图上下文
        Graphics2D g2d = bufferedImage.createGraphics();

        // 1. 将原始图像绘制到新的缓冲图像上
        g2d.drawImage(originalImage, 0, 0, null);

        // 2. 设置绘图的合成规则（Composite），SRC_OVER是默认的覆盖模式
        // alpha值决定了接下来绘制内容的透明度
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        // 3. 设置遮罩颜色为黑色
        g2d.setColor(Color.WHITE);

        // 4. 在整个图像上绘制一个填充矩形作为遮罩
        g2d.fillRect(0, 0, width, height);

        // 5. 释放绘图资源
        g2d.dispose();

        // 返回处理完成的新图像
        return bufferedImage;
    }
    // New method to choose background image
    private void chooseBackgroundImage() {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "图片文件", "jpg", "jpeg", "png", "gif", "bmp"
        );
        fileChooser.setFileFilter(filter);

        int result = fileChooser.showOpenDialog(mainPanel);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                File selectedFile = fileChooser.getSelectedFile();
                ImageIcon icon = new ImageIcon(selectedFile.getAbsolutePath());

                // --- 新增代码 ---
                // 调用辅助方法创建一张变暗的图片，以增强文字的可读性。
                // 这里的 0.4f 代表遮罩的不透明度为40%，你可以根据需要调整这个值。
                Image processedImage = createProcessedImage(icon.getImage(), 0.6f);
                // --- 新增代码结束 ---

                // 将处理后的图片设置为背景
                mainPanel.setBackgroundImage(processedImage);

            } catch (Exception e) {
                // 更新错误提示信息
                JOptionPane.showMessageDialog(mainPanel, "无法加载或处理图片：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    // 创建趋势图面板
private JPanel createTrendPanel(Map<YearMonth, List<Task>> monthlyTasks) {
    JPanel panel = new JPanel() {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            drawTrendChart(g, monthlyTasks);
        }
    };
    panel.setPreferredSize(new Dimension(600, 200));
    panel.setBorder(BorderFactory.createTitledBorder("完成趋势图"));
    panel.setBackground(new Color(230, 245, 255));
    
    return panel;
}

// 绘制趋势图
private void drawTrendChart(Graphics g, Map<YearMonth, List<Task>> monthlyTasks) {
    Graphics2D g2d = (Graphics2D) g;
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    
    // 获取绘制区域尺寸 - 现在通过 Graphics 对象获取
    int width = g.getClipBounds().width;
    int height = g.getClipBounds().height;
    
    // 如果获取失败则使用默认尺寸
    if (width <= 0 || height <= 0) {
        width = 600;
        height = 200;
    }
    
    int padding = 40;
    int chartWidth = width - 2 * padding;
    int chartHeight = height - 2 * padding;
    
    // 绘制坐标轴
    g2d.setColor(Color.BLACK);
    g2d.drawLine(padding, height - padding, width - padding, height - padding); // X轴
    g2d.drawLine(padding, height - padding, padding, padding); // Y轴
    
    // 获取最近6个月的数据（按时间顺序排序）
    List<YearMonth> recentMonths = monthlyTasks.keySet().stream()
            .sorted() // 按时间顺序排序
            .limit(6)  // 仅取最近6个月
            .collect(Collectors.toList());
    
    if (recentMonths.isEmpty()) {
        g2d.drawString("无数据", width / 2 - 20, height / 2);
        return;
    }
    
    // 找出最大值用于计算比例
    int maxCount = recentMonths.stream()
            .mapToInt(ym -> monthlyTasks.get(ym).size())
            .max()
            .orElse(1);
    
    // 绘制刻度
    g2d.setFont(new Font("SansSerif", Font.PLAIN, 10));
    for (int i = 0; i <= 5; i++) {
        int y = height - padding - i * chartHeight / 5;
        g2d.drawLine(padding - 5, y, padding, y);
        g2d.drawString(String.valueOf(maxCount * i / 5), padding - 35, y + 5);
    }
    
    // 绘制月份标签和柱子
    int barWidth = chartWidth / (recentMonths.size() * 2);
    int spacing = barWidth;
    
    for (int i = 0; i < recentMonths.size(); i++) {
        YearMonth month = recentMonths.get(i);
        int taskCount = monthlyTasks.get(month).size();
        int barHeight = (int) ((double) taskCount / maxCount * chartHeight);
        int x = padding + i * (barWidth + spacing);
        
        // 绘制柱子
        g2d.setColor(new Color(70, 130, 180)); // 钢蓝色
        g2d.fillRect(x, height - padding - barHeight, barWidth, barHeight);
        
        // 绘制数值
        g2d.setColor(Color.BLACK);
        g2d.drawString(String.valueOf(taskCount), x + barWidth / 2 - 5, height - padding - barHeight - 5);
        
        // 绘制月份标签
        String monthLabel = month.format(DateTimeFormatter.ofPattern("yy-MM"));
        g2d.drawString(monthLabel, x + barWidth / 2 - 10, height - padding + 15);
    }
    
    // 绘制标题
    g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
    g2d.drawString("近6个月任务完成情况", width / 2 - 50, padding / 2);
}

    private void showMonthlySummary() {
    // 获取月度任务数据
    Map<YearMonth, List<Task>> monthlyTasks = SummaryAnalyzer.getMonthlyTasks();
    
    // 创建主面板
    JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
    mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    mainPanel.setBackground(new Color(230, 245, 255)); // 浅蓝色背景
    
    // 创建顶部总结面板
    JPanel summaryPanel = new JPanel(new GridLayout(0, 1, 5, 5));
    summaryPanel.setBorder(BorderFactory.createTitledBorder("月度总结"));
    summaryPanel.setBackground(new Color(180, 220, 255)); // 稍深的蓝色
    
    // 添加总结数据
    JTextArea summaryText = new JTextArea(SummaryAnalyzer.generateMonthlySummary());
    summaryText.setEditable(false);
    summaryText.setFont(new Font("Monospaced", Font.PLAIN, 14));
    summaryText.setBackground(summaryPanel.getBackground());
    summaryPanel.add(summaryText);
    
    mainPanel.add(summaryPanel, BorderLayout.NORTH);
    
    // 创建月度任务面板
    JPanel monthsPanel = new JPanel(new BorderLayout(5, 5));
    monthsPanel.setBorder(BorderFactory.createTitledBorder("各月完成情况"));
    monthsPanel.setBackground(mainPanel.getBackground());
    
    // 添加月度选择器
    JPanel monthSelectorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    monthSelectorPanel.setBackground(monthsPanel.getBackground());
    monthSelectorPanel.add(new JLabel("选择月份:"));
    
    JComboBox<String> monthComboBox = new JComboBox<>();
    for (YearMonth ym : monthlyTasks.keySet()) {
        monthComboBox.addItem(ym.format(DateTimeFormatter.ofPattern("yyyy年MM月")));
    }
    monthSelectorPanel.add(monthComboBox);
    
    monthsPanel.add(monthSelectorPanel, BorderLayout.NORTH);
    
    // 创建任务列表区域
    JTextArea tasksArea = new JTextArea(10, 40);
    tasksArea.setEditable(false);
    tasksArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
    tasksArea.setBackground(new Color(255, 255, 255, 200)); // 半透明白色背景
    JScrollPane tasksScrollPane = new JScrollPane(tasksArea);
    tasksScrollPane.setBorder(BorderFactory.createEmptyBorder());
    
    // 更新任务列表的方法
    java.awt.event.ActionListener updateTasksList = new java.awt.event.ActionListener() {
        @Override
        public void actionPerformed(java.awt.event.ActionEvent e) {
            int selectedIndex = monthComboBox.getSelectedIndex();
            if (selectedIndex >= 0) {
                // 获取选中的月份
                YearMonth selectedMonth = (YearMonth) monthlyTasks.keySet().toArray()[selectedIndex];
                List<Task> tasks = monthlyTasks.get(selectedMonth);
                
                // 更新任务列表显示
                StringBuilder sb = new StringBuilder();
                if (tasks != null && !tasks.isEmpty()) {
                    for (Task task : tasks) {
                        sb.append("· ").append(task.getName())
                          .append(" (完成于 ")
                          .append(task.getCompletedTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                          .append(")\n");
                    }
                } else {
                    sb.append("该月没有完成的任务");
                }
                tasksArea.setText(sb.toString());
            }
        }
    };
    
    // 添加监听器
    monthComboBox.addActionListener(updateTasksList);
    
    // 初始化显示第一个月的任务
    if (!monthlyTasks.isEmpty()) {
        updateTasksList.actionPerformed(null); // 手动触发更新
    }
    
    monthsPanel.add(tasksScrollPane, BorderLayout.CENTER);
    
    // 创建趋势图面板
    JPanel trendPanel = createTrendPanel(monthlyTasks);
    monthsPanel.add(trendPanel, BorderLayout.SOUTH);
    
    mainPanel.add(monthsPanel, BorderLayout.CENTER);
    
    // 显示对话框
    JDialog dialog = new JDialog();
    dialog.setTitle("月度任务总结");
    dialog.setContentPane(mainPanel);
    dialog.pack();
    dialog.setLocationRelativeTo(null);
    dialog.setVisible(true);
}
private void showCategoryFilterDialog() {
    // 获取所有可用类别
    Set<String> categories = manager.getAllTasks().stream()
            .map(Task::getCategory)
            .filter(Objects::nonNull)
            .filter(cat -> !cat.isEmpty())
            .collect(Collectors.toSet());
    
    if (categories.isEmpty()) {
        JOptionPane.showMessageDialog(mainPanel, "没有可用的任务类别", "类别筛选", JOptionPane.INFORMATION_MESSAGE);
        return;
    }
    
    String[] categoryArray = categories.toArray(new String[0]);
    String selectedCategory = (String) JOptionPane.showInputDialog(
            mainPanel,
            "选择要筛选的类别:",
            "按类别筛选任务",
            JOptionPane.QUESTION_MESSAGE,
            null,
            categoryArray,
            categoryArray[0]
    );
    
    if (selectedCategory != null) {
        // 调用重载的 refreshRecommendedTasks 方法
        refreshRecommendedTasks(selectedCategory);
        
        // 添加返回主视图按钮
        JButton backButton = createStyledButton("返回主视图", 
                new Color(135, 206, 250), 
                new Color(0, 51, 102));
        
        backButton.addActionListener(e -> refreshRecommendedTasks(null));
        
        // 创建按钮容器
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        buttonPanel.add(backButton);
        
        // 将按钮添加到主面板底部
        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    if (selectedCategory != null) {
        // 调用重载的 refreshRecommendedTasks 方法
        refreshRecommendedTasks(selectedCategory);}
}

}