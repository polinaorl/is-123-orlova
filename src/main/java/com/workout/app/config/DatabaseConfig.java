package com.workout.app.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DatabaseConfig {
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = DatabaseConfig.class.getClassLoader()
                .getResourceAsStream("database.properties")) {

            if (input != null) {
                properties.load(input);
            } else {
                System.err.println(" Файл database.properties не найден в resources!");
            }

        } catch (IOException e) {
            System.err.println("️ Ошибка загрузки конфигурации: " + e.getMessage());
        }
    }

    public static String getDbUrl() {
        return properties.getProperty("db.url");
    }

    public static String getDbUsername() {
        return properties.getProperty("db.username", "SYSDBA");
    }

    public static String getDbPassword() {
        return properties.getProperty("db.password", "masterkey");
    }
}