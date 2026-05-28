package com.workout.app.view.javafx;

import com.workout.app.dao.UserDAO;
import com.workout.app.database.DatabaseManager;
import com.workout.app.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.sql.Connection;
import java.util.Optional;

public class LoginFXMLView {
    private Stage stage;

    public LoginFXMLView(Stage stage) {
        this.stage = stage;
        initUI();
    }

    private void initUI() {
        stage.setTitle("Вход в систему");

        VBox root = new VBox(20);
        root.setPadding(new Insets(40));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #f5f8fa;");

        Text title = new Text("Вход в систему");
        title.setFont(Font.font("Segoe UI", 28));
        title.setStyle("-fx-fill: #2c3e50; -fx-font-weight: bold;");

        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(15);
        form.setAlignment(Pos.CENTER);

        Label emailLabel = new Label("Email:");
        emailLabel.setFont(Font.font("Segoe UI", 14));
        TextField emailField = new TextField();
        emailField.setPrefWidth(300);
        emailField.setPrefHeight(35);

        Label passLabel = new Label("Пароль:");
        passLabel.setFont(Font.font("Segoe UI", 14));
        PasswordField passField = new PasswordField();
        passField.setPrefWidth(300);
        passField.setPrefHeight(35);

        form.add(emailLabel, 0, 0);
        form.add(emailField, 1, 0);
        form.add(passLabel, 0, 1);
        form.add(passField, 1, 1);

        HBox btnBox = new HBox(20);
        btnBox.setAlignment(Pos.CENTER);

        Button loginBtn = createButton("Войти", "#3498db");
        Button regBtn = createButton("Регистрация", "#2ecc71");

        loginBtn.setOnAction(e -> login(emailField.getText(), passField.getText()));
        regBtn.setOnAction(e -> new RegistrationFXMLView(stage));

        btnBox.getChildren().addAll(loginBtn, regBtn);

       // Text hint = new Text("Админ: admin / admin");
       // hint.setStyle("-fx-fill: #7f8c8d; -fx-font-style: italic;");

        root.getChildren().addAll(title, form, btnBox);
        stage.setScene(new Scene(root, 600, 450));
        stage.show();
    }

    private Button createButton(String text, String color) {
        Button btn = new Button(text);
        btn.setPrefSize(140, 40);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        btn.setCursor(javafx.scene.Cursor.HAND);
        return btn;
    }

    private void login(String email, String pass) {
        if (email.isEmpty() || pass.isEmpty()) {
            showAlert("Ошибка", "Введите email и пароль!");
            return;
        }

        // ПРОВЕРКА НА АДМИНА
        if ("admin".equals(email) && "admin".equals(pass)) {
            new AdminFXMLView(stage);
            return;
        }

        try (Connection conn = DatabaseManager.getConnection()) {
            Optional<User> userOpt = new UserDAO(conn).findByEmail(email);
            if (userOpt.isPresent() && userOpt.get().getPassword().equals(pass)) {
                new ProfileFXMLView(stage, userOpt.get());
            } else {
                showAlert("Ошибка", "Неверный email или пароль!");
            }
        } catch (Exception e) {
            showAlert("Ошибка", "Ошибка БД: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}