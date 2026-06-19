package com.taskmaster.models;

public class TaskStatistics {
    private int totalTasks;
    private int completedTasks;
    private int inProgressTasks;
    private int overdueTasks;
    private int totalPomodoros;
    private double completionRate;

    public TaskStatistics() {}

    public int getTotalTasks() { return totalTasks; }
    public void setTotalTasks(int totalTasks) { this.totalTasks = totalTasks; }

    public int getCompletedTasks() { return completedTasks; }
    public void setCompletedTasks(int completedTasks) { 
        this.completedTasks = completedTasks;
        calculateCompletionRate();
    }

    public int getInProgressTasks() { return inProgressTasks; }
    public void setInProgressTasks(int inProgressTasks) { this.inProgressTasks = inProgressTasks; }

    public int getOverdueTasks() { return overdueTasks; }
    public void setOverdueTasks(int overdueTasks) { this.overdueTasks = overdueTasks; }

    public int getTotalPomodoros() { return totalPomodoros; }
    public void setTotalPomodoros(int totalPomodoros) { this.totalPomodoros = totalPomodoros; }

    public double getCompletionRate() { return completionRate; }

    private void calculateCompletionRate() {
        if (totalTasks > 0) {
            this.completionRate = (completedTasks * 100.0) / totalTasks;
        } else {
            this.completionRate = 0;
        }
    }
}
