package com.workout.app.dao;

import com.workout.app.model.Exercise;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExerciseDAO {

    private Connection connection;

    public ExerciseDAO(Connection connection) {
        this.connection = connection;
    }

    public List<Exercise> findAll() throws SQLException {
        String sql = "SELECT * FROM exercises ORDER BY name";
        List<Exercise> exercises = new ArrayList<>();

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                exercises.add(extractExercise(resultSet));
            }
        }

        return exercises;
    }

    public Optional<Exercise> findById(int id) throws SQLException {
        String sql = "SELECT * FROM exercises WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(extractExercise(resultSet));
                } else {
                    return Optional.empty();
                }
            }
        }
    }

    public List<Exercise> findByMuscleGroup(String muscleGroup) throws SQLException {
        String sql = "SELECT * FROM exercises WHERE muscle_group = ? ORDER BY name";
        List<Exercise> exercises = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, muscleGroup);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    exercises.add(extractExercise(resultSet));
                }
            }
        }

        return exercises;
    }

    public int createExercise(Exercise exercise) throws SQLException {
        String sql = "INSERT INTO exercises (name, description, muscle_group, equipment, difficulty) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, exercise.getName());
            statement.setString(2, exercise.getDescription());
            statement.setString(3, exercise.getMuscleGroup());
            statement.setString(4, exercise.getEquipment());
            statement.setString(5, exercise.getDifficulty());

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Создание упражнения не удалось.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("ID упражнения не получен.");
                }
            }
        }
    }

    public void updateExercise(Exercise exercise) throws SQLException {
        String sql = "UPDATE exercises SET name = ?, description = ?, muscle_group = ?, " +
                "equipment = ?, difficulty = ? WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, exercise.getName());
            statement.setString(2, exercise.getDescription());
            statement.setString(3, exercise.getMuscleGroup());
            statement.setString(4, exercise.getEquipment());
            statement.setString(5, exercise.getDifficulty());
            statement.setInt(6, exercise.getId());

            statement.executeUpdate();
        }
    }

    public void deleteExercise(int id) throws SQLException {
        String sql = "DELETE FROM exercises WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    private Exercise extractExercise(ResultSet resultSet) throws SQLException {
        return new Exercise(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getString("description"),
                resultSet.getString("muscle_group"),
                resultSet.getString("equipment"),
                resultSet.getString("difficulty")
        );
    }
}