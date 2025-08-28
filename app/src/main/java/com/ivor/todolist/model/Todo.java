package com.ivor.todolist.model;

import java.util.Date;

public class Todo {
    private long id;
    private String title;
    private String description;
    private boolean isCompleted;
    private Date createdAt;
    private Date dueDate;
    private int priority; // 1: 高优先级, 2: 中优先级, 3: 低优先级
    private String category;

    // 构造函数
    public Todo() {
        this.createdAt = new Date();
        this.isCompleted = false;
        this.priority = 2; // 默认中优先级
    }

    public Todo(String title, String description) {
        this();
        this.title = title;
        this.description = description;
    }

    public Todo(String title, String description, Date dueDate, int priority) {
        this(title, description);
        this.dueDate = dueDate;
        this.priority = priority;
    }

    // Getter 和 Setter 方法
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    // 工具方法
    public boolean isOverdue() {
        if (dueDate == null || isCompleted) {
            return false;
        }
        return new Date().after(dueDate);
    }

    public String getPriorityText() {
        switch (priority) {
            case 1:
                return "高优先级";
            case 2:
                return "中优先级";
            case 3:
                return "低优先级";
            default:
                return "未知";
        }
    }

    @Override
    public String toString() {
        return "Todo{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", isCompleted=" + isCompleted +
                ", createdAt=" + createdAt +
                ", dueDate=" + dueDate +
                ", priority=" + priority +
                ", category='" + category + '\'' +
                '}';
    }
}