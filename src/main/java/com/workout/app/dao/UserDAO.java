package com.workout.app.dao;

import com.workout.app.model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Класс для работы с пользователями в базе данных

public class UserDAO {

    private Connection connection;

    public UserDAO(Connection connection) {
        this.connection = connection;
    }

    // Добавить нового пользователя

    public int createUser(User user) throws SQLException {
        String sql = "INSERT INTO users (email, password, height, weight, age, gender, goal, location) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, user.getEmail());
            statement.setString(2, user.getPassword());
            statement.setDouble(3, user.getHeight());
            statement.setDouble(4, user.getWeight());
            statement.setInt(5, user.getAge());
            statement.setString(6, user.getGender());
            statement.setString(7, user.getGoal());
            statement.setString(8, user.getLocation());

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Создание пользователя не удалось, ни одной строки не добавлено.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1); // Возвращаем ID нового пользователя
                } else {
                    throw new SQLException("Создание пользователя не удалось, ID не получен.");
                }
            }
        }
    }

    // Найти пользователя по email

    public Optional<User> findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(extractUser(resultSet));
                } else {
                    return Optional.empty();
                }
            }
        }
    }

    //Найти пользователя по ID

    public Optional<User> findById(int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(extractUser(resultSet));
                } else {
                    return Optional.empty();
                }
            }
        }
    }

    // Получить всех пользователей

    public List<User> findAll() throws SQLException {
        String sql = "SELECT * FROM users";
        List<User> users = new ArrayList<>();

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                users.add(extractUser(resultSet));
            }
        }

        return users;
    }

    // Обновить данные пользователя

    public void updateUser(User user) throws SQLException {
        String sql = "UPDATE users SET height = ?, weight = ?, age = ?, gender = ?, goal = ?, location = ? " +
                "WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDouble(1, user.getHeight());
            statement.setDouble(2, user.getWeight());
            statement.setInt(3, user.getAge());
            statement.setString(4, user.getGender());
            statement.setString(5, user.getGoal());
            statement.setString(6, user.getLocation());
            statement.setInt(7, user.getId());

            statement.executeUpdate();
        }
    }

    // Удалить пользователя

    public void deleteUser(int id) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    // Проверка существования пользователя по email

    public boolean existsByEmail(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
                return false;
            }
        }
    }

    // Вспомогательный метод извлекает данные пользователя из ResultSet

    private User extractUser(ResultSet resultSet) throws SQLException {
        return new User(
                resultSet.getInt("id"),
                resultSet.getString("email"),
                resultSet.getString("password"),
                resultSet.getDouble("height"),
                resultSet.getDouble("weight"),
                resultSet.getInt("age"),
                resultSet.getString("gender"),
                resultSet.getString("goal"),
                resultSet.getString("location")
        );
    }
}