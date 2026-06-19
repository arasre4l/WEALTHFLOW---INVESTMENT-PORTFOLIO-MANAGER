package com.taskmaster.models;

import java.time.LocalDateTime;

public class Task {
    private int id;
    private String title;
    private String description;
    private Priority priority;
    private Status status;
    private String category;
    private LocalDateTime createdAt;
    private LocalDateTime dueDate;
    private LocalDateTime completedAt;
    private int pomodoroCount;

    public enum Priority {
        LOW("Low", "#4ade80"),
        MEDIUM("Medium", "#fbbf24"),
        HIGH("High", "#f87171"),
        URGENT("Urgent", "#dc2626");

        private final String label;
        private final String color;

        Priority(String label, String color) {
            this.label = label;
            this.color = color;
        }

        public String getLabel() { return label; }
        public String getColor() { return color; }
    }

    public enum Status {
        TODO("To Do"),
        IN_PROGRESS("In Progress"),
        COMPLETED("Completed"),
        ARCHIVED("Archived");

        private final String label;

        Status(String label) {
            this.label = label;
        }

        public String getLabel() { return label; }
    }

    public Task() {
        this.createdAt = LocalDateTime.now();
        this.status = Status.TODO;
        this.priority = Priority.MEDIUM;
        this.pomodoroCount = 0;
    }

    public Task(String title, String description, Priority priority, String category, LocalDateTime dueDate) {
        this();
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.category = category;
        this.dueDate = dueDate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { 
        this.status = status;
        if (status == Status.COMPLETED && completedAt == null) {
            this.completedAt = LocalDateTime.now();
        }
    }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public int getPomodoroCount() { return pomodoroCount; }
    public void setPomodoroCount(int pomodoroCount) { this.pomodoroCount = pomodoroCount; }
    
    public void incrementPomodoro() { this.pomodoroCount++; }

    public boolean isOverdue() {
        return dueDate != null && 
               LocalDateTime.now().isAfter(dueDate) && 
               status != Status.COMPLETED;
    }
}
