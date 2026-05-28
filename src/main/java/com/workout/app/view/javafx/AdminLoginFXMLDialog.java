package com.workout.app.view.javafx;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class AdminLoginFXMLDialog {
    public AdminLoginFXMLDialog(Stage parent) {
        Stage dialog = new Stage();
        dialog.setTitle(" Вход администратора");

        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #f0f0f0;");

        TextField loginField = new TextField();
        loginField.setPromptText("Логин");
        loginField.setPrefWidth(200);

        PasswordField passField = new PasswordField();
        passField.setPromptText("Пароль");
        passField.setPrefWidth(200);

        Button okBtn = new Button("Войти");
        okBtn.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-font-size: 13px; -fx-padding: 10 20 10 20;");
        okBtn.setCursor(javafx.scene.Cursor.HAND);

        okBtn.setOnAction(e -> {
            if ("admin".equals(loginField.getText()) && "admin".equals(passField.getText())) {
                dialog.close();
                new AdminFXMLView(parent); // 🔥 Открываем админ-панель
            } else {
                showAlert("Ошибка", "Неверные данные!");
            }
        });

        Button cancelBtn = new Button("Отмена");
        cancelBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-size: 13px; -fx-padding: 10 20 10 20;");
        cancelBtn.setCursor(javafx.scene.Cursor.HAND);
        cancelBtn.setOnAction(e -> dialog.close());

        HBox btnBox = new HBox(10);
        btnBox.setAlignment(Pos.CENTER);
        btnBox.getChildren().addAll(okBtn, cancelBtn);

        root.getChildren().addAll(
                new Label("Логин:"), loginField,
                new Label("Пароль:"), passField,
                btnBox
        );

        dialog.setScene(new Scene(root, 300, 220));
        dialog.initOwner(parent);
        dialog.show();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}