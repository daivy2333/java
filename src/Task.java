package src;

import java.io.Serializable;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Task implements Serializable, Comparable<Task> {
    // 常量定义
     static final long serialVersionUID = 1L;
     static final double TIME_FACTOR_MULTIPLIER = 50.0;
     static final double TAG_WEIGHT_MULTIPLIER = 2.0;
     static final double URGENCY_THRESHOLD = 24.0; // 小时

    // 字段
     final String name;
     final String content;
     final LocalDateTime deadline;
     final int basePriority; // 主观权重[7](@ref)
     final Map<String, Integer> tags; // 标签权重
     boolean isCompleted = false;
     LocalDateTime completedTime;
     double cachedImportance = -1; // 缓存计算结果
     final String category;
     public static final List<String> CATEGORIES = Arrays.asList(
        "学习", "娱乐", "日常", "锻炼", "其他"
    );
    
    // 添加新字段

    public Task(String name, String content, LocalDateTime deadline,
                int basePriority, Map<String, Integer> tags, String category) {
        this.name = Objects.requireNonNull(name, "任务名称不能为null");
        this.content = Objects.requireNonNull(content, "任务内容不能为null");
        this.deadline = Objects.requireNonNull(deadline, "截止时间不能为null");
        this.basePriority = validatePriority(basePriority);
        this.tags = new HashMap<>(tags);
        
        // 确保 category 在所有路径上都初始化
        if (category != null && CATEGORIES.contains(category)) {
            this.category = category;
        } else {
            this.category = "其他"; // 默认值
        }
    }

    // 验证优先级范围 (1-10)
    private int validatePriority(int priority) {
        if (priority < 1 || priority > 10) {
            throw new IllegalArgumentException("优先级必须在1-10之间");
        }
        return priority;
    }

    /**
     * 计算动态重要性[7](@ref)
     * 公式: 基础权重 + 时间紧迫因子 + 标签权重总和
     */
    public double computeImportance() {
        if (isCompleted) return 0; // 已完成任务重要性为0

        if (cachedImportance == -1) {
            long hoursLeft = Duration.between(LocalDateTime.now(), deadline).toHours();
            double timeFactor = 1.0 / Math.max(1, hoursLeft);
            int tagSum = tags.values().stream().mapToInt(Integer::intValue).sum();

            cachedImportance = basePriority +
                    (timeFactor * TIME_FACTOR_MULTIPLIER) +
                    (tagSum * TAG_WEIGHT_MULTIPLIER);
        }
        return cachedImportance;
    }

    /**
     * 检查任务是否紧急（剩余时间小于阈值）
     */
    public boolean isUrgent() {
        return Duration.between(LocalDateTime.now(), deadline).toHours() < URGENCY_THRESHOLD;
    }

    // ✅ Getter 方法
    public String getName() { return name; }
    public String getContent() { return content; }
    public LocalDateTime getDeadline() { return deadline; }
    public int getBasePriority() { return basePriority; }
    public Map<String, Integer> getTags() { return Collections.unmodifiableMap(tags); }
    public boolean isCompleted() { return isCompleted; }
    public LocalDateTime getCompletedTime() { return completedTime; }

    /**
     * 标记任务为已完成
     */
    public void markCompleted() {
        if (!isCompleted) {
            this.isCompleted = true;
            this.completedTime = LocalDateTime.now();
            this.cachedImportance = 0; // 重置缓存
        }
    }

    /**
     * 添加或更新标签权重
     */
    public void addTag(String tag, int weight) {
        tags.put(tag, validatePriority(weight));
        cachedImportance = -1; // 重置缓存
    }

    @Override
    public int compareTo(Task other) {
        return Double.compare(other.computeImportance(), this.computeImportance()); // 降序排序
    }

    @Override
    public String toString() {
        String status = isCompleted ?
                "✅ 完成于 " + completedTime.format(DateTimeFormatter.ISO_LOCAL_TIME) :
                "⏳ 剩余 " + Duration.between(LocalDateTime.now(), deadline).toHours() + "小时";

            String tagsInfo = tags.entrySet().stream()
            .map(entry -> entry.getKey() + ":" + entry.getValue())
            .collect(Collectors.joining(", "));

        return String.format(
            "%-20s | 类别: %-10s | 重要性: %-5.2f | 截止: %s | %s\n" +
            "内容: %s\n",
            name, category, computeImportance(),
            deadline.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            status,
            content               
    );
    }

    // 用于序列化的版本控制
    private void readObject(java.io.ObjectInputStream in)
            throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.cachedImportance = -1; // 反序列化后重置缓存
    }

    public char[] getImportance() {
        // 计算各要素值
        double importance = computeImportance();
        double timeFactor = 1.0 / Math.max(1, Duration.between(LocalDateTime.now(), deadline).toHours());
        int tagSum = tags.values().stream().mapToInt(Integer::intValue).sum();

        // 将数值转换为字符串并拼接为CSV格式
        String importanceData = String.format("%.2f,%d,%.2f",
                importance, basePriority, timeFactor);

        // 转换为字符数组
        return importanceData.toCharArray();
    }
    public String getCategory() {
        return category;
    }
}