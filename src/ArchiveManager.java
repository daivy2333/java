package src;
// src/ArchiveManager.java
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ArchiveManager {
    // 定义归档目录
    private static final String ARCHIVE_DIR = "DB/archive/";

    // 归档任务
    public static void archiveTask(Task task) {
        try {
            // 创建归档目录
            File dir = new File(ARCHIVE_DIR);
            if (!dir.exists()) dir.mkdirs();

            // 获取任务完成时间对应的月份
            String month = task.completedTime.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            // 创建归档文件
            File file = new File(ARCHIVE_DIR + month + ".ser");

            // 创建一个任务列表，用于存储归档的任务
            List<Task> archived = new ArrayList<>();
            // 如果归档文件存在，则从文件中读取任务列表
            if (file.exists()) {
                ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
                archived = (List<Task>) in.readObject();
                in.close();
            }

            // 将任务添加到任务列表中
            archived.add(task);
            // 将任务列表写入归档文件
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
            out.writeObject(archived);
            out.close();
        } catch (IOException | ClassNotFoundException e) {
            // 如果归档失败，则输出错误信息
            System.out.println("归档失败: " + e.getMessage());
        }
    }
}