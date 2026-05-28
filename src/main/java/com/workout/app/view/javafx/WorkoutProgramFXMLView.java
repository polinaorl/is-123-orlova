package com.workout.app.view.javafx;

import com.workout.app.dao.ProgramExerciseDAO;
import com.workout.app.dao.ProgramExerciseDAO.ProgramExerciseInfo;
import com.workout.app.dao.WorkoutProgramDAO;
import com.workout.app.dao.ExerciseDAO;
import com.workout.app.database.DatabaseManager;
import com.workout.app.model.User;
import com.workout.app.model.WorkoutProgram;
import com.workout.app.model.Exercise;
import com.workout.app.patterns.strategy.TrainingContext;
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

public class WorkoutProgramFXMLView {
    private Stage stage;
    private User currentUser;
    private TrainingContext trainingContext;
    private VBox mainContent;

    public WorkoutProgramFXMLView(Stage stage, User user) {
        this.stage = stage;
        this.currentUser = user;
        this.trainingContext = new TrainingContext();
        trainingContext.selectStrategyByIndex(0, user);
        initUI();
        loadAllWorkouts();
    }

    private void initUI() {
        stage.setTitle("Программа тренировок");

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f8fa;");

        // Заголовок
        Text title = new Text("ВАША ПРОГРАММА");
        title.setFont(Font.font("Segoe UI", 22));
        title.setStyle("-fx-fill: #2c3e50; -fx-font-weight: bold;");
        BorderPane.setAlignment(title, Pos.CENTER);
        BorderPane.setMargin(title, new Insets(20));
        root.setTop(title);

        // Меню
        MenuBar menuBar = new MenuBar();
        Menu engineMenu = new Menu("Движок расчёта");

        String[] engines = {"1. Классический", "2. По ИМТ", "3. Умный (AI)"};
        for (int i = 0; i < engines.length; i++) {
            final int idx = i;
            MenuItem item = new MenuItem(engines[i]);
            item.setOnAction(e -> {
                trainingContext.selectStrategyByIndex(idx, currentUser);
                loadAllWorkouts();
            });
            engineMenu.getItems().add(item);
        }
        menuBar.getMenus().add(engineMenu);
        root.setTop(menuBar);

        // Контент
        mainContent = new VBox(15);
        mainContent.setPadding(new Insets(20));
        mainContent.setStyle("-fx-background-color: #f5f8fa;");

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #f5f8fa; -fx-background-color: transparent;");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        root.setCenter(scrollPane);

        // Кнопки
        HBox btnBox = new HBox(20);
        btnBox.setAlignment(Pos.CENTER);
        btnBox.setPadding(new Insets(15));

        Button editBtn = createButton("Редактировать", "#3498db");
        Button backBtn = createButton("В профиль", "#95a5a6");

        editBtn.setOnAction(e -> openEditWindow());
        backBtn.setOnAction(e -> new ProfileFXMLView(stage, currentUser));

        btnBox.getChildren().addAll(editBtn, backBtn);
        root.setBottom(btnBox);

        stage.setScene(new Scene(root, 900, 700));
        stage.show();
    }

    private Button createButton(String text, String color) {
        Button btn = new Button(text);
        btn.setPrefSize(160, 38);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");
        btn.setCursor(javafx.scene.Cursor.HAND);
        return btn;
    }

    private void loadAllWorkouts() {
        mainContent.getChildren().clear();

        try (Connection conn = DatabaseManager.getConnection()) {
            WorkoutProgramDAO programDAO = new WorkoutProgramDAO(conn);
            ProgramExerciseDAO exerciseDAO = new ProgramExerciseDAO(conn);

            List<WorkoutProgram> allPrograms = programDAO.findAll();

            for (WorkoutProgram program : allPrograms) {
                if (program.getGoal().equals(currentUser.getGoal()) &&
                        program.getLocation().equals(currentUser.getLocation())) {
                    addWorkoutSection(program, exerciseDAO);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addWorkoutSection(WorkoutProgram program, ProgramExerciseDAO dao) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-border-color: #c8d2dc; -fx-border-radius: 5; -fx-background-radius: 5;");
        card.setPadding(new Insets(15));
        card.setMaxWidth(800);

        Text title = new Text(program.getName());
        title.setFont(Font.font("Segoe UI", 16));
        title.setStyle("-fx-font-weight: bold; -fx-fill: #2c3e50;");

        HBox infoBox = new HBox(20);
        infoBox.getChildren().addAll(
                new Text("Цель: " + program.getGoalText()),
                new Text("Место: " + program.getLocationText())
        );
        infoBox.setStyle("-fx-text-fill: #3c3c3c;");

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #c8d2dc;");

        card.getChildren().addAll(title, infoBox, sep);

        try {
            List<ProgramExerciseInfo> exercises = dao.getExercisesForProgram(program.getId());
            int num = 1;

            for (ProgramExerciseInfo exInfo : exercises) {
                if (exInfo.getExercise() != null) {
                    HBox exRow = new HBox(10);
                    exRow.setAlignment(Pos.CENTER_LEFT);
                    exRow.setPadding(new Insets(5, 0, 5, 0));

                    Text name = new Text(num++ + ". " + exInfo.getExercise().getName());
                    name.setFont(Font.font("Segoe UI", 13));
                    name.setStyle("-fx-font-weight: bold; -fx-fill: #2c3e50;");

                    int weight = calculateWeight(exInfo.getExercise().getName());
                    String reps = trainingContext.getStrategy().modifyExerciseReps(exInfo.getReps());

                    Text details = new Text(exInfo.getSets() + " x " + reps + " | " + weight + " кг | " + exInfo.getExercise().getMuscleGroup());
                    details.setFont(Font.font("Segoe UI", 12));
                    details.setStyle("-fx-fill: #505050;");

                    exRow.getChildren().addAll(name, details);
                    card.getChildren().add(exRow);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        mainContent.getChildren().add(card);
        mainContent.getChildren().add(new Separator());
    }

    private int calculateWeight(String exerciseName) {
        double baseWeight = currentUser.getWeight() * 0.4;
        double factor = 1.0;
        String strategyName = trainingContext.getCurrentStrategyName();

        if (strategyName.contains("ИМТ")) factor = 0.8;
        else if (strategyName.contains("AI")) factor = 1.2;

        if (exerciseName.contains("ног") || exerciseName.contains("Приседания") || exerciseName.contains("Становая")) {
            factor *= 1.5;
        } else if (exerciseName.contains("рук") || exerciseName.contains("бицепс") || exerciseName.contains("трицепс")) {
            factor *= 0.6;
        }
        return (int) (baseWeight * factor);
    }

    private void openEditWindow() {
        try (Connection conn = DatabaseManager.getConnection()) {
            WorkoutProgramDAO programDAO = new WorkoutProgramDAO(conn);
            List<WorkoutProgram> allPrograms = programDAO.findAll();

            ChoiceDialog<String> dialog = new ChoiceDialog<>();
            dialog.setTitle("Редактирование");
            dialog.setHeaderText("Выберите тренировку для редактирования:");

            for (WorkoutProgram p : allPrograms) {
                if (p.getGoal().equals(currentUser.getGoal()) &&
                        p.getLocation().equals(currentUser.getLocation())) {
                    dialog.getItems().add(p.getName());
                }
            }

            Optional<String> selectedNameOpt = dialog.showAndWait();
            if (selectedNameOpt.isPresent()) {
                WorkoutProgram selectedProgram = allPrograms.stream()
                        .filter(p -> p.getName().equals(selectedNameOpt.get()))
                        .findFirst()
                        .orElse(null);

                if (selectedProgram != null) {
                    showFullEditWindow(selectedProgram);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Окно редактирования
    private void showFullEditWindow(WorkoutProgram program) {
        Stage editStage = new Stage();
        editStage.setTitle("Редактирование: " + program.getName());
        editStage.initOwner(stage);

        // Главное окно
        VBox mainRoot = new VBox(15);
        mainRoot.setPadding(new Insets(20));
        mainRoot.setStyle("-fx-background-color: #f5f8fa;");

        Text title = new Text("Управление упражнениями");
        title.setFont(Font.font("Segoe UI", 18));
        title.setStyle("-fx-font-weight: bold;");

        SplitPane splitPane = new SplitPane();
        splitPane.setDividerPositions(0.6);

        // ЛЕВАЯ ПАНЕЛЬ
        VBox leftPanel = new VBox(10);
        leftPanel.setPadding(new Insets(15));
        leftPanel.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5;");

        Label listLabel = new Label("Текущие упражнения:");
        listLabel.setFont(Font.font("Segoe UI", 14));

        ListView<String> exercisesList = new ListView<>();
        exercisesList.setStyle("-fx-background-color: #f9f9f9;");

        try (Connection conn = DatabaseManager.getConnection()) {
            ProgramExerciseDAO peDao = new ProgramExerciseDAO(conn);
            var exercises = peDao.getExercisesForProgram(program.getId());
            for (var ex : exercises) {
                if (ex.getExercise() != null) {
                    exercisesList.getItems().add(ex.getExercise().getName() + " (" + ex.getSets() + " x " + ex.getReps() + ")");
                }
            }
        } catch (Exception e) { e.printStackTrace(); }

        Button removeBtn = new Button("Удалить выбранное");
        removeBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");
        removeBtn.setMaxWidth(Double.MAX_VALUE);
        removeBtn.setOnAction(e -> {
            int selectedIndex = exercisesList.getSelectionModel().getSelectedIndex();
            if (selectedIndex != -1) exercisesList.getItems().remove(selectedIndex);
        });

        VBox.setVgrow(exercisesList, Priority.ALWAYS); // Список растет
        leftPanel.getChildren().addAll(listLabel, exercisesList, removeBtn);

        //  ПРАВАЯ ПАНЕЛЬ
        VBox rightPanel = new VBox(15);
        rightPanel.setPadding(new Insets(15));
        rightPanel.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5;");

        Label formLabel = new Label("Добавить новое упражнение:");
        formLabel.setFont(Font.font("Segoe UI", 14));

        ComboBox<String> exerciseCombo = new ComboBox<>();
        exerciseCombo.setPrefWidth(250);

        try (Connection conn = DatabaseManager.getConnection()) {
            ExerciseDAO exDao = new ExerciseDAO(conn);
            for (Exercise ex : exDao.findAll()) exerciseCombo.getItems().add(ex.getName());
        } catch (Exception e) { e.printStackTrace(); }

        GridPane inputsGrid = new GridPane();
        inputsGrid.setHgap(10);
        inputsGrid.setVgap(10);

        TextField setsField = new TextField("3");
        setsField.setPrefWidth(80);
        TextField repsField = new TextField("10-12");
        repsField.setPrefWidth(80);

        inputsGrid.add(new Label("Подходы:"), 0, 0);
        inputsGrid.add(setsField, 1, 0);
        inputsGrid.add(new Label("Повторения:"), 0, 1);
        inputsGrid.add(repsField, 1, 1);

        Button addBtn = new Button("Добавить в список");
        addBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setOnAction(e -> {
            String selectedEx = exerciseCombo.getValue();
            String sets = setsField.getText();
            String reps = repsField.getText();

            if (selectedEx != null && !sets.isEmpty() && !reps.isEmpty()) {
                exercisesList.getItems().add(selectedEx + " (" + sets + " x " + reps + ")");
                try (Connection conn = DatabaseManager.getConnection()) {
                    ExerciseDAO exDao = new ExerciseDAO(conn);
                    List<Exercise> allEx = exDao.findAll();
                    int exId = 0;
                    for(Exercise ex : allEx) if(ex.getName().equals(selectedEx)) exId = ex.getId();
                    if(exId > 0) {
                        ProgramExerciseDAO peDao = new ProgramExerciseDAO(conn);
                        peDao.addExerciseToProgram(program.getId(), exId, Integer.parseInt(sets), reps, exercisesList.getItems().size());
                    }
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        });

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        rightPanel.getChildren().addAll(formLabel, new Label("Упражнение:"), exerciseCombo, inputsGrid, spacer, addBtn);

        splitPane.getItems().addAll(leftPanel, rightPanel);

        // Кнопка закрытия
        Button closeBtn = new Button("Закрыть и обновить");
        closeBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        closeBtn.setPrefSize(200, 40);
        closeBtn.setOnAction(e -> { editStage.close(); loadAllWorkouts(); });

        VBox bottomBox = new VBox(10);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.getChildren().add(closeBtn);

        //  Собираем всё в один VBox
        mainRoot.getChildren().addAll(title, splitPane, bottomBox);

        editStage.setScene(new Scene(mainRoot, 800, 500));
        editStage.show();
    }
}