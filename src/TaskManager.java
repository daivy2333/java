package src;
// src/TaskManager.java
import java.io.*;
import java.time.*;
import java.util.*;

public class TaskManager {
    private List<Task> tasks = new ArrayList<>();
    private static final String SAVE_FILE = "DB/tasks.ser";

    public TaskManager() {
        loadFromFile();
    }

    public void addTask(Task task) {
        tasks.add(task);
        saveToFile();
    }

    public List<Task> getPendingTasksSortedByImportance() {
        return tasks.stream()
                .filter(t -> !t.isCompleted)
                .sorted((a, b) -> Double.compare(b.computeImportance(), a.computeImportance()))
                .toList();
    }

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

    public List<Task> getAllTasks() {
        return tasks;
    }

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
