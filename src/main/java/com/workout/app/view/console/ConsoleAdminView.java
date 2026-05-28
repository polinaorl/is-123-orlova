package com.workout.app.view.console;

import com.workout.app.dao.ExerciseDAO;
import com.workout.app.dao.UserDAO;
import com.workout.app.database.DatabaseManager;
import com.workout.app.model.Exercise;
import com.workout.app.model.User;

import java.sql.Connection;
import java.util.List;
import java.util.Scanner;

public class ConsoleAdminView {
    public void show() {
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\n═══════════════════════════════════════");
            System.out.println("    АДМИН-ПАНЕЛЬ");
            System.out.println("═══════════════════════════════════════");
            System.out.println("1.  Пользователи");
            System.out.println("2.  Упражнения");
            System.out.println("3.  Выйти");
            System.out.print("\nВыбор: ");

            String choice = sc.nextLine();
            switch (choice) {
                case "1" -> manageUsers();
                case "2" -> manageExercises();
                case "3" -> { return; }
                default -> System.out.println(" Неверный выбор!");
            }
        }
    }

    private void manageUsers() {
        Scanner sc = new Scanner(System.in);
        try (Connection conn = DatabaseManager.getConnection()) {
            UserDAO dao = new UserDAO(conn);
            List<User> users = dao.findAll();

            System.out.println("\n═══════════════════════════════════════");
            System.out.println("   ПОЛЬЗОВАТЕЛИ");
            System.out.println("═══════════════════════════════════════");
            for (int i = 0; i < users.size(); i++) {
                User u = users.get(i);
                System.out.println((i+1) + ". [" + u.getId() + "] " + u.getEmail() +
                        " | " + u.getGoalText() + " | " + u.getLocationText());
            }

            System.out.print("\nУдалить (введи ID или 0 для отмены): ");
            int id = Integer.parseInt(sc.nextLine());
            if (id > 0) {
                dao.deleteUser(id);
                System.out.println(" Пользователь удалён!");
            }
        } catch (Exception e) {
            System.out.println(" Ошибка: " + e.getMessage());
        }
    }

    private void manageExercises() {
        Scanner sc = new Scanner(System.in);
        try (Connection conn = DatabaseManager.getConnection()) {
            ExerciseDAO dao = new ExerciseDAO(conn);
            List<Exercise> exercises = dao.findAll();

            System.out.println("\n═══════════════════════════════════════");
            System.out.println("   УПРАЖНЕНИЯ");
            System.out.println("═══════════════════════════════════════");
            for (int i = 0; i < exercises.size(); i++) {
                Exercise e = exercises.get(i);
                System.out.println((i+1) + ". [" + e.getId() + "] " + e.getName() +
                        " | " + e.getMuscleGroup() + " | " + e.getEquipment());
            }

            System.out.print("\nУдалить (введи ID или 0 для отмены): ");
            int id = Integer.parseInt(sc.nextLine());
            if (id > 0) {
                dao.deleteExercise(id);
                System.out.println(" Упражнение удалено!");
            }
        } catch (Exception e) {
            System.out.println(" Ошибка: " + e.getMessage());
        }
    }
}