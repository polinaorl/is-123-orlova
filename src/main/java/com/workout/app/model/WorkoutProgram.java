package com.workout.app.model;


public class WorkoutProgram {
    private int id;
    private String name;
    private String goal;           // mass_gain, weight_loss, maintenance
    private String location;       // gym, home, mixed
    private String description;

    public WorkoutProgram(String name, String goal, String location, String description) {
        this.name = name;
        this.goal = goal;
        this.location = location;
        this.description = description;
    }

    public WorkoutProgram(int id, String name, String goal, String location, String description) {
        this.id = id;
        this.name = name;
        this.goal = goal;
        this.location = location;
        this.description = description;
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return name + " [" + getGoalText() + ", " + getLocationText() + "]";
    }

    // Вспомогательные методы для красивого отображения
    public String getGoalText() {
        switch (goal) {
            case "mass_gain": return "Набор массы";
            case "weight_loss": return "Похудение";
            case "maintenance": return "Поддержание";
            default: return goal;
        }
    }

    public String getLocationText() {
        switch (location) {
            case "gym": return "Зал";
            case "home": return "Дом";
            case "mixed": return "Смешанная";
            default: return location;
        }
    }
}