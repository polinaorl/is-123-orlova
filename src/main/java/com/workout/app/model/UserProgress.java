package com.workout.app.model;
import java.time.LocalDateTime;

public class UserProgress {
    private int id;
    private int userId;
    private LocalDateTime logDate;
    private double weight;
    private String note;

    public UserProgress() {}
    public UserProgress(int id, int userId, LocalDateTime logDate, double weight, String note) {
        this.id = id;
        this.userId = userId;
        this.logDate = logDate;
        this.weight = weight;
        this.note = note;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public LocalDateTime getLogDate() { return logDate; }
    public void setLogDate(LocalDateTime logDate) { this.logDate = logDate; }
    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}