package com.taskmaster.services;

import com.taskmaster.database.TaskRepository;
import com.taskmaster.models.Task;
import com.taskmaster.models.TaskStatistics;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class TaskService {
    private final TaskRepository repository;
    private final ObservableList<Task> tasks;

    public TaskService() {
        this.repository = new TaskRepository();
        this.tasks = FXCollections.observableArrayList();
        loadAllTasks();
    }

    public void loadAllTasks() {
        try {
            tasks.clear();
            tasks.addAll(repository.getAllTasks());
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load tasks", e);
        }
    }

    public void createTask(String title, String description, Task.Priority priority, 
                          String category, LocalDateTime dueDate) {
        try {
            Task task = new Task(title, description, priority, category, dueDate);
            repository.createTask(task);
            tasks.add(0, task);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create task", e);
        }
    }

    public void updateTask(Task task) {
        try {
            repository.updateTask(task);
            int index = tasks.indexOf(task);
            if (index >= 0) {
                tasks.set(index, task);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update task", e);
        }
    }

    public void deleteTask(Task task) {
        try {
            repository.deleteTask(task.getId());
            tasks.remove(task);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete task", e);
        }
    }

    public void completeTask(Task task) {
        task.setStatus(Task.Status.COMPLETED);
        updateTask(task);
    }

    public void startTask(Task task) {
        task.setStatus(Task.Status.IN_PROGRESS);
        updateTask(task);
    }

    public ObservableList<Task> getTasks() {
        return tasks;
    }

    public ObservableList<Task> filterTasks(String keyword, Task.Status status, String category) {
        try {
            List<Task> filtered = repository.getAllTasks();
            
            if (keyword != null && !keyword.isEmpty()) {
                filtered = repository.searchTasks(keyword);
            }
            
            if (status != null) {
                filtered = filtered.stream()
                    .filter(t -> t.getStatus() == status)
                    .toList();
            }
            
            if (category != null && !category.equals("All")) {
                filtered = filtered.stream()
                    .filter(t -> category.equals(t.getCategory()))
                    .toList();
            }
            
            return FXCollections.observableArrayList(filtered);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to filter tasks", e);
        }
    }

    public TaskStatistics getStatistics() {
        try {
            return repository.getStatistics();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get statistics", e);
        }
    }

    public List<String> getAllCategories() {
        try {
            return repository.getAllCategories();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get categories", e);
        }
    }
}
