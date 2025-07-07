package src;
// src/SummaryAnalyzer.java
import java.io.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.stream.Collectors;

public class SummaryAnalyzer {
    // 定义归档目录
    private static final String ARCHIVE_DIR = "DB/archive/";

    // 生成月度总结信息
    public static String generateMonthlySummary() {
        YearMonth now = YearMonth.now();
        YearMonth last = now.minusMonths(1);

        int thisMonthCount = countTasks(now);
        int lastMonthCount = countTasks(last);

        StringBuilder sb = new StringBuilder();
        sb.append("===== 月度总结 =====\n");
        sb.append("本月已完成任务数: ").append(thisMonthCount).append("\n");
        sb.append("上月已完成任务数: ").append(lastMonthCount).append("\n");
        if (lastMonthCount > 0) {
            double growth = (thisMonthCount - lastMonthCount) * 100.0 / lastMonthCount;
            sb.append(String.format("增长率: %.2f%%\n", growth));
        } else {
            sb.append("增长率: 无法计算（上月为0）\n");
        }
        return sb.toString();
    }
    
    // 获取月度任务详情
    public static Map<YearMonth, List<Task>> getMonthlyTasks() {
        Map<YearMonth, List<Task>> monthlyTasks = new TreeMap<>(Collections.reverseOrder());
        File archiveDir = new File(ARCHIVE_DIR);
        
        if (!archiveDir.exists() || !archiveDir.isDirectory()) {
            return monthlyTasks;
        }
        
        File[] files = archiveDir.listFiles((dir, name) -> name.endsWith(".ser"));
        if (files == null) return monthlyTasks;
        
        for (File file : files) {
            try {
                String fileName = file.getName();
                String monthStr = fileName.substring(0, fileName.lastIndexOf('.'));
                YearMonth yearMonth = YearMonth.parse(monthStr, DateTimeFormatter.ofPattern("yyyy-MM"));
                
                ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
                List<Task> tasks = (List<Task>) in.readObject();
                in.close();
                
                monthlyTasks.put(yearMonth, tasks);
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("读取归档文件错误: " + e.getMessage());
            }
        }
        
        return monthlyTasks;
    }

    // 计算指定月份已完成任务数
    private static int countTasks(YearMonth ym) {
        // 构造文件名
        String filename = ARCHIVE_DIR + ym.toString() + ".ser";
        try {
            // 创建文件对象
            File file = new File(filename);
            // 如果文件不存在，返回0
            if (!file.exists()) return 0;
            // 创建对象输入流
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
            // 读取任务列表
            List<Task> list = (List<Task>) in.readObject();
            // 关闭输入流
            in.close();
            // 返回任务列表大小
            return list.size();
        } catch (IOException | ClassNotFoundException e) {
            // 异常处理，返回0
            return 0;
        }
    }
}