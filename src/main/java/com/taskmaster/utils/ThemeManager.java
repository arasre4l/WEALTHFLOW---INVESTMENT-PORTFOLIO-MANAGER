package com.taskmaster.utils;

import javafx.scene.Scene;

public class ThemeManager {
    private static ThemeManager instance;
    private Scene scene;
    private String currentTheme = "dark";

    private ThemeManager() {}

    public static synchronized ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    public void applyTheme(String theme) {
        if (scene == null) return;

        scene.getStylesheets().clear();
        String themePath = "/styles/" + theme + "-theme.css";
        scene.getStylesheets().add(ThemeManager.class.getResource(themePath).toExternalForm());
        currentTheme = theme;
    }

    public void toggleTheme() {
        applyTheme(currentTheme.equals("dark") ? "light" : "dark");
    }

    public String getCurrentTheme() {
        return currentTheme;
    }
}
