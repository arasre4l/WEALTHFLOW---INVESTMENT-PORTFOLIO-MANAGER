package com.taskmaster.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.taskmaster.models.Task;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExportService {
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    public void exportToJSON(List<Task> tasks, String filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            gson.toJson(tasks, writer);
        }
    }

    public void exportToCSV(List<Task> tasks, String filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.append("ID,Title,Description,Priority,Status,Category,Created At,Due Date,Completed At,Pomodoros\n");
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            
            for (Task task : tasks) {
                writer.append(String.valueOf(task.getId())).append(",");
                writer.append(escapeCSV(task.getTitle())).append(",");
                writer.append(escapeCSV(task.getDescription())).append(",");
                writer.append(task.getPriority().name()).append(",");
                writer.append(task.getStatus().name()).append(",");
                writer.append(escapeCSV(task.getCategory())).append(",");
                writer.append(task.getCreatedAt().format(formatter)).append(",");
                writer.append(task.getDueDate() != null ? task.getDueDate().format(formatter) : "").append(",");
                writer.append(task.getCompletedAt() != null ? task.getCompletedAt().format(formatter) : "").append(",");
                writer.append(String.valueOf(task.getPomodoroCount())).append("\n");
            }
        }
    }

    private String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private static class LocalDateTimeAdapter extends com.google.gson.TypeAdapter<LocalDateTime> {
        private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        @Override
        public void write(com.google.gson.stream.JsonWriter out, LocalDateTime value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(formatter.format(value));
            }
        }

        @Override
        public LocalDateTime read(com.google.gson.stream.JsonReader in) throws IOException {
            if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            return LocalDateTime.parse(in.nextString(), formatter);
        }
    }
}
