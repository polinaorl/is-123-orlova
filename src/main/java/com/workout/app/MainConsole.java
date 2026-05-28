package com.workout.app;

import com.workout.app.database.DatabaseInitializer;
import com.workout.app.patterns.factory.ConsoleGUIFactory;

public class MainConsole {
    public static void main(String[] args) {
        System.out.println("\n═══════════════════════════════════════");
        System.out.println("   СИСТЕМА ПОДБОРА ТРЕНИРОВОК");
        System.out.println("   Console Interface");
        System.out.println("═══════════════════════════════════════\n");

        try {
            System.out.println("Инициализация базы данных...");
            DatabaseInitializer.initialize();
            System.out.println("База данных готова!\n");
        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
        }

        new ConsoleGUIFactory().run();
    }
}