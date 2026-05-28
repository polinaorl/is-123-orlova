package com.workout.app.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DatabaseManager {

    // Настройки подключения
    private static final String DB_HOST = "localhost";
    private static final String DB_PORT = "3050";
    private static final String DB_PATH = "D:\\workout.fdb";
    private static final String DB_USER = "SYSDBA";
    private static final String DB_PASSWORD = "masterkey";

    // Ссылка на базу данных
    private static final String URL = "jdbc:firebirdsql:" + DB_HOST + "/" + DB_PORT + ":" + DB_PATH;

    // Метод для получения подключения к базе данных

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, DB_USER, DB_PASSWORD);
    }
}