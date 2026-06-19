package com.taskmaster.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static DatabaseManager instance;
    private Connection connection;
    private static final String DB_URL = "jdbc:sqlite:taskmaster.db";

    private DatabaseManager() {}

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public void initialize() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);
            connection.createStatement().execute("PRAGMA journal_mode=WAL");
            createTables();
        } catch (Exception e) {
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    private void createTables() throws SQLException {
        String tasksTable = """
            CREATE TABLE IF NOT EXISTS tasks (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                description TEXT,
                priority TEXT NOT NULL,
                status TEXT NOT NULL,
                category TEXT,
                created_at TEXT NOT NULL,
                due_date TEXT,
                completed_at TEXT,
                pomodoro_count INTEGER DEFAULT 0
            )
        """;

        String categoriesTable = """
            CREATE TABLE IF NOT EXISTS categories (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT UNIQUE NOT NULL,
                color TEXT NOT NULL
            )
        """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(tasksTable);
            stmt.execute(categoriesTable);
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_task_status ON tasks(status)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_task_category ON tasks(category)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_task_due_date ON tasks(due_date)");
            
            insertDefaultCategories();
        }
    }

    private void insertDefaultCategories() throws SQLException {
        String[] defaultCategories = {
            "INSERT OR IGNORE INTO categories (name, color) VALUES ('Work', '#3b82f6')",
            "INSERT OR IGNORE INTO categories (name, color) VALUES ('Personal', '#8b5cf6')",
            "INSERT OR IGNORE INTO categories (name, color) VALUES ('Shopping', '#ec4899')",
            "INSERT OR IGNORE INTO categories (name, color) VALUES ('Health', '#10b981')",
            "INSERT OR IGNORE INTO categories (name, color) VALUES ('Education', '#f59e0b')"
        };

        try (Statement stmt = connection.createStatement()) {
            for (String sql : defaultCategories) {
                stmt.execute(sql);
            }
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
