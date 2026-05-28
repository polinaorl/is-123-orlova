package com.workout.app.view.console;

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
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.sql.Connection;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class ConsoleProfileView {
    private User user;
    private UserProfileSubject subject;

    public ConsoleProfileView(User user) {
        this.user = user;
        this.subject = new UserProfileSubject(String.valueOf(user.getId()));
        subject.attach(new TrainingProgramObserver(user, this::refresh));
        subject.attach(new AchievementObserver(user.getEmail()));
        subject.attach(new ProgressLoggerObserver());
    }

    public void show() {
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\n═══════════════════════════════════════");
            System.out.println("    ПРОФИЛЬ: " + user.getEmail());
            System.out.println("═══════════════════════════════════════");
            System.out.println(" Цель: " + user.getGoalText());
            System.out.println(" Место: " + user.getLocationText());
            System.out.println(" Параметры: " + (int)user.getHeight() + " см, " +
                    (int)user.getWeight() + " кг, " + user.getAge() + " лет");

            double bmi = user.getWeight() / Math.pow(user.getHeight() / 100, 2);
            String bmiCat = getBMICategory(bmi);
            System.out.println(" ИМТ: " + String.format("%.1f", bmi) + " (" + bmiCat + ")");

            System.out.println("\n1.  Моя тренировка");
            System.out.println("2.  Сохранить прогресс");
            System.out.println("3.  Записать вес");
            System.out.println("4.  Сменить цель");
            System.out.println("5.  Выйти");
            System.out.print("\nВыбор: ");

            String choice = sc.nextLine();
            switch (choice) {
                case "1" -> new ConsoleWorkoutView(user).show();
                case "2" -> saveProgressMenu(sc);
                case "3" -> logWeight(sc);
                case "4" -> changeGoal(sc);
                case "5" -> { return; }
                default -> System.out.println(" Неверный выбор!");
            }
        }
    }

    private void logWeight(Scanner sc) {
        System.out.print("Введите текущий вес (кг): ");
        try {
            String input = sc.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println(" Введите число!");
                return;
            }
            double weight = Double.parseDouble(input);
            try (Connection conn = DatabaseManager.getConnection()) {
                UserProgressDAO dao = new UserProgressDAO(conn);
                dao.saveWeight(user.getId(), weight, "Записано из консоли");
            }


            if (subject != null) {
                subject.onWeightChanged(weight);
            }

            System.out.println(" Вес " + weight + " кг успешно записан!");
        } catch (NumberFormatException e) {
            System.out.println(" Ошибка: введите корректное число!");
        } catch (Exception e) {
            System.out.println(" Ошибка БД: " + e.getMessage());
        }
    }

    private void changeGoal(Scanner sc) {
        String[] g = {"mass_gain", "weight_loss", "maintenance"};
        String[] n = {"Набор массы", "Похудение", "Поддержание"};
        int cur = 0;
        for(int i = 0; i < g.length; i++) {
            if(g[i].equals(user.getGoal())) {
                cur = i;
                break;
            }
        }
        int next = (cur + 1) % 3;
        user.setGoal(g[next]);
        subject.onGoalChanged(g[next]);
        System.out.println(" Цель изменена: " + n[next]);
    }

    private void saveProgressMenu(Scanner sc) {
        System.out.println("\n═══════════════════════════════════════");
        System.out.println("    СОХРАНИТЬ ПРОГРЕСС");
        System.out.println("═══════════════════════════════════════");
        System.out.println("1.  Excel таблица");
        System.out.println("2.  PDF файл");
        System.out.println("3.  DOC файл");
        System.out.println("0.  Назад");
        System.out.print("\nВыбор: ");

        String choice = sc.nextLine();
        switch (choice) {
            case "1" -> {
                try { saveProgressToExcel(); }
                catch (Exception e) { System.out.println(" Ошибка Excel: " + e.getMessage()); }
            }
            case "2" -> {
                try { saveProgressToPdf(); }
                catch (Exception e) { System.out.println(" Ошибка PDF: " + e.getMessage()); }
            }
            case "3" -> {
                try { saveProgressToDoc(); }
                catch (Exception e) { System.out.println(" Ошибка DOC: " + e.getMessage()); }
            }
            case "0" -> { /* ничего */ }
            default -> System.out.println(" Неверный выбор!");
        }
    }

    private String getWorkoutPlanText() {
        StringBuilder sb = new StringBuilder();
        try (Connection conn = DatabaseManager.getConnection()) {
            WorkoutProgramDAO progDao = new WorkoutProgramDAO(conn);
            ProgramExerciseDAO peDao = new ProgramExerciseDAO(conn);

            List<WorkoutProgram> programs = progDao.findAll();
            for (WorkoutProgram prog : programs) {
                if (prog.getGoal().equals(user.getGoal()) &&
                        prog.getLocation().equals(user.getLocation())) {

                    sb.append("\n").append(prog.getName().toUpperCase()).append("\n");
                    sb.append("=".repeat(50)).append("\n");

                    List<ProgramExerciseInfo> infos = peDao.getExercisesForProgram(prog.getId());
                    int num = 1;
                    for (ProgramExerciseInfo info : infos) {
                        if (info.getExercise() != null) {
                            sb.append("  ").append(num++).append(". ")
                                    .append(info.getExercise().getName())
                                    .append(" [").append(info.getExercise().getMuscleGroup()).append("]")
                                    .append(" — ").append(info.getSets())
                                    .append(" x ").append(info.getReps()).append("\n");
                        }
                    }
                    sb.append("\n");
                }
            }
        } catch (Exception e) {
            sb.append("Ошибка загрузки программы: ").append(e.getMessage()).append("\n");
        }
        return sb.toString();
    }

    //  DOC русски1 текст
    private void saveProgressToDoc() throws Exception {
        StringBuilder sb = new StringBuilder("<html><body style='font-family:Arial; padding:20px;'>");
        sb.append("<h2 style='color:#2c3e50;' История прогресса: ").append(user.getEmail()).append("</h2>");
        sb.append("<p><b>Параметры:</b> ").append(user.getHeight()).append(" см | ")
                .append(user.getWeight()).append(" кг | ").append(user.getAge()).append(" лет</p>");
        sb.append("<h3> История веса:</h3>");
        sb.append("<table border='1' cellspacing='0' cellpadding='5'>");
        sb.append("<tr style='background:#e8f4f8;'><th>Дата</th><th>Вес (кг)</th><th>Изменение</th><th>Примечание</th></tr>");

        double prevWeight = user.getWeight();
        sb.append("<tr><td>При регистрации</td><td>").append(prevWeight).append("</td><td>0</td><td>Начальная точка</td></tr>");

        try (Connection c = DatabaseManager.getConnection()) {
            UserProgressDAO dao = new UserProgressDAO(c);
            List<UserProgress> hist = dao.getHistory(user.getId());

            for (UserProgress p : hist) {
                double change = p.getWeight() - prevWeight;
                String changeStr = (change > 0 ? "+" : "") + String.format("%.1f", change);

                sb.append("<tr>")
                        .append("<td>").append(p.getLogDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))).append("</td>")
                        .append("<td>").append(p.getWeight()).append("</td>")
                        .append("<td>").append(changeStr).append(" кг</td>")
                        .append("<td>").append(p.getNote() != null ? p.getNote() : "").append("</td>")
                        .append("</tr>");

                prevWeight = p.getWeight();
            }
        }

        sb.append("</table>");
        sb.append("<h3> Программа тренировок:</h3><pre>")
                .append(getWorkoutPlanText())
                .append("</pre></body></html>");

        String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
        String fn = "progress_" + user.getId() + "_" + timestamp + ".doc";
        java.nio.file.Files.writeString(java.nio.file.Paths.get(fn), sb.toString());
        System.out.println(" DOC сохранён: " + fn);
    }

    //  PDF с транслитерацией русских названий
    private void saveProgressToPdf() throws Exception {
        String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
        String filename = "progress_" + user.getId() + "_" + timestamp + ".pdf";

        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(filename));
        document.open();

        com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 16, com.itextpdf.text.Font.BOLD);
        com.itextpdf.text.Font normalFont = new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 11, com.itextpdf.text.Font.NORMAL);
        com.itextpdf.text.Font boldFont = new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.BOLD);

        document.add(new Paragraph("WORKOUT PROGRESS REPORT", titleFont));
        document.add(new Paragraph(" "));

        document.add(new Paragraph("User: " + user.getEmail(), normalFont));
        document.add(new Paragraph("Height: " + user.getHeight() + " cm", normalFont));
        document.add(new Paragraph("Weight: " + user.getWeight() + " kg", normalFont));
        document.add(new Paragraph("Age: " + user.getAge() + " years", normalFont));
        double bmi = user.getWeight() / Math.pow(user.getHeight() / 100, 2);
        document.add(new Paragraph("BMI: " + String.format("%.1f", bmi), normalFont));
        document.add(new Paragraph(" "));
        document.add(new Paragraph("--------------------------------------------------", normalFont));
        document.add(new Paragraph(" "));

        // ИСТОРИЯ ВЕСА
        document.add(new Paragraph("WEIGHT HISTORY", boldFont));
        document.add(new Paragraph(" ", normalFont));

        double prevWeight = user.getWeight();
        document.add(new Paragraph("* Registration: " + prevWeight + " kg (baseline)", normalFont));

        try (Connection conn = DatabaseManager.getConnection()) {
            UserProgressDAO dao = new UserProgressDAO(conn);
            List<UserProgress> hist = dao.getHistory(user.getId());

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

        // Программа тренировок с трансл
        document.add(new Paragraph("WORKOUT PROGRAM", boldFont));
        document.add(new Paragraph(" ", normalFont));

        try (Connection conn = DatabaseManager.getConnection()) {
            WorkoutProgramDAO progDao = new WorkoutProgramDAO(conn);
            ProgramExerciseDAO peDao = new ProgramExerciseDAO(conn);

            List<WorkoutProgram> programs = progDao.findAll();
            int exerciseNum = 1;

            for (WorkoutProgram prog : programs) {
                if (prog.getGoal().equals(user.getGoal()) && prog.getLocation().equals(user.getLocation())) {

                    // Транслитерируем название программы
                    String progNameTranslit = transliterate(prog.getName());
                    document.add(new Paragraph(">> " + progNameTranslit.toUpperCase(), boldFont));
                    document.add(new Paragraph("   Goal: " + prog.getGoal() + " | Location: " + prog.getLocation(), normalFont));
                    document.add(new Paragraph(" ", normalFont));

                    List<ProgramExerciseInfo> infos = peDao.getExercisesForProgram(prog.getId());
                    for (ProgramExerciseInfo info : infos) {
                        if (info.getExercise() != null) {
                            //  Транслитерируем название упражнения и группу мышц
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
        System.out.println(" PDF saved: " + filename);
    }

    //  (русский -> английский)
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

    //  EXCEL с программой тренировок
    private void saveProgressToExcel() throws Exception {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sh = wb.createSheet("Прогресс тренировок");

            org.apache.poi.ss.usermodel.Font titleFont = wb.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);

            org.apache.poi.ss.usermodel.Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);

            CellStyle titleStyle = wb.createCellStyle();
            titleStyle.setFont(titleFont);
            titleStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle headerStyle = wb.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            CellStyle dataStyle = wb.createCellStyle();
            dataStyle.setAlignment(HorizontalAlignment.CENTER);
            dataStyle.setBorderBottom(BorderStyle.THIN);

            int rowNum = 0;

            Row titleRow = sh.createRow(rowNum++);
            titleRow.createCell(0).setCellValue("ПРОГРЕСС: " + user.getEmail());
            titleRow.getCell(0).setCellStyle(titleStyle);
            sh.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

            rowNum++;
            sh.createRow(rowNum).createCell(0).setCellValue("Параметры: " + user.getHeight() + " см | " +
                    user.getWeight() + " кг | " + user.getAge() + " лет");
            rowNum += 2;

            // ИСТОРИЯ ВЕСА
            Row weightTitleRow = sh.createRow(rowNum++);
            weightTitleRow.createCell(0).setCellValue("ИСТОРИЯ ВЕСА");
            weightTitleRow.getCell(0).setCellStyle(headerStyle);
            sh.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 3));

            String[] weightHeaders = {"Дата", "Вес (кг)", "Изменение", "Примечание"};
            Row wh = sh.createRow(rowNum++);
            for (int i = 0; i < weightHeaders.length; i++) {
                Cell cell = wh.createCell(i);
                cell.setCellValue(weightHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            double prevWeight = user.getWeight();
            Row initialRow = sh.createRow(rowNum++);
            initialRow.createCell(0).setCellValue("При регистрации");
            initialRow.createCell(1).setCellValue(prevWeight);
            initialRow.createCell(2).setCellValue("0");
            initialRow.createCell(3).setCellValue("Начало");
            for (int i = 0; i < 4; i++) initialRow.getCell(i).setCellStyle(dataStyle);

            try (Connection c = DatabaseManager.getConnection()) {
                UserProgressDAO dao = new UserProgressDAO(c);
                List<UserProgress> hist = dao.getHistory(user.getId());

                for (UserProgress p : hist) {
                    Row row = sh.createRow(rowNum++);
                    row.createCell(0).setCellValue(p.getLogDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
                    row.createCell(1).setCellValue(p.getWeight());

                    double change = p.getWeight() - prevWeight;
                    String changeStr = (change > 0 ? "+" : "") + String.format("%.1f", change);
                    row.createCell(2).setCellValue(changeStr);
                    row.createCell(3).setCellValue(p.getNote() != null ? p.getNote() : "");

                    for (int i = 0; i < 4; i++) row.getCell(i).setCellStyle(dataStyle);
                    prevWeight = p.getWeight();
                }
            }

            rowNum += 2;

            // ПРОГРАММА ТРЕНИРОВОК
            Row progTitleRow = sh.createRow(rowNum++);
            progTitleRow.createCell(0).setCellValue("ПРОГРАММА ТРЕНИРОВОК");
            progTitleRow.getCell(0).setCellStyle(headerStyle);
            sh.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 6));

            String[] progHeaders = {"№", "Упражнение", "Группа мышц", "Подходы", "Повторения", "Вес (кг)", "Инвентарь"};
            Row ph = sh.createRow(rowNum++);
            for (int i = 0; i < progHeaders.length; i++) {
                Cell cell = ph.createCell(i);
                cell.setCellValue(progHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            try (Connection conn = DatabaseManager.getConnection()) {
                WorkoutProgramDAO progDao = new WorkoutProgramDAO(conn);
                ProgramExerciseDAO peDao = new ProgramExerciseDAO(conn);

                List<WorkoutProgram> programs = progDao.findAll();
                int exerciseRowNum = rowNum;
                int exNum = 1;

                for (WorkoutProgram prog : programs) {
                    if (prog.getGoal().equals(user.getGoal()) && prog.getLocation().equals(user.getLocation())) {
                        Row progRow = sh.createRow(exerciseRowNum++);
                        progRow.createCell(0).setCellValue(prog.getName());
                        progRow.getCell(0).setCellStyle(headerStyle);
                        sh.addMergedRegion(new CellRangeAddress(exerciseRowNum-1, exerciseRowNum-1, 0, 6));

                        List<ProgramExerciseInfo> infos = peDao.getExercisesForProgram(prog.getId());
                        for (ProgramExerciseInfo info : infos) {
                            if (info.getExercise() != null) {
                                Row dataRow = sh.createRow(exerciseRowNum++);
                                dataRow.createCell(0).setCellValue(exNum++);
                                dataRow.createCell(1).setCellValue(info.getExercise().getName());
                                dataRow.createCell(2).setCellValue(info.getExercise().getMuscleGroup());
                                dataRow.createCell(3).setCellValue(info.getSets());
                                dataRow.createCell(4).setCellValue(info.getReps());

                                double weight = info.getExercise().getName().contains("ног") ||
                                        info.getExercise().getName().contains("Присед") ?
                                        Math.round(user.getWeight() * 0.6) :
                                        Math.round(user.getWeight() * 0.4);
                                dataRow.createCell(5).setCellValue(weight);
                                dataRow.createCell(6).setCellValue(info.getExercise().getEquipment());

                                for (int c = 0; c < 7; c++) dataRow.getCell(c).setCellStyle(dataStyle);
                            }
                        }
                        exerciseRowNum++;
                        exNum = 1;
                    }
                }
            }

            for (int i = 0; i < 7; i++) {
                sh.autoSizeColumn(i);
                sh.setColumnWidth(i, sh.getColumnWidth(i) + 512);
            }

            String ts = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
            String fn = "progress_" + user.getId() + "_" + ts + ".xlsx";
            try (FileOutputStream fo = new FileOutputStream(fn)) {
                wb.write(fo);
            }
            System.out.println(" Excel сохранён: " + fn);

        } catch (Exception e) {
            System.out.println(" Ошибка Excel: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private String getBMICategory(double bmi) {
        if (bmi < 18.5) return "Недостаточный вес";
        if (bmi < 25) return "Норма";
        if (bmi < 30) return "Избыточный вес";
        return "Ожирение";
    }

    private void refresh() {
        System.out.println(" Программа обновлена для новой цели!");
    }
}