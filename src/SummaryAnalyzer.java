package src;
// src/SummaryAnalyzer.java
import java.io.*;
import java.time.YearMonth;
import java.util.*;

public class SummaryAnalyzer {
    // 定义归档目录
    private static final String ARCHIVE_DIR = "DB/archive/";

    // 显示月度总结
    public static void showMonthlySummary() {
        // 获取当前月份
        YearMonth now = YearMonth.now();
        // 获取上个月份
        YearMonth last = now.minusMonths(1);

        // 获取本月已完成任务数
        int thisMonth = countTasks(now);
        // 获取上月已完成任务数
        int lastMonth = countTasks(last);

        // 输出月度总结
        System.out.println("\n===== 月度总结 =====");
        System.out.println("本月已完成任务数: " + thisMonth);
        System.out.println("上月已完成任务数: " + lastMonth);
        // 如果上月已完成任务数大于0，计算增长率
        if (lastMonth > 0) {
            double growth = (thisMonth - lastMonth) * 100.0 / lastMonth;
            System.out.printf("增长率: %.2f%%\n", growth);
        } else {
            // 否则输出无法计算
            System.out.println("增长率: 无法计算（上月为0）");
        }
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
