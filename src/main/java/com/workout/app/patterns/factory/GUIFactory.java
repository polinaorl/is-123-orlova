package com.workout.app.patterns.factory;

import com.workout.app.model.User;

import javax.swing.*;


public interface GUIFactory {
    JFrame createLoginView();
    JFrame createRegistrationView();
    JFrame createProfileView(User user);
    JFrame createWorkoutProgramView(User user);
}