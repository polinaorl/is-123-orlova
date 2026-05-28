package com.workout.app.dao;

import com.workout.app.model.Exercise;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProgramExerciseDAO {
    private final Connection conn;

    public ProgramExerciseDAO(Connection conn) {
        this.conn = conn;
    }

    //Добавить упражнение в программу
    public void addExerciseToProgram(int programId, int exerciseId, int sets, String reps, int order) throws SQLException {
        String sql = "INSERT INTO program_exercises (program_id, exercise_id, sets, reps, exercise_order) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, programId);
            stmt.setInt(2, exerciseId);
            stmt.setInt(3, sets);
            stmt.setString(4, reps);
            stmt.setInt(5, order);
            stmt.executeUpdate();
        }
    }

    // Удалить упражнение из программы

    public void removeExerciseFromProgram(int programId, int exerciseId) throws SQLException {
        String sql = "DELETE FROM program_exercises WHERE program_id = ? AND exercise_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, programId);
            stmt.setInt(2, exerciseId);
            stmt.executeUpdate();
        }
    }

    // Получить все упражнения для программы
    public List<ProgramExerciseInfo> getExercisesForProgram(int programId) throws SQLException {
        List<ProgramExerciseInfo> list = new ArrayList<>();

        String sql = "SELECT pe.*, e.name, e.description, e.muscle_group, e.equipment, e.difficulty " +
                "FROM program_exercises pe " +
                "LEFT JOIN exercises e ON pe.exercise_id = e.id " +
                "WHERE pe.program_id = ? " +
                "ORDER BY pe.exercise_order";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, programId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ProgramExerciseInfo info = new ProgramExerciseInfo();
                info.setProgramId(rs.getInt("program_id"));
                info.setExerciseId(rs.getInt("exercise_id"));
                info.setSets(rs.getInt("sets"));
                info.setReps(rs.getString("reps"));
                info.setOrder(rs.getInt("exercise_order"));

                // Если упражнение найдено — заполняем данные
                if (rs.getString("name") != null) {
                    Exercise ex = new Exercise(
                            rs.getInt("exercise_id"),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getString("muscle_group"),
                            rs.getString("equipment"),
                            rs.getString("difficulty")
                    );
                    info.setExercise(ex);
                }

                list.add(info);
            }
        }
        return list;
    }

    //Внутренний класс для хранения информации об упражнении в программе

    public static class ProgramExerciseInfo {
        private int programId;
        private int exerciseId;
        private int sets;
        private String reps;
        private int order;
        private Exercise exercise;

        // Getters
        public int getProgramId() { return programId; }
        public int getExerciseId() { return exerciseId; }
        public int getSets() { return sets; }
        public String getReps() { return reps; }
        public int getOrder() { return order; }
        public Exercise getExercise() { return exercise; }

        // Setters
        public void setProgramId(int programId) { this.programId = programId; }
        public void setExerciseId(int exerciseId) { this.exerciseId = exerciseId; }
        public void setSets(int sets) { this.sets = sets; }
        public void setReps(String reps) { this.reps = reps; }
        public void setOrder(int order) { this.order = order; }
        public void setExercise(Exercise exercise) { this.exercise = exercise; }
    }
}