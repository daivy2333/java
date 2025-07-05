package src;
// src/SummaryAnalyzer.java
import java.io.*;
import java.time.YearMonth;
import java.util.*;

public class SummaryAnalyzer {
    private static final String ARCHIVE_DIR = "DB/archive/";

    public static void showMonthlySummary() {
        YearMonth now = YearMonth.now();
        YearMonth last = now.minusMonths(1);

        int thisMonth = countTasks(now);
        int lastMonth = countTasks(last);

        System.out.println("\n===== 月度总结 =====");
        System.out.println("本月已完成任务数: " + thisMonth);
        System.out.println("上月已完成任务数: " + lastMonth);
        if (lastMonth > 0) {
            double growth = (thisMonth - lastMonth) * 100.0 / lastMonth;
            System.out.printf("增长率: %.2f%%\n", growth);
        } else {
            System.out.println("增长率: 无法计算（上月为0）");
        }
    }

    private static int countTasks(YearMonth ym) {
        String filename = ARCHIVE_DIR + ym.toString() + ".ser";
        try {
            File file = new File(filename);
            if (!file.exists()) return 0;
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
            List<Task> list = (List<Task>) in.readObject();
            in.close();
            return list.size();
        } catch (IOException | ClassNotFoundException e) {
            return 0;
        }
    }
}
