package com.workout.app.model;

public class User {
    private int id;
    private String email;
    private String password;
    private double height;
    private double weight;
    private int age;
    private String gender;
    private String goal;
    private String location;

    // Конструкторы
    public User(String email, String password, double height, double weight,
                int age, String gender, String goal, String location) {
        this.id = 0;
        this.email = email;
        this.password = password;
        this.height = height;
        this.weight = weight;
        this.age = age;
        this.gender = gender;
        this.goal = goal;
        this.location = location;
    }

    public User(int id, String email, String password, double height, double weight,
                int age, String gender, String goal, String location) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.height = height;
        this.weight = weight;
        this.age = age;
        this.gender = gender;
        this.goal = goal;
        this.location = location;
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    // Вспомогательные методы
    public String getGoalText() {
        switch (goal) {
            case "mass_gain": return "Набор мышечной массы";
            case "weight_loss": return "Похудение";
            case "maintenance": return "Поддержание формы";
            default: return goal;
        }
    }

    public String getLocationText() {
        switch (location) {
            case "gym": return "Спортивный зал";
            case "home": return "Дома";
            case "mixed": return "Смешанный";
            default: return location;
        }
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", goal='" + goal + '\'' +
                ", location='" + location + '\'' +
                '}';
    }
}