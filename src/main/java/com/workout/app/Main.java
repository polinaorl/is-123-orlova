package com.workout.app;

import com.workout.app.database.DatabaseInitializer;
import com.workout.app.patterns.factory.Application;
import com.workout.app.patterns.factory.SwingGUIFactory;

import javax.swing.*;


public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            System.out.println("=== Система подбора тренировок ===");
            System.out.println("Запуск с использованием Factory Pattern...\n");

            // Инициализация БД
            try {
                System.out.println("Инициализация базы данных...");
                DatabaseInitializer.initialize();
                System.out.println("База данных готова!\n");
            } catch (Exception e) {
                System.err.println("Ошибка инициализации БД: " + e.getMessage());
            }

            // Создаём приложение с Swing-фабрикой
            Application app = new Application(new SwingGUIFactory());

            // Запускаем окно входа
            app.showLogin();
            System.out.println("Приложение запущено!");
        });
    }
}