package src;
// src/ArchiveManager.java
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ArchiveManager {
    private static final String ARCHIVE_DIR = "DB/archive/";

    public static void archiveTask(Task task) {
        try {
            File dir = new File(ARCHIVE_DIR);
            if (!dir.exists()) dir.mkdirs();

            String month = task.completedTime.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            File file = new File(ARCHIVE_DIR + month + ".ser");

            List<Task> archived = new ArrayList<>();
            if (file.exists()) {
                ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
                archived = (List<Task>) in.readObject();
                in.close();
            }

            archived.add(task);
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
            out.writeObject(archived);
            out.close();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("归档失败: " + e.getMessage());
        }
    }
}