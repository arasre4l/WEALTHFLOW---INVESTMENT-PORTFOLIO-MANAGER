# WEALTHFLOW---INVESTMENT-PORTFOLIO-MANAGER
Created, Developed and Published by Altan ARAS
Advanced Task Management System with Pomodoro Integration, Analytics Dashboard, and Export Capabilities.

## Features

- **Task Management**: Full CRUD operations with priority levels, categories, and due dates
- **Smart Filtering**: Search and filter by status, category, and keywords
- **Pomodoro Timer**: Built-in productivity timer with task association
- **Statistics Dashboard**: Real-time analytics showing completion rates and productivity metrics
- **Data Export**: Export tasks to JSON and CSV formats
- **Theme Support**: Dark and Light theme toggle
- **SQLite Database**: Embedded persistence with WAL mode for performance
- **Modern UI**: JavaFX-based interface with glassmorphic design

## Architecture

- **MVC Pattern**: Clean separation between Models, Views, and Controllers
- **Service Layer**: Business logic isolation
- **Repository Pattern**: Data access abstraction
- **Observer Pattern**: Real-time UI updates
- **Singleton Pattern**: Database and theme management

## Technologies

- Java 17+
- JavaFX 21
- SQLite 3.44
- Maven Build System
- GSON for JSON serialization

## Running the Application

### Prerequisites
- Java Development Kit (JDK) 17 or higher
- Maven 3.6+

### Build and Run

```bash
mvn clean javafx:run
```

### Create Executable JAR

```bash
mvn clean package
java --module-path "path/to/javafx-sdk/lib" --add-modules javafx.controls,javafx.fxml -jar target/taskmaster-pro-1.0.0.jar
```

## Project Structure

```
src/main/java/com/taskmaster/
├── App.java                      # Application entry point
├── controllers/
│   └── MainController.java       # Primary UI controller
├── models/
│   ├── Task.java                 # Task entity
│   └── TaskStatistics.java       # Statistics model
├── database/
│   ├── DatabaseManager.java      # Connection management
│   └── TaskRepository.java       # Data access layer
├── services/
│   ├── TaskService.java          # Business logic
│   ├── PomodoroService.java      # Timer functionality
│   └── ExportService.java        # Data export
└── utils/
    └── ThemeManager.java          # Theme switching

src/main/resources/
├── views/
│   └── main.fxml                 # Main UI layout
└── styles/
    ├── dark-theme.css            # Dark mode styles
    └── light-theme.css           # Light mode styles
```

## Key Capabilities

1. **Task Prioritization**: LOW, MEDIUM, HIGH, URGENT levels with color coding
2. **Task Status Tracking**: TODO, IN_PROGRESS, COMPLETED, ARCHIVED
3. **Overdue Detection**: Automatic identification of past-due tasks
4. **Pomodoro Integration**: Track productivity sessions per task
5. **Category Management**: Organize tasks by predefined or custom categories
6. **Advanced Search**: Full-text search across titles, descriptions, and categories
7. **Data Persistence**: Automatic saving with database indexing
8. **Export Functionality**: CSV and JSON export with proper escaping

## Database Schema

### tasks
- id (INTEGER PRIMARY KEY)
- title (TEXT)
- description (TEXT)
- priority (TEXT)
- status (TEXT)
- category (TEXT)
- created_at (TEXT)
- due_date (TEXT)
- completed_at (TEXT)
- pomodoro_count (INTEGER)

### categories
- id (INTEGER PRIMARY KEY)
- name (TEXT UNIQUE)
- color (TEXT)

## Performance Optimizations

- WAL (Write-Ahead Logging) mode for concurrent access
- Database indexing on frequently queried columns
- Auto-refresh timer for statistics (30-second intervals)
- Lazy loading for task lists
- Prepared statements for SQL injection prevention

## Security Features

- Parameterized SQL queries
- Input validation
- CSV injection prevention
- XSS protection in export functions

## Future Enhancements

- Recurring task engine
- Task dependency graphs
- Cloud synchronization
- Mobile companion app
- Advanced analytics with charts
- Collaborative features
- Custom theme creation

---

**Development Date**: June 2026
**Version**: 1.0.0
**License**: MIT
