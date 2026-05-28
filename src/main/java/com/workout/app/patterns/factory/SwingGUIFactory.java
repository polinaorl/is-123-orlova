package com.workout.app.patterns.factory;

import com.workout.app.model.User;
import com.workout.app.view.*;

import javax.swing.*;

public class SwingGUIFactory implements GUIFactory {

    @Override
    public JFrame createLoginView() {
        return new LoginView();
    }

    @Override
    public JFrame createRegistrationView() {
        return new RegistrationView();
    }

    @Override
    public JFrame createProfileView(User user) {
        return new ProfileView(user);
    }

    @Override
    public JFrame createWorkoutProgramView(User user) {
        return new WorkoutProgramView(user);
    }
}