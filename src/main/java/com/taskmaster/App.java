package com.taskmaster;

import com.taskmaster.database.DatabaseManager;
import com.taskmaster.utils.ThemeManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        DatabaseManager.getInstance().initialize();
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/main.fxml"));
        Scene scene = new Scene(loader.load(), 1400, 900);
        
        ThemeManager.getInstance().setScene(scene);
        ThemeManager.getInstance().applyTheme("dark");
        
        primaryStage.setTitle("TaskMaster Pro - Advanced Task Management");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(800);
        primaryStage.show();
        
        primaryStage.setOnCloseRequest(event -> {
            DatabaseManager.getInstance().close();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
