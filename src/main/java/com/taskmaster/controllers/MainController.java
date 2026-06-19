package com.taskmaster.controllers;

import com.taskmaster.models.Task;
import com.taskmaster.models.TaskStatistics;
import com.taskmaster.services.ExportService;
import com.taskmaster.services.PomodoroService;
import com.taskmaster.services.TaskService;
import com.taskmaster.utils.ThemeManager;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

public class MainController {
    @FXML private VBox taskListContainer;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private Label totalTasksLabel;
    @FXML private Label completedTasksLabel;
    @FXML private Label inProgressLabel;
    @FXML private Label overdueLabel;
    @FXML private Label pomodoroCountLabel;
    @FXML private Label completionRateLabel;
    @FXML private Label pomodoroTimerLabel;
    @FXML private Button pomodoroStartBtn;
    @FXML private Button themeToggleBtn;

    private final TaskService taskService;
    private final PomodoroService pomodoroService;
    private final ExportService exportService;
    private Task selectedTask;

    public MainController() {
        this.taskService = new TaskService();
        this.pomodoroService = new PomodoroService();
        this.exportService = new ExportService();
    }

    @FXML
    public void initialize() {
        setupFilters();
        setupAutoRefresh();
        loadTasks();
        updateStatistics();
        setupPomodoroTimer();
    }

    private void setupFilters() {
        statusFilter.getItems().addAll("All", "To Do", "In Progress", "Completed");
        statusFilter.setValue("All");
        statusFilter.setOnAction(e -> applyFilters());

        categoryFilter.getItems().add("All");
        categoryFilter.getItems().addAll(taskService.getAllCategories());
        categoryFilter.setValue("All");
        categoryFilter.setOnAction(e -> applyFilters());

        searchField.textProperty().addListener((obs, old, newVal) -> applyFilters());
    }

    private void setupAutoRefresh() {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> updateStatistics());
            }
        }, 30000, 30000);
    }

    private void setupPomodoroTimer() {
        pomodoroService.remainingSecondsProperty().addListener((obs, old, newVal) -> {
            Platform.runLater(() -> pomodoroTimerLabel.setText(pomodoroService.getFormattedTime()));
        });

        pomodoroTimerLabel.setText(pomodoroService.getFormattedTime());
    }

    private void loadTasks() {
        taskListContainer.getChildren().clear();
        ObservableList<Task> tasks = taskService.getTasks();

        for (Task task : tasks) {
            taskListContainer.getChildren().add(createTaskCard(task));
        }
    }

    private void applyFilters() {
        String keyword = searchField.getText();
        String statusText = statusFilter.getValue();
        String category = categoryFilter.getValue();

        Task.Status status = null;
        if (!statusText.equals("All")) {
            status = Task.Status.valueOf(statusText.replace(" ", "_").toUpperCase());
        }

        ObservableList<Task> filtered = taskService.filterTasks(keyword, status, category);
        
        taskListContainer.getChildren().clear();
        for (Task task : filtered) {
            taskListContainer.getChildren().add(createTaskCard(task));
        }
    }

    private VBox createTaskCard(Task task) {
        VBox card = new VBox(10);
        card.getStyleClass().add("task-card");
        card.setPadding(new Insets(15));

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(task.getTitle());
        titleLabel.getStyleClass().add("task-title");
        titleLabel.setWrapText(true);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        Label priorityLabel = new Label(task.getPriority().getLabel());
        priorityLabel.getStyleClass().add("priority-badge");
        priorityLabel.setStyle("-fx-background-color: " + task.getPriority().getColor());

        Label statusLabel = new Label(task.getStatus().getLabel());
        statusLabel.getStyleClass().add("status-badge");

        header.getChildren().addAll(titleLabel, priorityLabel, statusLabel);

        Label descLabel = new Label(task.getDescription());
        descLabel.getStyleClass().add("task-description");
        descLabel.setWrapText(true);

        HBox metadata = new HBox(15);
        metadata.setAlignment(Pos.CENTER_LEFT);

        if (task.getCategory() != null) {
            Label categoryLabel = new Label("📁 " + task.getCategory());
            categoryLabel.getStyleClass().add("task-meta");
            metadata.getChildren().add(categoryLabel);
        }

        if (task.getDueDate() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
            Label dueDateLabel = new Label("📅 " + task.getDueDate().format(formatter));
            dueDateLabel.getStyleClass().add("task-meta");
            if (task.isOverdue()) {
                dueDateLabel.setStyle("-fx-text-fill: #ef4444;");
            }
            metadata.getChildren().add(dueDateLabel);
        }

        Label pomodoroLabel = new Label("🍅 " + task.getPomodoroCount());
        pomodoroLabel.getStyleClass().add("task-meta");
        metadata.getChildren().add(pomodoroLabel);

        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);

        if (task.getStatus() != Task.Status.COMPLETED) {
            Button startBtn = new Button("Start");
            startBtn.getStyleClass().add("action-btn");
            startBtn.setOnAction(e -> {
                taskService.startTask(task);
                loadTasks();
                updateStatistics();
            });

            Button completeBtn = new Button("Complete");
            completeBtn.getStyleClass().add("action-btn");
            completeBtn.setOnAction(e -> {
                taskService.completeTask(task);
                loadTasks();
                updateStatistics();
            });

            Button pomodoroBtn = new Button("🍅");
            pomodoroBtn.getStyleClass().add("action-btn");
            pomodoroBtn.setOnAction(e -> startPomodoroForTask(task));

            actions.getChildren().addAll(startBtn, completeBtn, pomodoroBtn);
        }

        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("action-btn");
        editBtn.setOnAction(e -> showEditDialog(task));

        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("action-btn-danger");
        deleteBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Deletion");
            confirm.setHeaderText("Delete Task");
            confirm.setContentText("Are you sure you want to delete this task?");
            
            if (confirm.showAndWait().get() == ButtonType.OK) {
                taskService.deleteTask(task);
                loadTasks();
                updateStatistics();
            }
        });

        actions.getChildren().addAll(editBtn, deleteBtn);

        card.getChildren().addAll(header, descLabel, metadata, actions);
        return card;
    }

    @FXML
    private void handleAddTask() {
        showTaskDialog(null);
    }

    private void showEditDialog(Task task) {
        showTaskDialog(task);
    }

    private void showTaskDialog(Task existingTask) {
        Dialog<Task> dialog = new Dialog<>();
        dialog.setTitle(existingTask == null ? "Add New Task" : "Edit Task");
        dialog.setHeaderText(existingTask == null ? "Create a new task" : "Modify task details");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField titleField = new TextField();
        titleField.setPromptText("Task title");
        if (existingTask != null) titleField.setText(existingTask.getTitle());

        TextArea descArea = new TextArea();
        descArea.setPromptText("Description");
        descArea.setPrefRowCount(3);
        if (existingTask != null) descArea.setText(existingTask.getDescription());

        ComboBox<Task.Priority> priorityBox = new ComboBox<>();
        priorityBox.getItems().addAll(Task.Priority.values());
        priorityBox.setValue(existingTask != null ? existingTask.getPriority() : Task.Priority.MEDIUM);

        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll(taskService.getAllCategories());
        if (existingTask != null) categoryBox.setValue(existingTask.getCategory());

        DatePicker dueDatePicker = new DatePicker();
        if (existingTask != null && existingTask.getDueDate() != null) {
            dueDatePicker.setValue(existingTask.getDueDate().toLocalDate());
        }

        TextField dueTimeField = new TextField();
        dueTimeField.setPromptText("HH:mm");
        if (existingTask != null && existingTask.getDueDate() != null) {
            dueTimeField.setText(existingTask.getDueDate().format(DateTimeFormatter.ofPattern("HH:mm")));
        }

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descArea, 1, 1);
        grid.add(new Label("Priority:"), 0, 2);
        grid.add(priorityBox, 1, 2);
        grid.add(new Label("Category:"), 0, 3);
        grid.add(categoryBox, 1, 3);
        grid.add(new Label("Due Date:"), 0, 4);
        grid.add(dueDatePicker, 1, 4);
        grid.add(new Label("Due Time:"), 0, 5);
        grid.add(dueTimeField, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                LocalDateTime dueDateTime = null;
                if (dueDatePicker.getValue() != null) {
                    dueDateTime = dueDatePicker.getValue().atStartOfDay();
                    if (!dueTimeField.getText().isEmpty()) {
                        try {
                            String[] parts = dueTimeField.getText().split(":");
                            dueDateTime = dueDateTime.withHour(Integer.parseInt(parts[0]))
                                                   .withMinute(Integer.parseInt(parts[1]));
                        } catch (Exception e) {
                            dueDateTime = dueDatePicker.getValue().atTime(23, 59);
                        }
                    }
                }

                if (existingTask != null) {
                    existingTask.setTitle(titleField.getText());
                    existingTask.setDescription(descArea.getText());
                    existingTask.setPriority(priorityBox.getValue());
                    existingTask.setCategory(categoryBox.getValue());
                    existingTask.setDueDate(dueDateTime);
                    return existingTask;
                } else {
                    return new Task(titleField.getText(), descArea.getText(), 
                                  priorityBox.getValue(), categoryBox.getValue(), dueDateTime);
                }
            }
            return null;
        });

        Optional<Task> result = dialog.showAndWait();
        result.ifPresent(task -> {
            if (existingTask != null) {
                taskService.updateTask(task);
            } else {
                taskService.createTask(task.getTitle(), task.getDescription(), 
                                     task.getPriority(), task.getCategory(), task.getDueDate());
            }
            loadTasks();
            updateStatistics();
        });
    }

    private void updateStatistics() {
        TaskStatistics stats = taskService.getStatistics();
        
        totalTasksLabel.setText(String.valueOf(stats.getTotalTasks()));
        completedTasksLabel.setText(String.valueOf(stats.getCompletedTasks()));
        inProgressLabel.setText(String.valueOf(stats.getInProgressTasks()));
        overdueLabel.setText(String.valueOf(stats.getOverdueTasks()));
        pomodoroCountLabel.setText(String.valueOf(stats.getTotalPomodoros()));
        completionRateLabel.setText(String.format("%.1f%%", stats.getCompletionRate()));
    }

    private void startPomodoroForTask(Task task) {
        selectedTask = task;
        pomodoroService.startPomodoro(task);
        pomodoroStartBtn.setText("Pause");
    }

    @FXML
    private void handlePomodoroControl() {
        if (pomodoroService.isRunning()) {
            pomodoroService.pause();
            pomodoroStartBtn.setText("Resume");
        } else {
            if (selectedTask != null) {
                pomodoroService.resume();
                pomodoroStartBtn.setText("Pause");
            }
        }
    }

    @FXML
    private void handlePomodoroReset() {
        pomodoroService.reset();
        pomodoroStartBtn.setText("Start");
        selectedTask = null;
    }

    @FXML
    private void handleExportJSON() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Tasks to JSON");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("JSON Files", "*.json")
        );
        
        File file = fileChooser.showSaveDialog(taskListContainer.getScene().getWindow());
        if (file != null) {
            try {
                exportService.exportToJSON(taskService.getTasks(), file.getAbsolutePath());
                showAlert("Export Successful", "Tasks exported to JSON successfully!");
            } catch (Exception e) {
                showAlert("Export Failed", "Failed to export tasks: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleExportCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Tasks to CSV");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        
        File file = fileChooser.showSaveDialog(taskListContainer.getScene().getWindow());
        if (file != null) {
            try {
                exportService.exportToCSV(taskService.getTasks(), file.getAbsolutePath());
                showAlert("Export Successful", "Tasks exported to CSV successfully!");
            } catch (Exception e) {
                showAlert("Export Failed", "Failed to export tasks: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleThemeToggle() {
        ThemeManager.getInstance().toggleTheme();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
