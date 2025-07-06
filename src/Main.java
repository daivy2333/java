package src;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import javax.swing.filechooser.FileNameExtensionFilter; 
import javax.swing.border.TitledBorder;
public class Main {
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

        
        // 设置按钮的背景色和前景色
        Color buttonColor = new Color(135, 206, 250); 
        Color buttonTextColor = new Color(0, 51, 102); 

        // 创建按钮并添加到按钮面板中
        JButton addButton = createStyledButton("添加任务", buttonColor, buttonTextColor);
        JButton completeButton = createStyledButton("完成任务", buttonColor, buttonTextColor);
        JButton viewAllButton = createStyledButton("查看所有任务", buttonColor, buttonTextColor);
        JButton summaryButton = createStyledButton("查看月度总结", buttonColor, buttonTextColor);
        JButton refreshButton = createStyledButton("刷新推荐", buttonColor, buttonTextColor);
        JButton setBackgroundButton = createStyledButton("设置背景", buttonColor, buttonTextColor); 
        JButton exitButton = createStyledButton("退出", buttonColor, buttonTextColor);


        // 为按钮添加事件监听器
        addButton.addActionListener(e -> showAddTaskDialog());
        completeButton.addActionListener(e -> showCompleteTaskDialog());
        viewAllButton.addActionListener(e -> showTaskList(true));
        summaryButton.addActionListener(e -> SummaryAnalyzer.showMonthlySummary());
        refreshButton.addActionListener(e -> refreshRecommendedTasks());
        setBackgroundButton.addActionListener(e -> chooseBackgroundImage()); 
        exitButton.addActionListener(e -> System.exit(0));

        // 将按钮添加到按钮面板中
        buttonPanel.add(addButton);
        buttonPanel.add(completeButton);
        buttonPanel.add(viewAllButton);
        buttonPanel.add(summaryButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(setBackgroundButton); 
        buttonPanel.add(exitButton);

        // 将按钮面板添加到背景面板中
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

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
                        .append("--------------\n");
            }
            taskDisplayArea.setText(sb.toString());
        }
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
                Task task = new Task(name, content, deadline, priority, tags);
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
}