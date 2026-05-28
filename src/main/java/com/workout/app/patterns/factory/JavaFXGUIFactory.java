package com.workout.app.patterns.factory;

import com.workout.app.model.User;
import com.workout.app.view.javafx.LoginFXMLView;
import com.workout.app.view.javafx.ProfileFXMLView;
import com.workout.app.view.javafx.RegistrationFXMLView;
import com.workout.app.view.javafx.WorkoutProgramFXMLView;
import javafx.stage.Stage;

public class JavaFXGUIFactory {
    private Stage primaryStage;

    public JavaFXGUIFactory(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void showLogin() { new LoginFXMLView(primaryStage); }
    public void showRegistration() { new RegistrationFXMLView(primaryStage); }
    public void showProfile(User user) { new ProfileFXMLView(primaryStage, user); }
    public void showWorkoutProgram(User user) { new WorkoutProgramFXMLView(primaryStage, user); }
}