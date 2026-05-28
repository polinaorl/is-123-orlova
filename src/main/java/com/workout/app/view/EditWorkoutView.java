package com.workout.app.view;

import com.workout.app.dao.ExerciseDAO;
import com.workout.app.dao.ProgramExerciseDAO;
import com.workout.app.database.DatabaseManager;
import com.workout.app.model.Exercise;
import com.workout.app.model.User;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.util.List;

public class EditWorkoutView extends JFrame {
    private User currentUser;
    private int programId;
    private JComboBox<String> exerciseCombo;
    private JTextField setsField, repsField;
    private JList<String> currentExercisesList;
    private DefaultListModel<String> listModel;
    private java.util.List<Integer> exerciseIds;

    public EditWorkoutView(User user, int programId, WorkoutProgramView parent) {
        this.currentUser = user;
        this.programId = programId;
        this.exerciseIds = new java.util.ArrayList<>();

        initUI();
        initComponents();
        loadExercises();
    }

    private void initUI() {
        setTitle("Редактировать тренировку");
        setSize(850, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(new Color(245, 248, 250));
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        mainPanel.setBackground(new Color(245, 248, 250));

        JLabel titleLabel = new JLabel("Редактирование программы тренировок", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(44, 62, 80));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel centerArea = new JPanel(new GridLayout(1, 2, 20, 0));
        centerArea.setBackground(new Color(245, 248, 250));

        // Левая панель
        JPanel leftPanel = createStyledPanel("Текущие упражнения");
        listModel = new DefaultListModel<>();
        currentExercisesList = new JList<>(listModel);
        currentExercisesList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        currentExercisesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        currentExercisesList.setBackground(Color.WHITE);
        currentExercisesList.setFixedCellHeight(35);

        JScrollPane scrollPane = new JScrollPane(currentExercisesList);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 220)));
        leftPanel.add(scrollPane, BorderLayout.CENTER);
        centerArea.add(leftPanel);

        // Правая панель
        JPanel rightPanel = createStyledPanel("Новое упражнение");
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.add(Box.createVerticalStrut(10));

        rightPanel.setMinimumSize(new Dimension(350, 400));

        JPanel selectRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        selectRow.setBackground(new Color(245, 248, 250));
        selectRow.add(new JLabel("Выберите упражнение:"));
        exerciseCombo = new JComboBox<>();
        exerciseCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        exerciseCombo.setPreferredSize(new Dimension(300, 30));
        selectRow.add(exerciseCombo);
        rightPanel.add(selectRow);

        JPanel setsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        setsRow.setBackground(new Color(245, 248, 250));
        setsRow.add(new JLabel("Количество подходов:"));
        setsField = new JTextField("3");
        setsField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        setsField.setPreferredSize(new Dimension(100, 30));
        setsRow.add(setsField);
        rightPanel.add(setsRow);

        JPanel repsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        repsRow.setBackground(new Color(245, 248, 250));
        repsRow.add(new JLabel("Количество повторений:"));
        repsField = new JTextField("10-12");
        repsField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        repsField.setPreferredSize(new Dimension(100, 30));
        repsRow.add(repsField);
        rightPanel.add(repsRow);

        rightPanel.add(Box.createVerticalStrut(20));

        JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        actionBtns.setBackground(new Color(245, 248, 250));
        actionBtns.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton addBtn = new JButton("Добавить в программу");
        styleButton(addBtn, new Color(46, 204, 113));
        addBtn.setPreferredSize(new Dimension(180, 35));
        addBtn.addActionListener(e -> addExercise());

        JButton removeBtn = new JButton("Удалить выбранное");
        styleButton(removeBtn, new Color(231, 76, 60));
        removeBtn.setPreferredSize(new Dimension(180, 35));
        removeBtn.addActionListener(e -> removeExercise());

        actionBtns.add(addBtn);
        actionBtns.add(removeBtn);
        rightPanel.add(actionBtns);

        centerArea.add(rightPanel);
        mainPanel.add(centerArea, BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        southPanel.setBackground(new Color(245, 248, 250));

        JButton doneBtn = new JButton("Я закончил, сохранить");
        styleButton(doneBtn, new Color(52, 152, 219));
        doneBtn.setPreferredSize(new Dimension(250, 40));
        doneBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));


        doneBtn.addActionListener(e -> {
            new WorkoutProgramView(currentUser).setVisible(true);
            this.dispose();
        });

        southPanel.add(doneBtn);
        mainPanel.add(southPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel createStyledPanel(String title) {

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(new Color(50, 50, 50));

        headerPanel.add(lbl);

        // Основная панель с контентом
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 210, 220), 1, true),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        panel.add(headerPanel, BorderLayout.NORTH);
        return panel;
    }

    private void styleButton(JButton btn, Color color) {
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void loadExercises() {
        try (Connection conn = DatabaseManager.getConnection()) {
            ExerciseDAO exerciseDAO = new ExerciseDAO(conn);
            ProgramExerciseDAO programDAO = new ProgramExerciseDAO(conn);

            List<Exercise> allExercises = exerciseDAO.findAll();
            exerciseCombo.removeAllItems();
            for (Exercise ex : allExercises) {
                //  Только название, без ID
                exerciseCombo.addItem(ex.getName());
            }

            var programExercises = programDAO.getExercisesForProgram(programId);
            listModel.clear();
            exerciseIds.clear();

            for (var item : programExercises) {
                if (item.getExercise() != null) {
                    String text = item.getExercise().getName() + "  —  " +
                            item.getSets() + " подх. × " + item.getReps();
                    listModel.addElement(text);
                    exerciseIds.add(item.getExercise().getId());
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Ошибка загрузки: " + e.getMessage());
        }
    }

    private void addExercise() {
        String selected = (String) exerciseCombo.getSelectedItem();
        if (selected == null) return;

        try {
            int exerciseId = -1;
            try (Connection conn = DatabaseManager.getConnection()) {
                ExerciseDAO dao = new ExerciseDAO(conn);
                List<Exercise> all = dao.findAll();
                for (Exercise ex : all) {
                    if (ex.getName().equals(selected)) {
                        exerciseId = ex.getId();
                        break;
                    }
                }
            }

            if (exerciseId == -1) {
                JOptionPane.showMessageDialog(this, "Упражнение не найдено!");
                return;
            }

            int sets = Integer.parseInt(setsField.getText().trim());
            String reps = repsField.getText().trim();

            try (Connection conn = DatabaseManager.getConnection()) {
                ProgramExerciseDAO dao = new ProgramExerciseDAO(conn);
                int order = listModel.getSize() + 1;
                dao.addExerciseToProgram(programId, exerciseId, sets, reps, order);

                loadExercises();
                JOptionPane.showMessageDialog(this, "Упражнение успешно добавлено!", "Успех", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Ошибка: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removeExercise() {
        int selectedIndex = currentExercisesList.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(this, "Выберите упражнение в списке слева!", "Ошибка", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Удалить это упражнение?", "Подтверждение", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            int exerciseId = exerciseIds.get(selectedIndex);
            try (Connection conn = DatabaseManager.getConnection()) {
                ProgramExerciseDAO dao = new ProgramExerciseDAO(conn);
                dao.removeExerciseFromProgram(programId, exerciseId);
                loadExercises();
                JOptionPane.showMessageDialog(this, "Упражнение удалено!", "Успех", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Ошибка: " + e.getMessage());
            }
        }
    }
}