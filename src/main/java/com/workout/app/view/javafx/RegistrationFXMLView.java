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

public class RegistrationFXMLView {
    private Stage stage;

    public RegistrationFXMLView(Stage stage) {
        this.stage = stage;
        initUI();
    }

    private void initUI() {
        stage.setTitle("Регистрация нового пользователя");

        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #f5f8fa;");

        Text title = new Text("Регистрация");
        title.setFont(Font.font("Segoe UI", 24));
        title.setStyle("-fx-fill: #2c3e50; -fx-font-weight: bold;");

        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(15);
        form.setAlignment(Pos.CENTER);

        int row = 0;

        Label emailLabel = new Label("Email:");
        emailLabel.setFont(Font.font("Segoe UI", 14));
        TextField emailField = new TextField();
        emailField.setPrefWidth(300);
        emailField.setPrefHeight(35);
        form.add(emailLabel, 0, row);
        form.add(emailField, 1, row++);

        Label passLabel = new Label("Пароль:");
        passLabel.setFont(Font.font("Segoe UI", 14));
        PasswordField passField = new PasswordField();
        passField.setPrefWidth(300);
        passField.setPrefHeight(35);
        form.add(passLabel, 0, row);
        form.add(passField, 1, row++);

        Label heightLabel = new Label("Рост (см):");
        heightLabel.setFont(Font.font("Segoe UI", 14));
        TextField heightField = new TextField();
        heightField.setPrefWidth(300);
        heightField.setPrefHeight(35);
        form.add(heightLabel, 0, row);
        form.add(heightField, 1, row++);

        Label weightLabel = new Label("Вес (кг):");
        weightLabel.setFont(Font.font("Segoe UI", 14));
        TextField weightField = new TextField();
        weightField.setPrefWidth(300);
        weightField.setPrefHeight(35);
        form.add(weightLabel, 0, row);
        form.add(weightField, 1, row++);

        Label ageLabel = new Label("Возраст:");
        ageLabel.setFont(Font.font("Segoe UI", 14));
        TextField ageField = new TextField();
        ageField.setPrefWidth(300);
        ageField.setPrefHeight(35);
        form.add(ageLabel, 0, row);
        form.add(ageField, 1, row++);

        Label genderLabel = new Label("Пол:");
        genderLabel.setFont(Font.font("Segoe UI", 14));
        ComboBox<String> genderCombo = new ComboBox<>();
        genderCombo.getItems().addAll("Мужской", "Женский");
        genderCombo.setValue("Мужской");
        genderCombo.setPrefWidth(300);
        genderCombo.setPrefHeight(35);
        form.add(genderLabel, 0, row);
        form.add(genderCombo, 1, row++);

        Label goalLabel = new Label("Цель:");
        goalLabel.setFont(Font.font("Segoe UI", 14));
        ComboBox<String> goalCombo = new ComboBox<>();
        goalCombo.getItems().addAll(
                "Набор мышечной массы",
                "Похудение",
                "Поддержание формы"
        );
        goalCombo.setValue("Набор мышечной массы");
        goalCombo.setPrefWidth(300);
        goalCombo.setPrefHeight(35);
        form.add(goalLabel, 0, row);
        form.add(goalCombo, 1, row++);

        Label locLabel = new Label("Место:");
        locLabel.setFont(Font.font("Segoe UI", 14));
        ComboBox<String> locCombo = new ComboBox<>();
        locCombo.getItems().addAll(
                "Спортивный зал",
                "Домашние условия",
                "Смешанные тренировки"
        );
        locCombo.setValue("Спортивный зал");
        locCombo.setPrefWidth(300);
        locCombo.setPrefHeight(35);
        form.add(locLabel, 0, row);
        form.add(locCombo, 1, row++);

        HBox btnBox = new HBox(20);
        btnBox.setAlignment(Pos.CENTER);

        Button regBtn = createButton("Зарегистрироваться", "#2ecc71");
        Button backBtn = createButton("Назад ко входу", "#95a5a6");

        regBtn.setOnAction(e -> register(
                emailField.getText(),
                passField.getText(),
                heightField.getText(),
                weightField.getText(),
                ageField.getText(),
                genderCombo.getValue(),
                goalCombo.getValue(),
                locCombo.getValue()
        ));

        backBtn.setOnAction(e -> new LoginFXMLView(stage));

        btnBox.getChildren().addAll(regBtn, backBtn);

        root.getChildren().addAll(title, form, btnBox);
        stage.setScene(new Scene(root, 550, 600));
        stage.show();
    }

    private Button createButton(String text, String color) {
        Button btn = new Button(text);
        btn.setPrefSize(180, 40);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");
        btn.setCursor(javafx.scene.Cursor.HAND);
        return btn;
    }

    private void register(String email, String pass, String height, String weight, String age,
                          String gender, String goal, String location) {
        if (email.isEmpty() || pass.isEmpty() || height.isEmpty() || weight.isEmpty() || age.isEmpty()) {
            showAlert("Ошибка", "Заполните все поля!");
            return;
        }

        try {
            double h = Double.parseDouble(height);
            double w = Double.parseDouble(weight);
            int a = Integer.parseInt(age);

            // Преобразуем русские названия в коды
            String goalCode = switch (goal) {
                case "Набор мышечной массы" -> "mass_gain";
                case "Похудение" -> "weight_loss";
                default -> "maintenance";
            };

            String locCode = switch (location) {
                case "Спортивный зал" -> "gym";
                case "Домашние условия" -> "home";
                default -> "mixed";
            };

            String genderCode = "Мужской".equals(gender) ? "male" : "female";

            try (Connection conn = DatabaseManager.getConnection()) {
                UserDAO dao = new UserDAO(conn);
                if (dao.existsByEmail(email)) {
                    showAlert("Ошибка", "Email уже занят!");
                    return;
                }

                User newUser = new User(0, email, pass, h, w, a, genderCode, goalCode, locCode);
                dao.createUser(newUser);

                showAlert("Успех", "Успешно! Теперь войдите.");
                new LoginFXMLView(stage);
            }
        } catch (NumberFormatException e) {
            showAlert("Ошибка", "Неверный формат чисел!");
        } catch (Exception e) {
            showAlert("Ошибка", "Ошибка: " + e.getMessage());
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