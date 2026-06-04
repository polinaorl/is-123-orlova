package com.workout.app.view;

import com.workout.app.dao.UserDAO;
import com.workout.app.database.DatabaseManager;
import com.workout.app.model.User;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;

public class RegistrationView extends JFrame {

    public RegistrationView() {
        initUI();
        initComponents();
    }

    private void initUI() {
        setTitle("Регистрация нового пользователя");
        setSize(480, 640);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(new Color(245, 248, 250));
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.setBackground(new Color(245, 248, 250));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(245, 248, 250));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 1;

        int row = 0;

        // Заголовок
        JLabel title = new JLabel("Регистрация", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(44, 62, 80));
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        formPanel.add(title, gbc);
        gbc.gridwidth = 1;
        row++;

        // Поля ввода
        JTextField emailField = addField(formPanel, gbc, row++, "Email:");
        JPasswordField passField = addPassField(formPanel, gbc, row++, "Пароль:");
        JTextField heightField = addField(formPanel, gbc, row++, "Рост (см):");
        JTextField weightField = addField(formPanel, gbc, row++, "Вес (кг):");
        JTextField ageField = addField(formPanel, gbc, row++, "Возраст:");

        // Выпадающие списки
        JComboBox<String> genderCombo = new JComboBox<>(new String[]{"Мужской", "Женский"});
        styleComboBox(genderCombo);
        addComponent(formPanel, gbc, row++, "Пол:", genderCombo);

        JComboBox<String> goalCombo = new JComboBox<>(new String[]{
                "Набор мышечной массы", "Похудение", "Поддержание формы"
        });
        styleComboBox(goalCombo);
        addComponent(formPanel, gbc, row++, "Цель:", goalCombo);

        JComboBox<String> locCombo = new JComboBox<>(new String[]{
                "Спортивный зал", "Домашние условия", "Смешанные тренировки"
        });
        styleComboBox(locCombo);
        addComponent(formPanel, gbc, row++, "Место:", locCombo);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // ПАНЕЛЬ КНОПОК
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnPanel.setBackground(new Color(245, 248, 250));

        JButton regBtn = new JButton("Зарегистрироваться");
        regBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        regBtn.setBackground(new Color(46, 204, 113));
        regBtn.setForeground(Color.WHITE);
        regBtn.setFocusPainted(false);
        regBtn.setPreferredSize(new Dimension(170, 36));
        regBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JButton backBtn = new JButton("Назад ко входу");
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        backBtn.setBackground(new Color(149, 165, 166));
        backBtn.setForeground(Color.WHITE);
        backBtn.setFocusPainted(false);
        backBtn.setPreferredSize(new Dimension(150, 36));
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnPanel.add(regBtn);
        btnPanel.add(backBtn);

        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        // Обработчики
        regBtn.addActionListener(e -> register(
                emailField.getText(), new String(passField.getPassword()),
                heightField.getText(), weightField.getText(), ageField.getText(),
                (String) genderCombo.getSelectedItem(),
                (String) goalCombo.getSelectedItem(),
                (String) locCombo.getSelectedItem()
        ));

        backBtn.addActionListener(e -> {
            this.dispose();
            new LoginView().setVisible(true);
        });

        add(mainPanel);
        pack();
        setLocationRelativeTo(null);
    }

    // Вспомогательные методы (без изменений)
    private JTextField addField(JPanel panel, GridBagConstraints gbc, int row, String label) {
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(createLabel(label), gbc);
        gbc.gridx = 1;
        JTextField field = new JTextField();
        styleField(field);
        panel.add(field, gbc);
        return field;
    }

    private JPasswordField addPassField(JPanel panel, GridBagConstraints gbc, int row, String label) {
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(createLabel(label), gbc);
        gbc.gridx = 1;
        JPasswordField field = new JPasswordField();
        styleField(field);
        panel.add(field, gbc);
        return field;
    }

    private void addComponent(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent comp) {
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(createLabel(label), gbc);
        gbc.gridx = 1;
        panel.add(comp, gbc);
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbl.setForeground(new Color(60, 60, 60));
        return lbl;
    }

    private void styleField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(250, 32));
    }

    private void styleComboBox(JComboBox<String> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        combo.setPreferredSize(new Dimension(250, 32));
    }

    private void register(String email, String pass, String h, String w, String a, String gender, String goalFull, String locFull) {
        if (email.isEmpty() || pass.isEmpty() || h.isEmpty() || w.isEmpty() || a.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Заполните все поля!", "Ошибка", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            double height = Double.parseDouble(h);
            double weight = Double.parseDouble(w);
            int age = Integer.parseInt(a);

            String goal = "maintenance";
            switch (goalFull) {
                case "Набор мышечной массы" -> goal = "mass_gain";
                case "Похудение" -> goal = "weight_loss";
            }

            String loc = "mixed";
            switch (locFull) {
                case "Спортивный зал" -> loc = "gym";
                case "Домашние условия" -> loc = "home";
            }

            String genderCode = "Мужской".equals(gender) ? "male" : "female";

            try (Connection conn = DatabaseManager.getConnection()) {
                UserDAO dao = new UserDAO(conn);
                if (dao.existsByEmail(email)) {
                    JOptionPane.showMessageDialog(this, "Email уже занят!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                User newUser = new User(0, email, pass, height, weight, age, genderCode, goal, loc);
                dao.createUser(newUser);
                JOptionPane.showMessageDialog(this, "Успешно! Теперь войдите.");
                this.dispose();
                new LoginView().setVisible(true);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Неверный формат чисел!", "Ошибка", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Ошибка: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }
}