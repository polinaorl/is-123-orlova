package com.workout.app.view.javafx;

import com.workout.app.dao.ExerciseDAO;
import com.workout.app.dao.UserDAO;
import com.workout.app.database.DatabaseManager;
import com.workout.app.model.Exercise;
import com.workout.app.model.User;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("unchecked")
public class AdminFXMLView {
    private Stage stage;
    private TabPane tabPane;

    public AdminFXMLView(Stage stage) {
        this.stage = stage;
        initUI();
    }

    private void initUI() {
        stage.setTitle("Панель администратора");

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f8fa;");

        Text title = new Text("ПАНЕЛЬ АДМИНИСТРАТОРА");
        title.setFont(Font.font("Segoe UI", 22));
        title.setStyle("-fx-fill: #2c3e50; -fx-font-weight: bold;");
        BorderPane.setAlignment(title, Pos.CENTER);
        BorderPane.setMargin(title, new Insets(20));
        root.setTop(title);

        tabPane = new TabPane();
        tabPane.setStyle("-fx-background-color: #f5f8fa;");

        Tab usersTab = createUsersTab();
        Tab exercisesTab = createExercisesTab();

        tabPane.getTabs().addAll(usersTab, exercisesTab);
        root.setCenter(tabPane);

        Button backBtn = createButton("На экран входа", "#e74c3c");
        backBtn.setOnAction(e -> new LoginFXMLView(stage));

        HBox btnBox = new HBox(backBtn);
        btnBox.setAlignment(Pos.CENTER);
        btnBox.setPadding(new Insets(15));
        root.setBottom(btnBox);

        stage.setScene(new Scene(root, 900, 600));
        stage.show();
    }

    private Tab createUsersTab() {
        Tab tab = new Tab("Пользователи");
        tab.setClosable(false);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        TableView<User> table = new TableView<>();
        table.setStyle("-fx-background-color: white;");

        TableColumn<User, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getId()));
        idCol.setPrefWidth(50);

        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getEmail()));
        emailCol.setPrefWidth(200);

        TableColumn<User, String> goalCol = new TableColumn<>("Цель");
        goalCol.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getGoalText()));
        goalCol.setPrefWidth(150);

        TableColumn<User, String> locCol = new TableColumn<>("Место");
        locCol.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getLocationText()));
        locCol.setPrefWidth(150);

        TableColumn<User, Double> heightCol = new TableColumn<>("Рост");
        heightCol.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getHeight()));
        heightCol.setPrefWidth(80);

        TableColumn<User, Double> weightCol = new TableColumn<>("Вес");
        weightCol.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getWeight()));
        weightCol.setPrefWidth(80);

        TableColumn<User, Integer> ageCol = new TableColumn<>("Возраст");
        ageCol.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getAge()));
        ageCol.setPrefWidth(80);

        table.getColumns().addAll(idCol, emailCol, goalCol, locCol, heightCol, weightCol, ageCol);

        try (Connection conn = DatabaseManager.getConnection()) {
            List<User> users = new UserDAO(conn).findAll();
            table.setItems(FXCollections.observableArrayList(users));
        } catch (Exception e) {
            e.printStackTrace();
        }

        HBox btnBox = new HBox(15);
        btnBox.setAlignment(Pos.CENTER);

        Button deleteBtn = createButton("Удалить пользователя", "#e74c3c");
        Button refreshBtn = createButton("Обновить", "#3498db");

        deleteBtn.setOnAction(e -> deleteUser(table));
        refreshBtn.setOnAction(e -> {
            try (Connection conn = DatabaseManager.getConnection()) {
                List<User> users = new UserDAO(conn).findAll();
                table.setItems(FXCollections.observableArrayList(users));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        btnBox.getChildren().addAll(deleteBtn, refreshBtn);
        content.getChildren().addAll(table, btnBox);
        tab.setContent(content);

        return tab;
    }

    private Tab createExercisesTab() {
        Tab tab = new Tab("Упражнения");
        tab.setClosable(false);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        TableView<Exercise> table = new TableView<>();
        table.setStyle("-fx-background-color: white;");

        TableColumn<Exercise, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getId()));
        idCol.setPrefWidth(50);

        TableColumn<Exercise, String> nameCol = new TableColumn<>("Название");
        nameCol.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getName()));
        nameCol.setPrefWidth(250);

        TableColumn<Exercise, String> muscleCol = new TableColumn<>("Группа мышц");
        muscleCol.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getMuscleGroup()));
        muscleCol.setPrefWidth(150);

        TableColumn<Exercise, String> equipCol = new TableColumn<>("Инвентарь");
        equipCol.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getEquipment()));
        equipCol.setPrefWidth(150);

        TableColumn<Exercise, String> diffCol = new TableColumn<>("Сложность");
        diffCol.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getDifficulty()));
        diffCol.setPrefWidth(100);

        table.getColumns().addAll(idCol, nameCol, muscleCol, equipCol, diffCol);

        try (Connection conn = DatabaseManager.getConnection()) {
            List<Exercise> exercises = new ExerciseDAO(conn).findAll();
            table.setItems(FXCollections.observableArrayList(exercises));
        } catch (Exception e) {
            e.printStackTrace();
        }

        HBox btnBox = new HBox(15);
        btnBox.setAlignment(Pos.CENTER);

        Button addBtn = createButton("Добавить упражнение", "#2ecc71");
        Button deleteBtn = createButton("Удалить упражнение", "#e74c3c");
        Button refreshBtn = createButton("Обновить", "#3498db");

        addBtn.setOnAction(e -> showAddExerciseDialog());
        deleteBtn.setOnAction(e -> deleteExercise(table));
        refreshBtn.setOnAction(e -> {
            try (Connection conn = DatabaseManager.getConnection()) {
                List<Exercise> exercises = new ExerciseDAO(conn).findAll();
                table.setItems(FXCollections.observableArrayList(exercises));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        btnBox.getChildren().addAll(addBtn, deleteBtn, refreshBtn);
        content.getChildren().addAll(table, btnBox);
        tab.setContent(content);

        return tab;
    }

    private Button createButton(String text, String color) {
        Button btn = new Button(text);
        btn.setPrefSize(180, 38);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");
        btn.setCursor(javafx.scene.Cursor.HAND);
        return btn;
    }

    private void deleteUser(TableView<User> table) {
        User selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Ошибка", "Выберите пользователя!");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение");
        alert.setHeaderText("Удалить пользователя " + selected.getEmail() + "?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try (Connection conn = DatabaseManager.getConnection()) {
                    new UserDAO(conn).deleteUser(selected.getId());
                    table.getItems().remove(selected);
                    showAlert("Успех", "Пользователь удалён!");
                } catch (Exception e) {
                    showAlert("Ошибка", e.getMessage());
                }
            }
        });
    }

    private void deleteExercise(TableView<Exercise> table) {
        Exercise selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Ошибка", "Выберите упражнение!");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение");
        alert.setHeaderText("Удалить упражнение \"" + selected.getName() + "\"?\n\n Оно будет удалено из всех программ!");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try (Connection conn = DatabaseManager.getConnection()) {
                    // 🔥 Сначала удаляем из программ
                    String sqlDeleteRelations = "DELETE FROM program_exercises WHERE exercise_id = ?";
                    var stmt1 = conn.prepareStatement(sqlDeleteRelations);
                    stmt1.setInt(1, selected.getId());
                    stmt1.executeUpdate();

                    // 🔥 Потом удаляем само упражнение
                    new ExerciseDAO(conn).deleteExercise(selected.getId());

                    table.getItems().remove(selected);
                    showAlert("Успех", "Упражнение удалено!");
                } catch (Exception e) {
                    showAlert("Ошибка", e.getMessage());
                }
            }
        });
    }

    private void showAddExerciseDialog() {
        Dialog<Exercise> dialog = new Dialog<>();
        dialog.setTitle("Добавить упражнение");
        dialog.setHeaderText("Введите данные упражнения");

        ButtonType saveButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Название");
        TextField descField = new TextField();
        descField.setPromptText("Описание");
        TextField muscleField = new TextField();
        muscleField.setPromptText("Группа мышц");
        TextField equipField = new TextField();
        equipField.setPromptText("Инвентарь");
        ComboBox<String> diffCombo = new ComboBox<>();
        diffCombo.getItems().addAll("Лёгкий", "Средний", "Сложный");
        diffCombo.setValue("Средний");

        grid.add(new Label("Название:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Описание:"), 0, 1);
        grid.add(descField, 1, 1);
        grid.add(new Label("Группа мышц:"), 0, 2);
        grid.add(muscleField, 1, 2);
        grid.add(new Label("Инвентарь:"), 0, 3);
        grid.add(equipField, 1, 3);
        grid.add(new Label("Сложность:"), 0, 4);
        grid.add(diffCombo, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return new Exercise(0, nameField.getText(), descField.getText(),
                        muscleField.getText(), equipField.getText(), diffCombo.getValue());
            }
            return null;
        });

        Optional<Exercise> result = dialog.showAndWait();
        result.ifPresent(exercise -> {
            try (Connection conn = DatabaseManager.getConnection()) {
                new ExerciseDAO(conn).createExercise(exercise);

                // Обновить таблицу
                Tab exercisesTab = tabPane.getTabs().get(1);
                VBox content = (VBox) exercisesTab.getContent();
                TableView<Exercise> table = (TableView<Exercise>) content.getChildren().get(0);

                List<Exercise> exercises = new ExerciseDAO(conn).findAll();
                table.setItems(FXCollections.observableArrayList(exercises));

                showAlert("Успех", "Упражнение добавлено!");
            } catch (Exception e) {
                showAlert("Ошибка", e.getMessage());
            }
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}