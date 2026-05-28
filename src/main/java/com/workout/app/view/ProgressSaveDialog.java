package com.workout.app.view;

import com.workout.app.model.User;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.Date;

public class ProgressSaveDialog extends JDialog {
    private User user;

    public ProgressSaveDialog(Frame parent, User user) {
        super(parent, " Сохранение прогресса", true);
        this.user = user;
        initUI();
    }

    private void initUI() {
        setSize(420, 280);
        setLocationRelativeTo(getParent());
        setLayout(new GridLayout(4, 1, 15, 15));
        getContentPane().setBackground(new Color(245, 248, 250));

        JLabel title = new JLabel("Выберите формат сохранения отчёта:", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        add(title);

        JButton docBtn = createButton(" Сохранить в DOC (Word)", new Color(41, 128, 185));
        docBtn.addActionListener(e -> saveToDoc());
        add(docBtn);

       // JButton photoBtn = createButton(" Сохранить как Фото (PNG)", new Color(39, 174, 96));
        //photoBtn.addActionListener(e -> saveToPhoto());
       // add(photoBtn);

        JButton pdfBtn = createButton(" Сохранить в PDF", new Color(192, 57, 43));
        pdfBtn.addActionListener(e -> saveToPdf());
        add(pdfBtn);
    }

    private JButton createButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    //  DOC
    private void saveToDoc() {
        try {
            String html = "<html><body style='font-family: Arial; padding: 20px;'>" +
                    "<h2 style='color: #2c3e50;'>Отчёт о прогрессе тренировок</h2>" +
                    "<hr>" +
                    "<p><b> Пользователь:</b> " + user.getEmail() + "</p>" +
                    "<p><b> Дата сохранения:</b> " + new Date() + "</p>" +
                    "<p><b> Текущая цель:</b> " + user.getGoalText() + "</p>" +
                    "<p><b> Место:</b> " + user.getLocationText() + "</p>" +
                    "<p><b> Параметры:</b> Рост " + user.getHeight() + " см | Вес " + user.getWeight() + " кг | Возраст " + user.getAge() + " лет</p>" +
                    "<p><b> Статус:</b> Прогресс успешно зафиксирован в системе.</p>" +
                    "</body></html>";

            File file = new File("workout_progress.doc");
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(html);
            }
            JOptionPane.showMessageDialog(this,
                    " Файл сохранён!\n Путь: " + file.getAbsolutePath() +
                            "\n Откроется в Microsoft Word.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, " Ошибка DOC: " + e.getMessage());
        }
    }

    //  ФОТО
    //private void saveToPhoto() {
      //  try {
        //    Robot robot = new Robot();
            // Делаем скриншот только родительского окна (аккуратнее)
          //  Rectangle bounds = ((Window) getParent()).getBounds();
           // BufferedImage capture = robot.createScreenCapture(bounds);

           // File file = new File("workout_progress.png");
           // ImageIO.write(capture, "png", file);

          //  JOptionPane.showMessageDialog(this,
           //         " Скриншот сохранён!\n Путь: " + file.getAbsolutePath());
       // } catch (Exception e) {
        //    JOptionPane.showMessageDialog(this, " Ошибка Фото: " + e.getMessage());
       // }
  //  }

    // PDF: Генерация отчёта через iText
    private void saveToPdf() {
        try {
            File file = new File("workout_progress.pdf");
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            document.add(new Paragraph("ОТЧЁТ О ПРОГРЕССЕ ТРЕНИРОВОК"));
            document.add(new Paragraph("Дата: " + new Date()));
            document.add(new Paragraph("Пользователь: " + user.getEmail()));
            document.add(new Paragraph("Цель: " + user.getGoalText()));
            document.add(new Paragraph("Место: " + user.getLocationText()));
            document.add(new Paragraph("Параметры: Рост " + user.getHeight() + " см, Вес " + user.getWeight() + " кг"));
            document.add(new Paragraph("Статус: Прогресс зафиксирован"));

            document.close();
            JOptionPane.showMessageDialog(this,
                    " PDF сохранён!\n Путь: " + file.getAbsolutePath());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, " Ошибка PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }
}