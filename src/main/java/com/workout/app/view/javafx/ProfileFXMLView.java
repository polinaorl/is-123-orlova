package com.workout.app.view.javafx;

import com.workout.app.dao.ProgramExerciseDAO;
import com.workout.app.dao.ProgramExerciseDAO.ProgramExerciseInfo;
import com.workout.app.dao.UserProgressDAO;
import com.workout.app.dao.WorkoutProgramDAO;
import com.workout.app.database.DatabaseManager;
import com.workout.app.model.User;
import com.workout.app.model.UserProgress;
import com.workout.app.model.WorkoutProgram;
import com.workout.app.patterns.observer.*;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.FileOutputStream;
import java.sql.Connection;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class ProfileFXMLView {
    private Stage stage;
    private User currentUser;
    private UserProfileSubject profileSubject;

    public ProfileFXMLView(Stage stage, User user) {
        this.stage = stage;
        this.currentUser = user;
        initUI();
        initObservers();
    }

    private void initUI() {
        stage.setTitle("Профиль пользователя");

        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #f5f8fa;");

        Text title = new Text("Ваш профиль");
        title.setFont(javafx.scene.text.Font.font("Segoe UI", 24));
        title.setStyle("-fx-fill: #2c3e50; -fx-font-weight: bold;");

        VBox infoCard = new VBox(15);
        infoCard.setStyle("-fx-background-color: white; -fx-border-color: #c8d2dc; -fx-border-radius: 5; -fx-background-radius: 5;");
        infoCard.setPadding(new Insets(20));
        infoCard.setMaxWidth(500);

        infoCard.getChildren().addAll(
                createInfoLine("Email:", currentUser.getEmail()),
                createInfoLine("Цель:", currentUser.getGoalText()),
                createInfoLine("Место:", currentUser.getLocationText()),
                createInfoLine("Параметры:",
                        (int)currentUser.getHeight() + " см | " + (int)currentUser.getWeight() + " кг | " + currentUser.getAge() + " лет")
        );

        double bmi = calculateBMI(currentUser.getWeight(), currentUser.getHeight());
        String bmiCategory = getBMICategory(bmi);
        javafx.scene.paint.Color bmiColor = getBMIColor(bmiCategory);

        HBox bmiBox = new HBox(10);
        Text bmiLabel = new Text("ИМТ:");
        bmiLabel.setFont(javafx.scene.text.Font.font("Segoe UI", 14));
        Text bmiValue = new Text(String.format("%.1f", bmi) + " (" + bmiCategory + ")");
        bmiValue.setFont(javafx.scene.text.Font.font("Segoe UI", 14));
        bmiValue.setFill(bmiColor);
        bmiBox.getChildren().addAll(bmiLabel, bmiValue);
        infoCard.getChildren().add(bmiBox);

        HBox btnBox = new HBox(15);
        btnBox.setAlignment(Pos.CENTER);

        Button workoutBtn = createButton("Программа", "#3498db");
        Button progressBtn = createButton("Прогресс", "#2ecc71");
        Button logBtn = createButton("Записать вес", "#f1c40f");
        Button logoutBtn = createButton("Выйти", "#e74c3c");

        workoutBtn.setOnAction(e -> new WorkoutProgramFXMLView(stage, currentUser));
        progressBtn.setOnAction(e -> showProgressSave());
        logBtn.setOnAction(e -> logWeight());
        logoutBtn.setOnAction(e -> new LoginFXMLView(stage));

        btnBox.getChildren().addAll(logBtn, progressBtn, workoutBtn, logoutBtn);

        root.getChildren().addAll(title, infoCard, btnBox);
        stage.setScene(new Scene(root, 600, 500));
        stage.show();
    }

    private VBox createInfoLine(String label, String value) {
        VBox box = new VBox(5);
        Text lbl = new Text(label);
        lbl.setFont(javafx.scene.text.Font.font("Segoe UI", 13));
        lbl.setStyle("-fx-font-weight: bold; -fx-fill: #3c3c3c;");
        Text val = new Text(value);
        val.setFont(javafx.scene.text.Font.font("Segoe UI", 13));
        val.setStyle("-fx-fill: #3c3c3c;");
        box.getChildren().addAll(lbl, val);
        return box;
    }

    private Button createButton(String text, String color) {
        Button btn = new Button(text);
        btn.setPrefSize(130, 40);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");
        btn.setCursor(javafx.scene.Cursor.HAND);
        return btn;
    }

    private double calculateBMI(double weightKg, double heightCm) {
        double heightM = heightCm / 100.0;
        return weightKg / (heightM * heightM);
    }

    private String getBMICategory(double bmi) {
        if (bmi < 18.5) return "Недостаточный вес";
        if (bmi < 25) return "Норма";
        if (bmi < 30) return "Избыточный вес";
        return "Ожирение";
    }

    private javafx.scene.paint.Color getBMIColor(String category) {
        switch (category) {
            case "Норма": return javafx.scene.paint.Color.GREEN;
            case "Недостаточный вес": return javafx.scene.paint.Color.GOLD;
            case "Избыточный вес": return javafx.scene.paint.Color.ORANGE;
            default: return javafx.scene.paint.Color.RED;
        }
    }

    private void initObservers() {
        profileSubject = new UserProfileSubject(String.valueOf(currentUser.getId()));
        profileSubject.attach(new TrainingProgramObserver(currentUser, this::refresh));
        profileSubject.attach(new AchievementObserver(currentUser.getEmail()));
        profileSubject.attach(new ProgressLoggerObserver());
    }

    private void refresh() {
        System.out.println("Программа обновлена: " + currentUser.getGoal());
    }

    // Запись веса
    private void logWeight() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Запись веса");
        dialog.setHeaderText("Введите текущий вес (кг):");
        dialog.setContentText("Вес:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(weightStr -> {
            try {
                double weight = Double.parseDouble(weightStr.trim());
                try (Connection conn = DatabaseManager.getConnection()) {
                    new UserProgressDAO(conn).saveWeight(currentUser.getId(), weight, "Записано из JavaFX");
                }


                if (profileSubject != null) {
                    profileSubject.onWeightChanged(weight);
                }

                showAlert("Успех", " Вес " + weight + " кг записан!");
            } catch (Exception e) {
                showAlert("Ошибка", "! " + e.getMessage());
            }
        });
    }

    private void showProgressSave() {
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Excel", "Excel", "PDF", "DOC");
        dialog.setTitle("Сохранение прогресса");
        dialog.setHeaderText("Выберите формат сохранения:");
        dialog.setContentText("Формат:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(method -> {
            try {
                switch (method) {
                    case "Excel" -> saveProgressToExcel();
                    case "PDF" -> saveProgressToPdf();
                    case "DOC" -> saveProgressToDoc();
                }
            } catch (Exception e) {
                showAlert("Ошибка", e.getMessage());
            }
        });
    }

    private String getWorkoutPlanText() {
        StringBuilder sb = new StringBuilder();
        try (Connection conn = DatabaseManager.getConnection()) {
            WorkoutProgramDAO progDao = new WorkoutProgramDAO(conn);
            ProgramExerciseDAO peDao = new ProgramExerciseDAO(conn);

            List<WorkoutProgram> programs = progDao.findAll();
            for (WorkoutProgram prog : programs) {
                if (prog.getGoal().equals(currentUser.getGoal()) && prog.getLocation().equals(currentUser.getLocation())) {
                    sb.append("\n").append(prog.getName().toUpperCase()).append("\n");
                    sb.append("=".repeat(50)).append("\n");

                    List<ProgramExerciseInfo> infos = peDao.getExercisesForProgram(prog.getId());
                    for (ProgramExerciseInfo info : infos) {
                        if (info.getExercise() != null) {
                            sb.append("  • ").append(info.getExercise().getName())
                                    .append(" [").append(info.getExercise().getMuscleGroup()).append("]")
                                    .append(" : ").append(info.getSets())
                                    .append(" x ").append(info.getReps()).append("\n");
                        }
                    }
                    sb.append("\n");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    //  DOC с транслитерацией
    private void saveProgressToDoc() {
        try {
            String workoutText = getWorkoutPlanText();
            double bmi = calculateBMI(currentUser.getWeight(), currentUser.getHeight());

            // Получаем историю веса
            StringBuilder historyText = new StringBuilder();
            historyText.append("Дата\t\t\tВес (кг)\tИзменение\n");
            historyText.append("--------------------------------------------------\n");

            double prevWeight = currentUser.getWeight();
            historyText.append("При регистрации\t").append(prevWeight).append("\t\t0 (начало)\n");

            try (Connection conn = DatabaseManager.getConnection()) {
                List<UserProgress> history = new UserProgressDAO(conn).getHistory(currentUser.getId());
                for (UserProgress entry : history) {
                    double change = entry.getWeight() - prevWeight;
                    String changeStr = (change > 0 ? "+" : "") + String.format("%.1f", change);
                    historyText.append(entry.getLogDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")))
                            .append("\t").append(entry.getWeight())
                            .append("\t\t").append(changeStr).append(" кг\n");
                    prevWeight = entry.getWeight();
                }
            }

            String html = """
                <html>
                <head><meta charset="UTF-8"></head>
                <body style="font-family: Arial; padding: 20px;">
                    <h2 style="color: #2c3e50;">ОТЧЁТ О ПРОГРЕССЕ ТРЕНИРОВОК</h2>
                    <hr>
                    <p><b>Пользователь:</b> %s</p>
                    <p><b>Дата:</b> %s</p>
                    <p><b>Параметры:</b> Рост %s см | Вес %s кг | Возраст %s лет</p>
                    <p><b>ИМТ:</b> %.1f (%s)</p>
                    <hr>
                    <h2 style="color: #2c3e50;">ПРОГРАММА ТРЕНИРОВОК</h2>
                    <pre style="font-family: Arial; font-size: 12px;">%s</pre>
                    <hr>
                    <h2 style="color: #2c3e50;">ИСТОРИЯ ВЕСА</h2>
                    <pre style="font-family: Arial; font-size: 12px;">%s</pre>
                </body>
                </html>
                """.formatted(
                    currentUser.getEmail(),
                    new java.util.Date(),
                    (int)currentUser.getHeight(),
                    (int)currentUser.getWeight(),
                    currentUser.getAge(),
                    bmi,
                    getBMICategory(bmi),
                    workoutText,
                    historyText.toString()
            );

            String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
            String filename = "progress_" + currentUser.getId() + "_" + timestamp + ".doc";
            java.nio.file.Files.writeString(java.nio.file.Paths.get(filename), html);

            showAlert("Успех", " Отчёт сохранён в DOC файл!\n Файл: " + filename);
        } catch (Exception e) {
            showAlert("Ошибка", " Не удалось сохранить DOC: " + e.getMessage());
        }
    }

    //  PDF с транслитерацией и программой
    private void saveProgressToPdf() {
        try {
            String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
            String filename = "progress_" + currentUser.getId() + "_" + timestamp + ".pdf";

            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(filename));
            document.open();

            // Шрифты
            com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 16, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Font normalFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 11, com.itextpdf.text.Font.NORMAL);
            com.itextpdf.text.Font boldFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.BOLD);

            // ЗАГОЛОВОК
            document.add(new Paragraph("WORKOUT PROGRESS REPORT", titleFont));
            document.add(new Paragraph(" "));

            // Параметры
            document.add(new Paragraph("User: " + currentUser.getEmail(), normalFont));
            document.add(new Paragraph("Height: " + currentUser.getHeight() + " cm", normalFont));
            document.add(new Paragraph("Weight: " + currentUser.getWeight() + " kg", normalFont));
            document.add(new Paragraph("Age: " + currentUser.getAge() + " years", normalFont));
            double bmi = calculateBMI(currentUser.getWeight(), currentUser.getHeight());
            document.add(new Paragraph("BMI: " + String.format("%.1f", bmi), normalFont));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("--------------------------------------------------", normalFont));
            document.add(new Paragraph(" "));

            // ИСТОРИЯ ВЕСА
            document.add(new Paragraph("WEIGHT HISTORY", boldFont));
            document.add(new Paragraph(" ", normalFont));

            double prevWeight = currentUser.getWeight();
            document.add(new Paragraph("* Registration: " + prevWeight + " kg (baseline)", normalFont));

            try (Connection conn = DatabaseManager.getConnection()) {
                UserProgressDAO dao = new UserProgressDAO(conn);
                List<UserProgress> hist = dao.getHistory(currentUser.getId());

                for (UserProgress p : hist) {
                    double change = p.getWeight() - prevWeight;
                    String sign = change > 0 ? "+" : "";
                    String dateStr = p.getLogDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));

                    document.add(new Paragraph("* " + dateStr + ": " + p.getWeight() +
                            " kg (" + sign + String.format("%.1f", change) + " kg)", normalFont));
                    prevWeight = p.getWeight();
                }
            }

            document.add(new Paragraph(" "));
            document.add(new Paragraph("--------------------------------------------------", normalFont));
            document.add(new Paragraph(" "));

            // ПРОГРАММА ТРЕНИРОВОК
            document.add(new Paragraph("WORKOUT PROGRAM", boldFont));
            document.add(new Paragraph(" ", normalFont));

            try (Connection conn = DatabaseManager.getConnection()) {
                WorkoutProgramDAO progDao = new WorkoutProgramDAO(conn);
                ProgramExerciseDAO peDao = new ProgramExerciseDAO(conn);

                List<WorkoutProgram> programs = progDao.findAll();
                int exerciseNum = 1;

                for (WorkoutProgram prog : programs) {
                    if (prog.getGoal().equals(currentUser.getGoal()) &&
                            prog.getLocation().equals(currentUser.getLocation())) {

                        // Транслитерируем название программы
                        String progNameTranslit = transliterate(prog.getName());
                        document.add(new Paragraph(">> " + progNameTranslit.toUpperCase(), boldFont));
                        document.add(new Paragraph("   Goal: " + prog.getGoal() + " | Location: " + prog.getLocation(), normalFont));
                        document.add(new Paragraph(" ", normalFont));

                        List<ProgramExerciseInfo> infos = peDao.getExercisesForProgram(prog.getId());
                        for (ProgramExerciseInfo info : infos) {
                            if (info.getExercise() != null) {
                                // Транслитерируем название упражнения и группу мышц
                                String exerciseName = transliterate(info.getExercise().getName());
                                String muscleGroup = transliterate(info.getExercise().getMuscleGroup());

                                String line = "   " + exerciseNum++ + ". " + exerciseName +
                                        " [" + muscleGroup + "] — " +
                                        info.getSets() + " sets x " + info.getReps() + " reps";
                                document.add(new Paragraph(line, normalFont));
                            }
                        }
                        document.add(new Paragraph(" ", normalFont));
                        exerciseNum = 1;
                    }
                }
            }

            document.close();
            showAlert("Успех", " PDF saved: " + filename);
        } catch (Exception e) {
            showAlert("Ошибка", " PDF Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Экспорт В EXCEL
    private void saveProgressToExcel() {
        try (org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Прогресс тренировок");

            // Стили
            org.apache.poi.ss.usermodel.Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);

            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);

            org.apache.poi.ss.usermodel.CellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setFont(titleFont);
            titleStyle.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.DARK_BLUE.getIndex());
            titleStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
            titleStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);

            org.apache.poi.ss.usermodel.CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);

            org.apache.poi.ss.usermodel.CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
            dataStyle.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);

            int rowNum = 0;

            //  ИНФОРМАЦИЯ О ПОЛЬЗОВАТЕЛЕ
            org.apache.poi.ss.usermodel.Row userInfoRow = sheet.createRow(rowNum++);
            userInfoRow.createCell(0).setCellValue(" ИНФОРМАЦИЯ О ПОЛЬЗОВАТЕЛЕ");
            userInfoRow.getCell(0).setCellStyle(titleStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowNum - 1, rowNum - 1, 0, 3));

            double bmi = calculateBMI(currentUser.getWeight(), currentUser.getHeight());
            String[][] userData = {
                    {"Email:", currentUser.getEmail()},
                    {"Текущий вес:", currentUser.getWeight() + " кг"},
                    {"Рост:", currentUser.getHeight() + " см"},
                    {"Возраст:", currentUser.getAge() + " лет"},
                    {"ИМТ:", String.format("%.1f", bmi) + " (" + getBMICategory(bmi) + ")"},
                    {"Цель:", currentUser.getGoalText()},
                    {"Место:", currentUser.getLocationText()}
            };
            for (String[] row : userData) {
                org.apache.poi.ss.usermodel.Row r = sheet.createRow(rowNum++);
                r.createCell(0).setCellValue(row[0]);
                r.createCell(1).setCellValue(row[1]);
                for (int i = 0; i < 2; i++) r.getCell(i).setCellStyle(dataStyle);
            }
            rowNum++;

            //  ПРОГРАММА ТРЕНИРОВОК
            org.apache.poi.ss.usermodel.Row workoutTitleRow = sheet.createRow(rowNum++);
            workoutTitleRow.createCell(0).setCellValue(" ПРОГРАММА ТРЕНИРОВОК");
            workoutTitleRow.getCell(0).setCellStyle(titleStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowNum - 1, rowNum - 1, 0, 6));

            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {"№", "Упражнение", "Группа мышц", "Подходы", "Повторения", "Вес (кг)", "Инвентарь"};
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            try (java.sql.Connection conn = DatabaseManager.getConnection()) {
                WorkoutProgramDAO progDao = new WorkoutProgramDAO(conn);
                ProgramExerciseDAO peDao = new ProgramExerciseDAO(conn);
                List<WorkoutProgram> programs = progDao.findAll();
                int exerciseRowNum = rowNum;
                int exNum = 1;

                for (WorkoutProgram prog : programs) {
                    if (prog.getGoal().equals(currentUser.getGoal()) && prog.getLocation().equals(currentUser.getLocation())) {
                        org.apache.poi.ss.usermodel.Row titleRow = sheet.createRow(exerciseRowNum++);
                        org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
                        titleCell.setCellValue("📅 " + prog.getName());
                        titleCell.setCellStyle(headerStyle);
                        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(exerciseRowNum - 1, exerciseRowNum - 1, 0, headers.length - 1));

                        List<ProgramExerciseInfo> exercises = peDao.getExercisesForProgram(prog.getId());
                        for (ProgramExerciseInfo info : exercises) {
                            if (info.getExercise() != null) {
                                org.apache.poi.ss.usermodel.Row dataRow = sheet.createRow(exerciseRowNum++);
                                dataRow.createCell(0).setCellValue(exNum++);
                                dataRow.createCell(1).setCellValue(info.getExercise().getName());
                                dataRow.createCell(2).setCellValue(info.getExercise().getMuscleGroup());
                                dataRow.createCell(3).setCellValue(info.getSets());
                                dataRow.createCell(4).setCellValue(info.getReps());

                                double weight = info.getExercise().getName().contains("ног") || info.getExercise().getName().contains("Присед")
                                        ? Math.round(currentUser.getWeight() * 0.6)
                                        : Math.round(currentUser.getWeight() * 0.4);
                                dataRow.createCell(5).setCellValue(weight);
                                dataRow.createCell(6).setCellValue(info.getExercise().getEquipment());

                                for (int c = 0; c < headers.length; c++) dataRow.getCell(c).setCellStyle(dataStyle);
                            }
                        }
                        exerciseRowNum++;
                        exNum = 1;
                    }
                }
                rowNum = exerciseRowNum + 1;
            }

            //  ИСТОРИЯ ВЕСА
            org.apache.poi.ss.usermodel.Row historyTitleRow = sheet.createRow(rowNum++);
            historyTitleRow.createCell(0).setCellValue(" ИСТОРИЯ ВЕСА");
            historyTitleRow.getCell(0).setCellStyle(titleStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowNum - 1, rowNum - 1, 0, 3));

            org.apache.poi.ss.usermodel.Row historyHeaderRow = sheet.createRow(rowNum++);
            String[] histHeaders = {"Дата", "Вес (кг)", "Изменение", "Примечание"};
            for (int i = 0; i < histHeaders.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = historyHeaderRow.createCell(i);
                cell.setCellValue(histHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            double prevWeight = currentUser.getWeight();
            org.apache.poi.ss.usermodel.Row initialRow = sheet.createRow(rowNum++);
            initialRow.createCell(0).setCellValue("При регистрации");
            initialRow.createCell(1).setCellValue(prevWeight);
            initialRow.createCell(2).setCellValue("0");
            initialRow.createCell(3).setCellValue("Начальная точка");
            for (int i = 0; i < 4; i++) initialRow.getCell(i).setCellStyle(dataStyle);

            try (java.sql.Connection conn = DatabaseManager.getConnection()) {
                List<UserProgress> history = new UserProgressDAO(conn).getHistory(currentUser.getId());
                for (UserProgress entry : history) {
                    org.apache.poi.ss.usermodel.Row histRow = sheet.createRow(rowNum++);
                    histRow.createCell(0).setCellValue(entry.getLogDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
                    histRow.createCell(1).setCellValue(entry.getWeight());
                    double change = entry.getWeight() - prevWeight;
                    String changeStr = (change > 0 ? "+" : "") + String.format("%.1f", change);
                    histRow.createCell(2).setCellValue(changeStr);
                    histRow.createCell(3).setCellValue(entry.getNote() != null ? entry.getNote() : "");
                    for (int i = 0; i < 4; i++) histRow.getCell(i).setCellStyle(dataStyle);
                    prevWeight = entry.getWeight();
                }
            }

            // Автоширина
            for (int i = 0; i < 7; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 512);
            }

            String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
            String filename = "progress_" + currentUser.getId() + "_" + timestamp + ".xlsx";
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(filename)) {
                workbook.write(fos);
            }
            showAlert("Успех", " Таблица прогресса сохранена!\n Файл: " + filename);

        } catch (Exception e) {
            showAlert("Ошибка", " Ошибка Excel: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // МЕТОД ТРАНСЛИТЕРАЦИИ
    private String transliterate(String text) {
        if (text == null || text.isEmpty()) {
            return "Unknown";
        }

        return text
                .replace("а", "a").replace("А", "A")
                .replace("б", "b").replace("Б", "B")
                .replace("в", "v").replace("В", "V")
                .replace("г", "g").replace("Г", "G")
                .replace("д", "d").replace("Д", "D")
                .replace("е", "e").replace("Е", "E")
                .replace("ё", "yo").replace("Ё", "Yo")
                .replace("ж", "zh").replace("Ж", "Zh")
                .replace("з", "z").replace("З", "Z")
                .replace("и", "i").replace("И", "I")
                .replace("й", "y").replace("Й", "Y")
                .replace("к", "k").replace("К", "K")
                .replace("л", "l").replace("Л", "L")
                .replace("м", "m").replace("М", "M")
                .replace("н", "n").replace("Н", "N")
                .replace("о", "o").replace("О", "O")
                .replace("п", "p").replace("П", "P")
                .replace("р", "r").replace("Р", "R")
                .replace("с", "s").replace("С", "S")
                .replace("т", "t").replace("Т", "T")
                .replace("у", "u").replace("У", "U")
                .replace("ф", "f").replace("Ф", "F")
                .replace("х", "kh").replace("Х", "Kh")
                .replace("ц", "ts").replace("Ц", "Ts")
                .replace("ч", "ch").replace("Ч", "Ch")
                .replace("ш", "sh").replace("Ш", "Sh")
                .replace("щ", "sch").replace("Щ", "Sch")
                .replace("ъ", "").replace("Ъ", "")
                .replace("ы", "y").replace("Ы", "Y")
                .replace("ь", "").replace("Ь", "")
                .replace("э", "e").replace("Э", "E")
                .replace("ю", "yu").replace("Ю", "Yu")
                .replace("я", "ya").replace("Я", "Ya");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}