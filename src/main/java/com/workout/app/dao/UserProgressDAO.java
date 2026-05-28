package com.workout.app.dao;

import com.workout.app.model.UserProgress;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserProgressDAO {
    private final Connection conn;

    public UserProgressDAO(Connection conn) {
        this.conn = conn;
    }

    // 🔥 Метод для записи веса
    public void saveWeight(int userId, double weight, String note) throws SQLException {
        String sql = "INSERT INTO progress_logs (user_id, weight, notes) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setDouble(2, weight);
            stmt.setString(3, note);
            stmt.executeUpdate();
        }
    }

    // Метод для получения истории
    public List<UserProgress> getHistory(int userId) throws SQLException {
        List<UserProgress> list = new ArrayList<>();
        String sql = "SELECT * FROM progress_logs WHERE user_id = ? ORDER BY log_date DESC";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new UserProgress(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getTimestamp("log_date").toLocalDateTime(),
                            rs.getDouble("weight"),
                            rs.getString("notes")
                    ));
                }
            }
        }
        return list;
    }
}