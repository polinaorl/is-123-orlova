package com.workout.app.view;

import com.workout.app.dao.UserDAO;
import com.workout.app.database.DatabaseManager;
import com.workout.app.model.User;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.util.Optional;

public class LoginView extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;

    public LoginView() {
        initUI();
        initComponents();
    }

    private void initUI() {
        setTitle("Вход в систему");
        setSize(500, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(new Color(245, 248, 250));
    }

    private void initComponents() {
        JPanel main = new JPanel(new GridBagLayout());
        main.setBackground(new Color(245, 248, 250));
        main.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 15, 12, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Заголовок
        JLabel title = new JLabel("Вход в систему", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(44, 62, 80));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        main.add(title, gbc);

        // Поле Email
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        main.add(createStyledLabel("Email:"), gbc);
        gbc.gridx = 1;
        emailField = createStyledField();
        main.add(emailField, gbc);

        // Поле Пароль
        gbc.gridx = 0;
        gbc.gridy = 2;
        main.add(createStyledLabel("Пароль:"), gbc);
        gbc.gridx = 1;
        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setPreferredSize(new Dimension(250, 35));
        main.add(passwordField, gbc);

        // Панель с 3 кнопками
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        btnPanel.setBackground(new Color(245, 248, 250));

        // Кнопка Войти
        JButton loginBtn = createStyledButton("Войти", new Color(52, 152, 219));
        //  Кнопка Регистрация
        JButton regBtn = createStyledButton("Регистрация", new Color(46, 204, 113));
        //  Кнопка Админ
        JButton adminBtn = createStyledButton("Админ", new Color(149, 165, 166));

        loginBtn.addActionListener(e -> onLogin());
        regBtn.addActionListener(e -> {
            this.dispose();
            new RegistrationView().setVisible(true);
        });
        adminBtn.addActionListener(e -> new AdminLoginDialog(this).setVisible(true));

        btnPanel.add(loginBtn);
        btnPanel.add(regBtn);
        btnPanel.add(adminBtn);
        main.add(btnPanel, gbc);



        add(main);
    }

    private JLabel createStyledLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbl.setForeground(new Color(60, 60, 60));
        return lbl;
    }

    private JTextField createStyledField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(250, 35));
        return field;
    }

    private JButton createStyledButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(140, 40)); // Чуть уже, чтобы влезли 3 кнопки
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void onLogin() {
        String email = emailField.getText().trim();
        String pass = new String(passwordField.getPassword());

        if (email.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Введите email и пароль!", "Ошибка", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Проверка на админа
        if ("admin".equals(email) && "admin".equals(pass)) {

            this.dispose();
            new AdminView().setVisible(true);
            return;
        }

        // Проверка обычного пользователя
        try (Connection conn = DatabaseManager.getConnection()) {
            Optional<User> userOpt = new UserDAO(conn).findByEmail(email);

            if (userOpt.isPresent() && userOpt.get().getPassword().equals(pass)) {

                this.dispose();
                new ProfileView(userOpt.get()).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Неверный email или пароль!", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Ошибка БД: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Класс диалога входа админа
    private static class AdminLoginDialog extends JDialog {
        public AdminLoginDialog(JFrame parent) {
            super(parent, "Вход администратора", true);
            setSize(350, 200);
            setLocationRelativeTo(parent);
            setLayout(new FlowLayout(FlowLayout.CENTER, 10, 20));
            getContentPane().setBackground(new Color(245, 248, 250));

            JTextField loginField = new JTextField(15);
            JPasswordField passField = new JPasswordField(15);
            loginField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            passField.setFont(new Font("Segoe UI", Font.PLAIN, 13));

            add(new JLabel("Логин:"));
            add(loginField);
            add(new JLabel("Пароль:"));
            add(passField);

            JButton okBtn = new JButton("Войти");
            okBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            okBtn.setBackground(new Color(52, 152, 219));
            okBtn.setForeground(Color.WHITE);
            okBtn.addActionListener(e -> {
                if ("admin".equals(loginField.getText()) && "admin".equals(new String(passField.getPassword()))) {
                    dispose();
                    new AdminView().setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(this, "Неверные данные!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            });
            add(okBtn);
        }
    }
}