package com.workout.app;

import com.workout.app.database.DatabaseInitializer;
import com.workout.app.patterns.factory.JavaFXGUIFactory;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainJavaFX extends Application {
    @Override
    public void start(Stage primaryStage) {
        System.out.println("=== Система подбора тренировок (JavaFX) ===");
        try {
            System.out.println("Инициализация базы данных...");
            DatabaseInitializer.initialize();
            System.out.println("База данных успешно инициализирована!");
        } catch (Exception e) {
            System.err.println("Ошибка инициализации БД: " + e.getMessage());
        }

        JavaFXGUIFactory factory = new JavaFXGUIFactory(primaryStage);
        factory.showLogin();
        System.out.println("JavaFX приложение запущено!");
    }

    public static void main(String[] args) {
        launch(args);
    }
}