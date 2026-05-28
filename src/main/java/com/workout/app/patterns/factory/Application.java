package com.workout.app.patterns.factory;

import com.workout.app.model.User;

import javax.swing.*;

// сменить GUI (Swing JavaFX Console)
public class Application {
    private GUIFactory factory;
    private User currentUser;

    public Application(GUIFactory factory) {
        this.factory = factory;
    }

    public void showLogin() {
        JFrame loginView = factory.createLoginView();
        loginView.setVisible(true);
    }

    public void showRegistration() {
        JFrame regView = factory.createRegistrationView();
        regView.setVisible(true);
    }

    public void showProfile(User user) {
        this.currentUser = user;
        JFrame profileView = factory.createProfileView(user);
        profileView.setVisible(true);
    }

    public void showWorkoutProgram(User user) {
        JFrame programView = factory.createWorkoutProgramView(user);
        programView.setVisible(true);
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
}