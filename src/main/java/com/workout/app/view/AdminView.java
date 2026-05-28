package com.workout.app.view;

import com.workout.app.dao.ExerciseDAO;
import com.workout.app.dao.UserDAO;
import com.workout.app.database.DatabaseManager;
import com.workout.app.model.Exercise;
import com.workout.app.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

public class AdminView extends JFrame {
    private JTabbedPane tabbedPane;
    private JTable userTable;
    private JTable exerciseTable;
    private DefaultTableModel userModel;
    private DefaultTableModel exerciseModel;

    public AdminView() {
        initUI();
        initComponents();
        loadUsers();
        loadExercises();
    }

    private void initUI() {
        setTitle(" Панель администратора");
        setSize(950, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(new Color(245, 245, 245));
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        mainPanel.setBackground(new Color(245, 245, 245));

        JLabel titleLabel = new JLabel(" ПАНЕЛЬ АДМИНИСТРАТОРА", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(30, 30, 30));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabbedPane.setBackground(new Color(245, 245, 245));

        JPanel usersPanel = createUsersPanel();
        tabbedPane.addTab(" Пользователи", usersPanel);

        JPanel exercisesPanel = createExercisesPanel();
        tabbedPane.addTab(" Упражнения", exercisesPanel);

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        JButton backBtn = new JButton(" На экран входа");
        backBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        backBtn.setPreferredSize(new Dimension(220, 38));
        backBtn.setBackground(new Color(231, 76, 60));
        backBtn.setForeground(Color.WHITE);
        backBtn.setFocusPainted(false);
        backBtn.addActionListener(e -> {
            this.dispose();
            new LoginView().setVisible(true);
        });

        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        southPanel.setBackground(new Color(245, 245, 245));
        southPanel.add(backBtn);
        mainPanel.add(southPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel createUsersPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(245, 245, 245));

        String[] userColumns = {"ID", "Email", "Цель", "Место", "Рост", "Вес", "Возраст"};
        userModel = new DefaultTableModel(userColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        userTable = new JTable(userModel);
        userTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        userTable.setRowHeight(28);
        userTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        userTable.getTableHeader().setBackground(new Color(230, 235, 240));
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.setGridColor(new Color(220, 220, 220));

        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 220)));
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnPanel.setBackground(new Color(245, 245, 245));

        JButton deleteBtn = new JButton("️ Удалить пользователя");
        deleteBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        deleteBtn.setPreferredSize(new Dimension(220, 38));
        deleteBtn.setBackground(new Color(231, 76, 60));
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.setFocusPainted(false);
        deleteBtn.addActionListener(e -> deleteUser());

        JButton refreshBtn = new JButton(" Обновить");
        refreshBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        refreshBtn.setPreferredSize(new Dimension(160, 38));
        refreshBtn.setBackground(new Color(52, 152, 219));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFocusPainted(false);
        refreshBtn.addActionListener(e -> loadUsers());

        btnPanel.add(deleteBtn);
        btnPanel.add(refreshBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createExercisesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(245, 245, 245));

        String[] exColumns = {"ID", "Название", "Группа мышц", "Инвентарь", "Сложность"};
        exerciseModel = new DefaultTableModel(exColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        exerciseTable = new JTable(exerciseModel);
        exerciseTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        exerciseTable.setRowHeight(28);
        exerciseTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        exerciseTable.getTableHeader().setBackground(new Color(230, 235, 240));
        exerciseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        exerciseTable.setGridColor(new Color(220, 220, 220));

        JScrollPane scrollPane = new JScrollPane(exerciseTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 220)));
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnPanel.setBackground(new Color(245, 245, 245));

        //  КНОПКА ДОБАВИТЬ УПРАЖНЕНИЕ
        JButton addBtn = new JButton("Добавить упражнение");
        addBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        addBtn.setPreferredSize(new Dimension(220, 38));
        addBtn.setBackground(new Color(46, 204, 113));
        addBtn.setForeground(Color.WHITE);
        addBtn.setFocusPainted(false);
        addBtn.addActionListener(e -> showAddExerciseDialog());

        JButton deleteBtn = new JButton(" Удалить упражнение");
        deleteBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        deleteBtn.setPreferredSize(new Dimension(220, 38));
        deleteBtn.setBackground(new Color(231, 76, 60));
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.setFocusPainted(false);
        deleteBtn.addActionListener(e -> deleteExercise());

        JButton refreshBtn = new JButton(" Обновить");
        refreshBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        refreshBtn.setPreferredSize(new Dimension(160, 38));
        refreshBtn.setBackground(new Color(52, 152, 219));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFocusPainted(false);
        refreshBtn.addActionListener(e -> loadExercises());

        btnPanel.add(addBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(refreshBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    //  ДИАЛОГ ДОБАВЛЕНИЯ УПРАЖНЕНИЯ
    private void showAddExerciseDialog() {
        JDialog dialog = new JDialog(this, " Добавить упражнение", true);
        dialog.setSize(450, 380);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridLayout(6, 2, 10, 15));
        dialog.getContentPane().setBackground(new Color(245, 248, 250));

        JTextField nameField = new JTextField();
        JTextField descField = new JTextField();
        JTextField muscleField = new JTextField();
        JTextField equipmentField = new JTextField();
        JComboBox<String> difficultyCombo = new JComboBox<>(new String[]{"Лёгкий", "Средний", "Сложный"});

        styleField(nameField);
        styleField(descField);
        styleField(muscleField);
        styleField(equipmentField);
        difficultyCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        dialog.add(createLabel("Название упражнения:"));
        dialog.add(nameField);
        dialog.add(createLabel("Описание:"));
        dialog.add(descField);
        dialog.add(createLabel("Группа мышц:"));
        dialog.add(muscleField);
        dialog.add(createLabel("Инвентарь:"));
        dialog.add(equipmentField);
        dialog.add(createLabel("Сложность:"));
        dialog.add(difficultyCombo);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnPanel.setBackground(new Color(245, 248, 250));

        JButton saveBtn = new JButton("💾 Сохранить");
        saveBtn.setBackground(new Color(46, 204, 113));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        saveBtn.addActionListener(e -> {
            saveExercise(nameField.getText(), descField.getText(),
                    muscleField.getText(), equipmentField.getText(),
                    (String) difficultyCombo.getSelectedItem());
            dialog.dispose();
            loadExercises();
        });

        JButton cancelBtn = new JButton("Отмена");
        cancelBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cancelBtn.addActionListener(e -> dialog.dispose());

        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);
        dialog.add(btnPanel);

        dialog.setVisible(true);
    }

    private void saveExercise(String name, String desc, String muscle, String equip, String diff) {
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Введите название упражнения!", "Ошибка", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "INSERT INTO exercises (name, description, muscle_group, equipment, difficulty) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setString(2, desc);
            stmt.setString(3, muscle);
            stmt.setString(4, equip);
            stmt.setString(5, diff);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, " Упражнение добавлено!", "Успех", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Ошибка: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(new Color(60, 60, 60));
        return lbl;
    }

    private void styleField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setPreferredSize(new Dimension(200, 32));
    }

    private void loadUsers() {
        userModel.setRowCount(0);
        try (Connection conn = DatabaseManager.getConnection()) {
            UserDAO dao = new UserDAO(conn);
            List<User> users = dao.findAll();
            for (User u : users) {
                userModel.addRow(new Object[]{
                        u.getId(),
                        u.getEmail(),
                        u.getGoalText(),
                        u.getLocationText(),
                        (int) u.getHeight(),
                        (int) u.getWeight(),
                        u.getAge()
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Ошибка загрузки: " + e.getMessage());
        }
    }

    private void loadExercises() {
        exerciseModel.setRowCount(0);
        try (Connection conn = DatabaseManager.getConnection()) {
            ExerciseDAO dao = new ExerciseDAO(conn);
            List<Exercise> exercises = dao.findAll();
            for (Exercise e : exercises) {
                exerciseModel.addRow(new Object[]{
                        e.getId(),
                        e.getName(),
                        e.getMuscleGroup(),
                        e.getEquipment(),
                        e.getDifficulty()
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Ошибка загрузки: " + e.getMessage());
        }
    }

    private void deleteUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Выберите пользователя для удаления!");
            return;
        }

        int id = (int) userModel.getValueAt(selectedRow, 0);
        String email = (String) userModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Удалить пользователя " + email + "?\n\n Это действие нельзя отменить!",
                "Подтверждение удаления",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseManager.getConnection()) {
                UserDAO dao = new UserDAO(conn);
                dao.deleteUser(id);
                JOptionPane.showMessageDialog(this, " Пользователь удалён!");
                loadUsers();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, " Ошибка: " + e.getMessage());
            }
        }
    }

    private void deleteExercise() {
        int selectedRow = exerciseTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Выберите упражнение для удаления!");
            return;
        }

        int id = (int) exerciseModel.getValueAt(selectedRow, 0);
        String name = (String) exerciseModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Удалить упражнение \"" + name + "\"?",
                "Подтверждение удаления",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseManager.getConnection()) {
                ExerciseDAO dao = new ExerciseDAO(conn);
                dao.deleteExercise(id);
                JOptionPane.showMessageDialog(this, " Упражнение удалено!");
                loadExercises();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, " Ошибка: " + e.getMessage());
            }
        }
    }
}