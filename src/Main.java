package src;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import javax.swing.filechooser.FileNameExtensionFilter; // Import for file filter
import javax.swing.border.TitledBorder;
public class Main {
    private static final TaskManager manager = new TaskManager();
    private JTextArea taskDisplayArea;
    private BackgroundPanel mainPanel; // To hold all components and allow background image

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().createAndShowGUI());
    }
    // 在 Main 类的内部，但在所有方法的外部，添加这个类定义
    private static class BackgroundPanel extends JPanel {
        private Image backgroundImage;

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, this.getWidth(), this.getHeight(), this);
            }
        }

        public void setBackgroundImage(Image image) {
            this.backgroundImage = image;
            repaint();
        }
    }
    private void createAndShowGUI() {
        JFrame frame = new JFrame("工作助手");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null); // Center the frame

        // Create a custom JPanel for background image
        mainPanel = new BackgroundPanel();
        mainPanel.setLayout(new BorderLayout());
        frame.setContentPane(mainPanel); // Set our custom panel as the content pane
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
        // Top display area for recommended tasks
        taskDisplayArea = new JTextArea();
        taskDisplayArea.setEditable(false);
        taskDisplayArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        taskDisplayArea.setOpaque(false); // Make text area transparent
        taskDisplayArea.setForeground(new Color(0, 51, 102)); // Darker blue for text
        refreshRecommendedTasks();

        JScrollPane scrollPane = new JScrollPane(taskDisplayArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(135, 206, 250), 2), // Sky Blue border
                "推荐任务（按重要性排序）",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 16),
                new Color(0, 51, 102) // Darker blue for title text
        ));
        scrollPane.setOpaque(false); // Make scroll pane transparent
        scrollPane.getViewport().setOpaque(false); // Make viewport transparent

        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Bottom button panel
        JPanel buttonPanel = new JPanel(new GridLayout(1, 7, 10, 10)); // Added one more for background button
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.setOpaque(false); // Make button panel transparent

        // Define a nice sky blue color for buttons
        Color buttonColor = new Color(135, 206, 250); // Sky Blue
        Color buttonTextColor = new Color(0, 51, 102); // Darker blue

        JButton addButton = createStyledButton("添加任务", buttonColor, buttonTextColor);
        JButton completeButton = createStyledButton("完成任务", buttonColor, buttonTextColor);
        JButton viewAllButton = createStyledButton("查看所有任务", buttonColor, buttonTextColor);
        JButton summaryButton = createStyledButton("查看月度总结", buttonColor, buttonTextColor);
        JButton refreshButton = createStyledButton("刷新推荐", buttonColor, buttonTextColor);
        JButton setBackgroundButton = createStyledButton("设置背景", buttonColor, buttonTextColor); // New button
        JButton exitButton = createStyledButton("退出", buttonColor, buttonTextColor);


        addButton.addActionListener(e -> showAddTaskDialog());
        completeButton.addActionListener(e -> showCompleteTaskDialog());
        viewAllButton.addActionListener(e -> showTaskList(true));
        summaryButton.addActionListener(e -> SummaryAnalyzer.showMonthlySummary());
        refreshButton.addActionListener(e -> refreshRecommendedTasks());
        setBackgroundButton.addActionListener(e -> chooseBackgroundImage()); // Action for new button
        exitButton.addActionListener(e -> System.exit(0));

        buttonPanel.add(addButton);
        buttonPanel.add(completeButton);
        buttonPanel.add(viewAllButton);
        buttonPanel.add(summaryButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(setBackgroundButton); // Add the new button
        buttonPanel.add(exitButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    // Helper method to create styled buttons
    private JButton createStyledButton(String text, Color bgColor, Color fgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFocusPainted(false); // Remove focus border
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        return button;
    }

    private void refreshRecommendedTasks() {
        List<Task> list = manager.getPendingTasksSortedByImportance();
        if (list.isEmpty()) {
            taskDisplayArea.setText("暂无推荐任务。");
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < list.size(); i++) {
                Task task = list.get(i);
                sb.append(i + 1).append(". ")
                        .append("名称：").append(task.getName()).append("\n")
                        .append("内容：").append(task.getContent()).append("\n")
                        .append("截止：").append(task.getDeadline().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append("\n")
                        .append("重要性：").append(task.getImportance()).append("\n")  // 修改这里
                        .append("主观权重：").append(task.getBasePriority()).append("\n")  // 修改这里
                        .append("--------------\n");
            }
            taskDisplayArea.setText(sb.toString());
        }
    }

    private void showAddTaskDialog() {
        JTextField nameField = new JTextField();
        JTextField contentField = new JTextField();
        JTextField deadlineField = new JTextField(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))); // Default to current time
        JTextField priorityField = new JTextField("5");

        JPanel inputPanel = new JPanel(new GridLayout(0, 1));
        inputPanel.add(new JLabel("任务名称:"));
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("任务内容:"));
        inputPanel.add(contentField);
        inputPanel.add(new JLabel("截止时间 (yyyy-MM-ddTHH:mm):"));
        inputPanel.add(deadlineField);
        inputPanel.add(new JLabel("初始重要性 (1-10):"));
        inputPanel.add(priorityField);

        // Set dialog background to a lighter sky blue
        UIManager.put("Panel.background", new Color(204, 229, 255));
        UIManager.put("OptionPane.background", new Color(204, 229, 255));
        UIManager.put("Label.background", new Color(204, 229, 255));

        int result = JOptionPane.showConfirmDialog(null, inputPanel, "添加任务", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText();
                String content = contentField.getText();
                LocalDateTime deadline = LocalDateTime.parse(deadlineField.getText());
                int priority = Integer.parseInt(priorityField.getText());
                Map<String, Integer> tags = new HashMap<>();
                tags.put("默认", 0);
                Task task = new Task(name, content, deadline, priority, tags);
                manager.addTask(task);
                refreshRecommendedTasks();
                JOptionPane.showMessageDialog(null, "任务添加成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "输入格式错误，请重试。", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
        // Reset UIManager defaults after dialog is closed to avoid affecting other components
        UIManager.put("Panel.background", null);
        UIManager.put("OptionPane.background", null);
        UIManager.put("Label.background", null);
    }

    private void showTaskList(boolean showAll) {
        List<Task> list = showAll ? manager.getAllTasks() : manager.getPendingTasksSortedByImportance();
        if (list.isEmpty()) {
            JOptionPane.showMessageDialog(null, "暂无任务", "任务列表", JOptionPane.INFORMATION_MESSAGE);
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < list.size(); i++) {
                sb.append(i + 1).append(". ").append(list.get(i)).append("\n");
            }
            JTextArea textArea = new JTextArea(sb.toString());
            textArea.setEditable(false);
            textArea.setLineWrap(true);
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

    private void showCompleteTaskDialog() {
        List<Task> list = manager.getPendingTasksSortedByImportance();
        if (list.isEmpty()) {
            JOptionPane.showMessageDialog(null, "暂无待办任务", "完成任务", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String[] taskArray = list.stream().map(Task::toString).toArray(String[]::new);
        String selected = (String) JOptionPane.showInputDialog(null, "选择已完成任务:", "完成任务",
                JOptionPane.QUESTION_MESSAGE, null, taskArray, taskArray[0]);

        if (selected != null) {
            for (int i = 0; i < list.size(); i++) {
                if (selected.equals(list.get(i).toString())) {
                    // 修复：将任务对象 list.get(i) 修改为它的索引 i
                    manager.markTaskAsCompleted(i); // <-- 这里是修改后的代码

                    refreshRecommendedTasks();
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