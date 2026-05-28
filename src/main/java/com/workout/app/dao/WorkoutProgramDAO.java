package com.workout.app.dao;

import com.workout.app.model.WorkoutProgram;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WorkoutProgramDAO {
    private Connection conn;

    public WorkoutProgramDAO(Connection conn) {
        this.conn = conn;
    }

    // ручное создание объекта
    private WorkoutProgram mapRow(ResultSet rs) throws SQLException {
        return new WorkoutProgram(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("goal"),
                rs.getString("location"),
                rs.getString("description")
        );
    }


    public List<WorkoutProgram> findAll() {
        List<WorkoutProgram> programs = new ArrayList<>();
        try {
            String sql = "SELECT * FROM workout_programs ORDER BY id";
            var stmt = conn.prepareStatement(sql);
            var rs = stmt.executeQuery();
            while (rs.next()) {
                programs.add(mapRow(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return programs;
    }

    // Поиск программы по цели и месту
    public Optional<WorkoutProgram> findByGoalAndLocation(String goal, String location) {
        try {

            String sql = "SELECT FIRST 1 * FROM workout_programs WHERE goal = ? AND location = ?";
            var stmt = conn.prepareStatement(sql);
            stmt.setString(1, goal);
            stmt.setString(2, location);
            var rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
            return Optional.empty();
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    // Слздать новую программу
    public void create(WorkoutProgram program) {
        try {
            String sql = "INSERT INTO workout_programs (name, goal, location, description) VALUES (?, ?, ?, ?)";
            var stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, program.getName());
            stmt.setString(2, program.getGoal());
            stmt.setString(3, program.getLocation());
            stmt.setString(4, program.getDescription());
            stmt.executeUpdate();

            // Получаем сгенерированный ID
            var keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                program.setId(keys.getInt(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Обновить программу
    public void update(WorkoutProgram program) {
        try {
            String sql = "UPDATE workout_programs SET name = ?, goal = ?, location = ?, description = ? WHERE id = ?";
            var stmt = conn.prepareStatement(sql);
            stmt.setString(1, program.getName());
            stmt.setString(2, program.getGoal());
            stmt.setString(3, program.getLocation());
            stmt.setString(4, program.getDescription());
            stmt.setInt(5, program.getId());
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Удалить программу
    public void delete(int id) {
        try {
            //  Сначала связи
            String deleteRelations = "DELETE FROM program_exercises WHERE program_id = ?";
            var stmt1 = conn.prepareStatement(deleteRelations);
            stmt1.setInt(1, id);
            stmt1.executeUpdate();

            // Потом удаляем саму программу
            String sql = "DELETE FROM workout_programs WHERE id = ?";
            var stmt2 = conn.prepareStatement(sql);
            stmt2.setInt(1, id);
            stmt2.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Существует ли программа
    public boolean exists(String name, String goal, String location) {
        try {
            String sql = "SELECT FIRST 1 1 FROM workout_programs WHERE name = ? AND goal = ? AND location = ?";
            var stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setString(2, goal);
            stmt.setString(3, location);
            var rs = stmt.executeQuery();
            return rs.next();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}