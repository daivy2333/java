package src;
// src/TaskManager.java
import java.io.*;
import java.time.*;
import java.util.*;

public class TaskManager {
    // 定义一个Task列表，用于存储任务
    private List<Task> tasks = new ArrayList<>();
    // 定义一个常量，用于存储任务文件的路径
    private static final String SAVE_FILE = "DB/tasks.ser";

    // 构造函数，用于加载任务文件
    public TaskManager() {
        loadFromFile();
    }

    // 添加任务到任务列表，并保存到文件
    public void addTask(Task task) {
        tasks.add(task);
        saveToFile();
    }

    // 获取未完成的任务，并按重要性排序
    public List<Task> getPendingTasksSortedByImportance() {
        return tasks.stream()
                .filter(t -> !t.isCompleted)
                .sorted((a, b) -> Double.compare(b.computeImportance(), a.computeImportance()))
                .toList();
    }

    // 标记任务为已完成，并归档
    public void markTaskAsCompleted(int index) {
        List<Task> pending = getPendingTasksSortedByImportance();
        if (index >= 0 && index < pending.size()) {
            Task t = pending.get(index);
            t.isCompleted = true;
            t.completedTime = LocalDateTime.now();
            ArchiveManager.archiveTask(t);
            System.out.println("\n任务已归档：" + t.name);
            saveToFile();
        } else {
            System.out.println("无效索引。");
        }
    }

    // 获取所有任务
    public List<Task> getAllTasks() {
        return tasks;
    }

    // 保存任务到文件
    public void saveToFile() {
        try {
            File dir = new File("DB");
            if (!dir.exists()) dir.mkdirs();
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(SAVE_FILE));
            out.writeObject(tasks);
            out.close();
        } catch (IOException e) {
            System.out.println("任务保存失败: " + e.getMessage());
        }
    }

    // 从文件加载任务
    @SuppressWarnings("unchecked")
    public void loadFromFile() {
        try {
            File file = new File(SAVE_FILE);
            if (!file.exists()) return;
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
            tasks = (List<Task>) in.readObject();
            in.close();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("任务加载失败: " + e.getMessage());
        }
    }
}
