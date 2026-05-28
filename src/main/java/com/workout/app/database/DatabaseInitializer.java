package com.workout.app.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class DatabaseInitializer {

    public static void initialize() throws SQLException {
        System.out.println("Инициализация базы данных...");

        try (Connection conn = DatabaseManager.getConnection()) {

            conn.setAutoCommit(false);

            // Читаем SQL скрипт
            String sqlScript = readSqlFile("sql/init_db.sql");

            System.out.println("Подключение к базе данных...");
            System.out.println("   URL: jdbc:firebirdsql:localhost/3050:D:\\workout.fdb");
            System.out.println("Подключение успешно!");

            // Разбиваем на отдельные команды
            String[] commands = splitSqlCommands(sqlScript);
            System.out.println("Разделение на команды...");
            System.out.println("Всего команд: " + commands.length);

            System.out.println("\nЭТАП 1: Создание структуры БД...");
            int cmdNum = 1;
            int structureCommands = 0;

            // Сначала выполняем CREATE TABLE и DROP TABLE
            for (String command : commands) {
                String trimmed = command.trim();
                if (trimmed.isEmpty()) continue;

                // Пропускаем INSERT и COMMIT на первом этапе
                String upper = trimmed.toUpperCase();
                if (upper.startsWith("INSERT") || upper.startsWith("COMMIT")) continue;

                try (Statement stmt = conn.createStatement()) {
                    String preview = trimmed.length() > 50 ?
                            trimmed.substring(0, 50).replaceAll("\\s+", " ") + "..." :
                            trimmed.replaceAll("\\s+", " ");

                    System.out.println("   [" + cmdNum + "] Выполняю: " + preview);
                    stmt.execute(trimmed);
                    System.out.println("OK");
                    structureCommands++;
                } catch (SQLException e) {
                    System.out.println("Пропущено: " + e.getMessage());
                }
                cmdNum++;
            }

            System.out.println("Фиксация изменений...");
            conn.commit();
            System.out.println("Структура создана (команд: " + structureCommands + ")\n");

            // Заполнение данными
            System.out.println("ЭТАП 2: Заполнение данными...");
            int dataCommands = 0;
            cmdNum = 1;

            for (String command : commands) {
                String trimmed = command.trim();
                if (trimmed.isEmpty()) continue;

                // Выполняем только INSERT
                if (!trimmed.toUpperCase().startsWith("INSERT")) continue;

                try (Statement stmt = conn.createStatement()) {
                    System.out.println("   [" + cmdNum + "] Выполняю INSERT...");
                    stmt.execute(trimmed);
                    System.out.println("OK");
                    dataCommands++;
                } catch (SQLException e) {
                    System.out.println("Пропущен: " + e.getMessage());
                }
                cmdNum++;
            }

            System.out.println("Фиксация изменений...");
            conn.commit();
            System.out.println("Данные добавлены (команд: " + dataCommands + ")\n");

            System.out.println("База данных успешно инициализирована!");

        } catch (Exception e) {
            System.err.println("Ошибка инициализации: " + e.getMessage());
            e.printStackTrace();
            throw new SQLException("Failed to initialize database", e);
        }
    }



    private static String readSqlFile(String fileName) throws Exception {
        System.out.println("Чтение SQL скрипта...");

        try (Scanner scanner = new Scanner(
                DatabaseInitializer.class.getClassLoader().getResourceAsStream(fileName), "UTF-8")) {
            scanner.useDelimiter("\\A");
            String sqlScript = scanner.hasNext() ? scanner.next() : "";
            System.out.println("Скрипт загружен: " + sqlScript.length() + " символов");
            return sqlScript;
        }
    }

    // Разбивает SQL скрипт на отдельные команды

    private static String[] splitSqlCommands(String sqlScript) {
        sqlScript = sqlScript.replaceAll("--[^\n]*", "");
        sqlScript = sqlScript.replaceAll("/\\*.*?\\*/", "");

        // Разбиваем по ;
        String[] commands = sqlScript.split(";");

        // Фильтруем пустые команды
        java.util.List<String> result = new java.util.ArrayList<>();
        for (String cmd : commands) {
            String trimmed = cmd.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }

        return result.toArray(new String[0]);
    }
}