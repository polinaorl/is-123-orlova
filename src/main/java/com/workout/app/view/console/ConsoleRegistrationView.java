package com.workout.app.view.console;

import com.workout.app.dao.UserDAO;
import com.workout.app.database.DatabaseManager;
import com.workout.app.model.User;

import java.sql.Connection;
import java.util.Scanner;

public class ConsoleRegistrationView {
    public void show() {
        Scanner sc = new Scanner(System.in);

        System.out.println("\n═══════════════════════════════════════");
        System.out.println("    РЕГИСТРАЦИЯ");
        System.out.println("═══════════════════════════════════════\n");

        System.out.print("Email: ");
        String email = sc.nextLine().trim();

        System.out.print("Пароль: ");
        String pass = sc.nextLine();

        System.out.print("Рост (см): ");
        double height = Double.parseDouble(sc.nextLine());

        System.out.print("Вес (кг): ");
        double weight = Double.parseDouble(sc.nextLine());

        System.out.print("Возраст: ");
        int age = Integer.parseInt(sc.nextLine());

        System.out.println("\nПол: 1-Мужской, 2-Женский");
        System.out.print("Выбор: ");
        String gender = sc.nextLine().equals("1") ? "male" : "female";

        System.out.println("\nЦель: 1-Набор массы, 2-Похудение, 3-Поддержание");
        System.out.print("Выбор: ");
        String g = sc.nextLine();
        String goal = switch (g) {
            case "1" -> "mass_gain";
            case "2" -> "weight_loss";
            default -> "maintenance";
        };

        System.out.println("\nМесто: 1-Зал, 2-Дом, 3-Смешанно");
        System.out.print("Выбор: ");
        String l = sc.nextLine();
        String location = switch (l) {
            case "1" -> "gym";
            case "2" -> "home";
            default -> "mixed";
        };

        try (Connection conn = DatabaseManager.getConnection()) {
            UserDAO dao = new UserDAO(conn);
            if (dao.existsByEmail(email)) {
                System.out.println(" Пользователь уже существует!");
                return;
            }

            User newUser = new User(0, email, pass, height, weight, age, gender, goal, location);
            dao.createUser(newUser);

            System.out.println("\n Регистрация успешна! Теперь войдите.");
        } catch (Exception e) {
            System.out.println(" Ошибка: " + e.getMessage());
        }
    }
}