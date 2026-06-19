package com.taskmaster.database;

import com.taskmaster.models.Task;
import com.taskmaster.models.TaskStatistics;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TaskRepository {
    private final Connection connection;

    public TaskRepository() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    public void createTask(Task task) throws SQLException {
        String sql = """
            INSERT INTO tasks (title, description, priority, status, category, 
                             created_at, due_date, pomodoro_count)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, task.getTitle());
            stmt.setString(2, task.getDescription());
            stmt.setString(3, task.getPriority().name());
            stmt.setString(4, task.getStatus().name());
            stmt.setString(5, task.getCategory());
            stmt.setString(6, task.getCreatedAt().toString());
            stmt.setString(7, task.getDueDate() != null ? task.getDueDate().toString() : null);
            stmt.setInt(8, task.getPomodoroCount());

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    task.setId(keys.getInt(1));
                }
            }
        }
    }

    public void updateTask(Task task) throws SQLException {
        String sql = """
            UPDATE tasks SET title = ?, description = ?, priority = ?, 
                           status = ?, category = ?, due_date = ?, 
                           completed_at = ?, pomodoro_count = ?
            WHERE id = ?
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, task.getTitle());
            stmt.setString(2, task.getDescription());
            stmt.setString(3, task.getPriority().name());
            stmt.setString(4, task.getStatus().name());
            stmt.setString(5, task.getCategory());
            stmt.setString(6, task.getDueDate() != null ? task.getDueDate().toString() : null);
            stmt.setString(7, task.getCompletedAt() != null ? task.getCompletedAt().toString() : null);
            stmt.setInt(8, task.getPomodoroCount());
            stmt.setInt(9, task.getId());

            stmt.executeUpdate();
        }
    }

    public void deleteTask(int taskId) throws SQLException {
        String sql = "DELETE FROM tasks WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, taskId);
            stmt.executeUpdate();
        }
    }

    public Task getTaskById(int id) throws SQLException {
        String sql = "SELECT * FROM tasks WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTask(rs);
                }
            }
        }
        return null;
    }

    public List<Task> getAllTasks() throws SQLException {
        String sql = "SELECT * FROM tasks ORDER BY created_at DESC";
        List<Task> tasks = new ArrayList<>();

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                tasks.add(mapResultSetToTask(rs));
            }
        }
        return tasks;
    }

    public List<Task> getTasksByStatus(Task.Status status) throws SQLException {
        String sql = "SELECT * FROM tasks WHERE status = ? ORDER BY created_at DESC";
        List<Task> tasks = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tasks.add(mapResultSetToTask(rs));
                }
            }
        }
        return tasks;
    }

    public List<Task> searchTasks(String keyword) throws SQLException {
        String sql = """
            SELECT * FROM tasks 
            WHERE title LIKE ? OR description LIKE ? OR category LIKE ?
            ORDER BY created_at DESC
        """;
        List<Task> tasks = new ArrayList<>();
        String searchPattern = "%" + keyword + "%";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tasks.add(mapResultSetToTask(rs));
                }
            }
        }
        return tasks;
    }

    public TaskStatistics getStatistics() throws SQLException {
        TaskStatistics stats = new TaskStatistics();

        String sql = """
            SELECT 
                COUNT(*) as total,
                SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) as completed,
                SUM(CASE WHEN status = 'IN_PROGRESS' THEN 1 ELSE 0 END) as in_progress,
                SUM(CASE WHEN due_date < datetime('now') AND status != 'COMPLETED' THEN 1 ELSE 0 END) as overdue,
                SUM(pomodoro_count) as total_pomodoros
            FROM tasks
        """;

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                stats.setTotalTasks(rs.getInt("total"));
                stats.setCompletedTasks(rs.getInt("completed"));
                stats.setInProgressTasks(rs.getInt("in_progress"));
                stats.setOverdueTasks(rs.getInt("overdue"));
                stats.setTotalPomodoros(rs.getInt("total_pomodoros"));
            }
        }
        return stats;
    }

    public List<String> getAllCategories() throws SQLException {
        String sql = "SELECT DISTINCT name FROM categories ORDER BY name";
        List<String> categories = new ArrayList<>();

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                categories.add(rs.getString("name"));
            }
        }
        return categories;
    }

    private Task mapResultSetToTask(ResultSet rs) throws SQLException {
        Task task = new Task();
        task.setId(rs.getInt("id"));
        task.setTitle(rs.getString("title"));
        task.setDescription(rs.getString("description"));
        task.setPriority(Task.Priority.valueOf(rs.getString("priority")));
        task.setStatus(Task.Status.valueOf(rs.getString("status")));
        task.setCategory(rs.getString("category"));
        task.setCreatedAt(LocalDateTime.parse(rs.getString("created_at")));
        
        String dueDate = rs.getString("due_date");
        if (dueDate != null) {
            task.setDueDate(LocalDateTime.parse(dueDate));
        }
        
        String completedAt = rs.getString("completed_at");
        if (completedAt != null) {
            task.setCompletedAt(LocalDateTime.parse(completedAt));
        }
        
        task.setPomodoroCount(rs.getInt("pomodoro_count"));
        return task;
    }
}
