package com.workout.app.view;

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


import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ProfileView extends JFrame {
    private User currentUser;
    private UserProfileSubject profileSubject;

    public ProfileView(User user) {
        this.currentUser = user;
        initUI();
        initComponents();
        initObservers();
    }

    private void initUI() {
        setTitle("Профиль пользователя");
        setSize(520, 540); // 🔥 Чуть больше для новой кнопки
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(new Color(245, 248, 250));
    }

    private void initComponents() {
        JPanel main = new JPanel(new BorderLayout(20, 20));
        main.setBackground(new Color(245, 248, 250));
        main.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JLabel title = new JLabel("Ваш профиль", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(44, 62, 80));
        main.add(title, BorderLayout.NORTH);

        JPanel infoCard = new JPanel();
        infoCard.setLayout(new BoxLayout(infoCard, BoxLayout.Y_AXIS));
        infoCard.setBackground(Color.WHITE);
        infoCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 210, 220), 1, true),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        infoCard.setMaximumSize(new Dimension(420, Short.MAX_VALUE));

        JPanel gridPanel = new JPanel(new GridBagLayout());
        gridPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(6, 0, 6, 0);

        gbc.gridy = 0;
        gridPanel.add(createInfoLine("Email:", currentUser.getEmail()), gbc);
        gbc.gridy = 1;
        gridPanel.add(createInfoLine("Цель:", currentUser.getGoalText()), gbc);
        gbc.gridy = 2;
        gridPanel.add(createInfoLine("Место:", currentUser.getLocationText()), gbc);
        gbc.gridy = 3;
        gridPanel.add(createInfoLine("Параметры:",
                (int)currentUser.getHeight() + " см | " + (int)currentUser.getWeight() + " кг | " + currentUser.getAge() + " лет"), gbc);

        double bmi = calculateBMI(currentUser.getWeight(), currentUser.getHeight());
        String bmiCategory = getBMICategory(bmi);
        Color bmiColor = getBMIColor(bmiCategory);

        gbc.gridy = 4;
        JPanel bmiPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bmiPanel.setBackground(Color.WHITE);
        JLabel bmiLabel = new JLabel("ИМТ:");
        bmiLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        bmiLabel.setForeground(new Color(60, 60, 60));

        JLabel bmiValue = new JLabel(String.format("%.1f", bmi) + " (" + bmiCategory + ")");
        bmiValue.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        bmiValue.setForeground(bmiColor);

        bmiPanel.add(bmiLabel);
        bmiPanel.add(bmiValue);
        gridPanel.add(bmiPanel, gbc);

        infoCard.add(gridPanel);
        main.add(infoCard, BorderLayout.CENTER);

        //  КНОПКИ
        JPanel btnPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        btnPanel.setBackground(new Color(245, 248, 250));
        btnPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton workoutBtn = createStyledButton(" Программа", new Color(52, 152, 219));
        JButton progressBtn = createStyledButton(" Прогресс", new Color(46, 204, 113));
        JButton logBtn = createStyledButton(" Записать вес", new Color(241, 196, 15)); // 🔥 НОВАЯ
        JButton logoutBtn = createStyledButton(" Выйти", new Color(231, 76, 60));

        workoutBtn.addActionListener(e -> {
            this.dispose();
            new WorkoutProgramView(currentUser).setVisible(true);
        });
        progressBtn.addActionListener(e -> showProgressSave());
        logBtn.addActionListener(e -> logWeight());
        logoutBtn.addActionListener(e -> {
            this.dispose();
            new LoginView().setVisible(true);
        });

        btnPanel.add(logBtn);
        btnPanel.add(progressBtn);
        btnPanel.add(workoutBtn);
        btnPanel.add(logoutBtn);

        main.add(btnPanel, BorderLayout.SOUTH);
        add(main);
    }

    private JPanel createInfoLine(String label, String value) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panel.setBackground(Color.WHITE);
        panel.setMaximumSize(new Dimension(380, 28));
        JLabel lbl = new JLabel("<html><b>" + label + "</b> " + value + "</html>");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(new Color(60, 60, 60));
        panel.add(lbl);
        return panel;
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

    private Color getBMIColor(String category) {
        switch (category) {
            case "Норма": return new Color(39, 174, 96);
            case "Недостаточный вес": return new Color(241, 196, 15);
            case "Избыточный вес": return new Color(230, 126, 34);
            default: return new Color(231, 76, 60);
        }
    }

    private JButton createStyledButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
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

    // Запись веса в БД
    private void logWeight() {
        String w = JOptionPane.showInputDialog(this,
                "Введите текущий вес (кг):",
                " Запись веса",
                JOptionPane.QUESTION_MESSAGE);

        if (w != null && !w.trim().isEmpty()) {
            try {
                double weight = Double.parseDouble(w.trim());
                try (Connection conn = DatabaseManager.getConnection()) {
                    new UserProgressDAO(conn).saveWeight(
                            currentUser.getId(),
                            weight,
                            "Записано из приложения"
                    );
                }


                if (profileSubject != null) {
                    profileSubject.onWeightChanged(weight);
                }

                JOptionPane.showMessageDialog(this,
                        " Вес " + weight + " кг записан!",
                        "Успех",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                        " Введите корректное число!",
                        "Ошибка",
                        JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        " Ошибка: " + e.getMessage(),
                        "Ошибка",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private void showProgressSave() {
        String[] options = {"DOC файл", "PDF файл", "Excel таблица"};
        String selected = (String) JOptionPane.showInputDialog(this,
                "Выберите формат сохранения:",
                "Сохранение прогресса",
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        if (selected != null) {
            try {
                switch (selected) {
                    case "DOC файл" -> saveProgressToDoc();
                    case "PDF файл" -> saveProgressToPdf();
                    case "Excel таблица" -> saveProgressToExcel();
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Ошибка: " + e.getMessage());
            }
        }
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

    private void saveProgressToDoc() {
        try {
            String workoutText = getWorkoutPlanText();
            double bmi = calculateBMI(currentUser.getWeight(), currentUser.getHeight());

            // Получаем историю веса
            StringBuilder historyText = new StringBuilder();
            historyText.append("Дата\t\t\tВес (кг)\tИзменение\n");
            historyText.append("--------------------------------------------------\n");

            double previousWeight = currentUser.getWeight();
            historyText.append("При регистрации\t").append(previousWeight).append("\t\t0 (начало)\n");

            try (Connection conn = DatabaseManager.getConnection()) {
                List<UserProgress> history = new UserProgressDAO(conn).getHistory(currentUser.getId());
                for (UserProgress entry : history) {
                    double change = entry.getWeight() - previousWeight;
                    String changeStr = (change > 0 ? "+" : "") + String.format("%.1f", change);
                    historyText.append(entry.getLogDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")))
                            .append("\t").append(entry.getWeight())
                            .append("\t\t").append(changeStr).append(" кг\n");
                    previousWeight = entry.getWeight();
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

            String filename = "progress_" + currentUser.getId() + "_" + new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date()) + ".doc";
            java.nio.file.Files.writeString(java.nio.file.Paths.get(filename), html);

            JOptionPane.showMessageDialog(this, " Отчёт сохранён в DOC файл!\n📁 Файл: " + filename, "Успех", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, " Не удалось сохранить DOC: " + e.getMessage());
        }
    }

    //  PDF с транслитерацией и программой тренировок
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

            // Параметры пользователя
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
            JOptionPane.showMessageDialog(this, " PDF saved: " + filename, "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, " PDF Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //  ( русский -> английский)
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


    //  ЭКСПОРТ В EXCEL с знаками + и -
    private void saveProgressToExcel() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Прогресс тренировок");

            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setAlignment(HorizontalAlignment.CENTER);
            dataStyle.setBorderBottom(BorderStyle.THIN);

            int rowNum = 0;

            // ИНФОРМАЦИЯ О ПОЛЬЗОВАТЕЛЕ
            Row userInfoRow = sheet.createRow(rowNum++);
            userInfoRow.createCell(0).setCellValue(" ИНФОРМАЦИЯ О ПОЛЬЗОВАТЕЛЕ");
            userInfoRow.getCell(0).setCellStyle(headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 3));

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
                Row r = sheet.createRow(rowNum++);
                r.createCell(0).setCellValue(row[0]);
                r.createCell(1).setCellValue(row[1]);
                for (int i = 0; i < 2; i++) r.getCell(i).setCellStyle(dataStyle);
            }
            rowNum++;

            // ПРОГРАММА ТРЕНИРОВОК
            Row workoutTitleRow = sheet.createRow(rowNum++);
            workoutTitleRow.createCell(0).setCellValue(" ПРОГРАММА ТРЕНИРОВОК");
            workoutTitleRow.getCell(0).setCellStyle(headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 6));

            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {"№", "Упражнение", "Группа мышц", "Подходы", "Повторения", "Вес (кг)", "Инвентарь"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            try (Connection conn = DatabaseManager.getConnection()) {
                WorkoutProgramDAO progDao = new WorkoutProgramDAO(conn);
                ProgramExerciseDAO peDao = new ProgramExerciseDAO(conn);

                List<WorkoutProgram> programs = progDao.findAll();
                int exerciseRowNum = rowNum;

                for (WorkoutProgram prog : programs) {
                    if (prog.getGoal().equals(currentUser.getGoal()) && prog.getLocation().equals(currentUser.getLocation())) {
                        Row titleRow = sheet.createRow(exerciseRowNum++);
                        Cell titleCell = titleRow.createCell(0);
                        titleCell.setCellValue(" " + prog.getName());
                        titleCell.setCellStyle(headerStyle);
                        sheet.addMergedRegion(new CellRangeAddress(exerciseRowNum - 1, exerciseRowNum - 1, 0, headers.length - 1));

                        List<ProgramExerciseInfo> exercises = peDao.getExercisesForProgram(prog.getId());
                        int exNum = 1;

                        for (ProgramExerciseInfo info : exercises) {
                            if (info.getExercise() != null) {
                                Row dataRow = sheet.createRow(exerciseRowNum++);
                                dataRow.createCell(0).setCellValue(exNum++);
                                dataRow.createCell(1).setCellValue(info.getExercise().getName());
                                dataRow.createCell(2).setCellValue(info.getExercise().getMuscleGroup());
                                dataRow.createCell(3).setCellValue(info.getSets());
                                dataRow.createCell(4).setCellValue(info.getReps());

                                double weight = calculateRecommendedWeight(info.getExercise().getName(), currentUser.getWeight());
                                dataRow.createCell(5).setCellValue(weight);
                                dataRow.createCell(6).setCellValue(info.getExercise().getEquipment());

                                for (int c = 0; c < headers.length; c++) {
                                    dataRow.getCell(c).setCellStyle(dataStyle);
                                }
                            }
                        }
                        exerciseRowNum++;
                    }
                }
                rowNum = exerciseRowNum + 1;
            }

            // ИСТОРИЯ ВЕСА (с знаками + и -)
            Row historyTitleRow = sheet.createRow(rowNum++);
            historyTitleRow.createCell(0).setCellValue(" ИСТОРИЯ ВЕСА");
            historyTitleRow.getCell(0).setCellStyle(headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 3));

            Row historyHeaderRow = sheet.createRow(rowNum++);
            String[] histHeaders = {"Дата", "Вес (кг)", "Изменение", "Примечание"};
            for (int i = 0; i < histHeaders.length; i++) {
                Cell cell = historyHeaderRow.createCell(i);
                cell.setCellValue(histHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            // Начальный вес
            double previousWeight = currentUser.getWeight();
            Row initialRow = sheet.createRow(rowNum++);
            initialRow.createCell(0).setCellValue("При регистрации");
            initialRow.createCell(1).setCellValue(previousWeight);
            initialRow.createCell(2).setCellValue("0");
            initialRow.createCell(3).setCellValue("Начальная точка");
            for (int i = 0; i < 4; i++) initialRow.getCell(i).setCellStyle(dataStyle);

            // История из БД
            try (Connection conn = DatabaseManager.getConnection()) {
                List<UserProgress> history = new UserProgressDAO(conn).getHistory(currentUser.getId());

                for (UserProgress entry : history) {
                    Row histRow = sheet.createRow(rowNum++);
                    histRow.createCell(0).setCellValue(entry.getLogDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
                    histRow.createCell(1).setCellValue(entry.getWeight());


                    double change = entry.getWeight() - previousWeight;
                    String changeStr = (change > 0 ? "+" : "") + String.format("%.1f", change);
                    histRow.createCell(2).setCellValue(changeStr);

                    histRow.createCell(3).setCellValue(entry.getNote() != null ? entry.getNote() : "");

                    for (int i = 0; i < 4; i++) histRow.getCell(i).setCellStyle(dataStyle);
                    previousWeight = entry.getWeight();
                }
            }

            for (int i = 0; i < 7; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 512);
            }

            String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
            String filename = "progress_" + currentUser.getId() + "_" + timestamp + ".xlsx";
            try (FileOutputStream fos = new FileOutputStream(filename)) {
                workbook.write(fos);
            }

            JOptionPane.showMessageDialog(this, " Таблица сохранена!\n Файл: " + filename, "Успех", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, " Ошибка Excel: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private double calculateRecommendedWeight(String exerciseName, double userWeight) {
        double baseWeight = userWeight * 0.4;
        double factor = 1.0;

        if (exerciseName.contains("ног") || exerciseName.contains("Приседания") || exerciseName.contains("Становая")) {
            factor *= 1.5;
        } else if (exerciseName.contains("рук") || exerciseName.contains("бицепс") || exerciseName.contains("трицепс")) {
            factor *= 0.6;
        }
        return Math.round(baseWeight * factor);
    }

    private void saveProgressAsScreenshot() {
        SwingUtilities.invokeLater(() -> {
            try {
                this.dispose();
                WorkoutProgramView workoutWindow = new WorkoutProgramView(currentUser);
                workoutWindow.setVisible(true);

                JOptionPane.showMessageDialog(workoutWindow,
                        "Нажмите OK для сохранения скриншота",
                        "Скриншот",
                        JOptionPane.INFORMATION_MESSAGE);

                Thread.sleep(500);

                Robot robot = new Robot();
                Rectangle bounds = workoutWindow.getBounds();
                bounds.setLocation(workoutWindow.getLocationOnScreen());
                BufferedImage screenshot = robot.createScreenCapture(bounds);

                ImageIO.write(screenshot, "png",
                        new File("progress_screenshot_" + currentUser.getId() + ".png"));

                JOptionPane.showMessageDialog(workoutWindow,
                        " Скриншот сохранён!", "Успех", JOptionPane.INFORMATION_MESSAGE);

                workoutWindow.dispose();
                new ProfileView(currentUser).setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}