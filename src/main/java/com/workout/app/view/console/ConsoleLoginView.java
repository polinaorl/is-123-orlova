package com.workout.app.view.console;

import com.workout.app.dao.UserDAO;
import com.workout.app.database.DatabaseManager;
import com.workout.app.model.User;

import java.sql.Connection;
import java.util.Optional;
import java.util.Scanner;

public class ConsoleLoginView {
    public void show() {
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\n═══════════════════════════════════════");
            System.out.println("    ВХОД В СИСТЕМУ (Console)");
            System.out.println("═══════════════════════════════════════");
            System.out.println("1. Войти");
            System.out.println("2. Зарегистрироваться");
            System.out.println("3. Выход");
            System.out.print("\nВыбор: ");

            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1" -> loginUser(sc);
                case "2" -> {
                    new ConsoleRegistrationView().show();
                    System.out.println("\n Регистрация завершена! Теперь войдите (пункт 1).");
                }
                case "3" -> {
                    System.out.println(" До свидания!");
                    return;
                }
                default -> System.out.println(" Неверный выбор!");
            }
        }
    }

    private void loginUser(Scanner sc) {
        System.out.print("Email: ");
        String email = sc.nextLine().trim();
        System.out.print("Пароль: ");
        String pass = sc.nextLine();

        if (email.isEmpty() || pass.isEmpty()) {
            System.out.println(" Введите email и пароль!");
            return;
        }

        //  Вход админа
        if ("admin".equals(email) && "admin".equals(pass)) {
            System.out.println(" Добро пожаловать, Админ!");
            new ConsoleAdminView().show();
            return;
        }

        try (Connection conn = DatabaseManager.getConnection()) {
            Optional<User> u = new UserDAO(conn).findByEmail(email);
            if (u.isPresent() && u.get().getPassword().equals(pass)) {
                System.out.println("\n Добро пожаловать, " + u.get().getEmail() + "!");
                new ConsoleProfileView(u.get()).show();
                return;
            } else {
                System.out.println(" Неверные данные!");
            }
        } catch (Exception e) {
            System.out.println(" Ошибка БД: " + e.getMessage());
        }
    }
}