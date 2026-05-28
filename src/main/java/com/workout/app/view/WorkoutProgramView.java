package com.workout.app.view;

import com.workout.app.dao.ProgramExerciseDAO;
import com.workout.app.dao.ProgramExerciseDAO.ProgramExerciseInfo;
import com.workout.app.dao.WorkoutProgramDAO;
import com.workout.app.database.DatabaseManager;
import com.workout.app.model.User;
import com.workout.app.model.WorkoutProgram;
import com.workout.app.patterns.strategy.TrainingContext;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.util.List;

public class WorkoutProgramView extends JFrame {
    private User currentUser;
    private JPanel mainContentPanel;
    private TrainingContext trainingContext;

    public WorkoutProgramView(User user) {
        this.currentUser = user;
        this.trainingContext = new TrainingContext();
        trainingContext.selectStrategyByIndex(0, user);

        initUI();
        initComponents();
        loadAllWorkouts();
    }

    private void initUI() {
        setTitle("Программа тренировок");
        setSize(800, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(new Color(245, 248, 250));
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(245, 248, 250));

        JLabel titleLabel = new JLabel("ВАША ПРОГРАММА", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(44, 62, 80));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JMenuBar menuBar = new JMenuBar();
        JMenu engineMenu = new JMenu("Движок расчёта");
        engineMenu.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        String[] engines = {"1. Классический", "2. По ИМТ", "3. Умный (AI)"};
        for (int i = 0; i < engines.length; i++) {
            final int idx = i;
            JMenuItem item = new JMenuItem(engines[i]);
            item.addActionListener(e -> {
                trainingContext.selectStrategyByIndex(idx, currentUser);
                loadAllWorkouts();
            });
            engineMenu.add(item);
        }
        menuBar.add(engineMenu);
        setJMenuBar(menuBar);

        mainContentPanel = new JPanel();
        mainContentPanel.setLayout(new BoxLayout(mainContentPanel, BoxLayout.Y_AXIS));
        mainContentPanel.setBackground(new Color(245, 248, 250));

        JScrollPane scrollPane = new JScrollPane(
                mainContentPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(new Color(245, 248, 250));

        JButton editBtn = new JButton("Редактировать");
        styleButton(editBtn, new Color(52, 152, 219));
        editBtn.addActionListener(e -> openEditView());

        JButton backBtn = new JButton("В профиль");
        styleButton(backBtn, new Color(149, 165, 166));
        backBtn.addActionListener(e -> {
            this.dispose();
            new ProfileView(currentUser).setVisible(true);
        });

        buttonPanel.add(editBtn);
        buttonPanel.add(backBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void styleButton(JButton btn, Color color) {
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setPreferredSize(new Dimension(150, 35));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
    }

    public void loadAllWorkouts() {
        mainContentPanel.removeAll();

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
        } catch (Exception e) { e.printStackTrace(); }
        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }

    private void addWorkoutSection(WorkoutProgram program, ProgramExerciseDAO dao) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 210, 220), 1, true),
                BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        card.setMaximumSize(new Dimension(760, Short.MAX_VALUE));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel(program.getName());
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(new Color(44, 62, 80));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(title);

        card.add(Box.createVerticalStrut(5));

        JPanel panelInfo = new JPanel(new GridLayout(1, 2, 10, 0));
        panelInfo.setBackground(null);
        panelInfo.setMaximumSize(new Dimension(700, 25));
        panelInfo.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelInfo.add(new JLabel("Цель: " + program.getGoalText()));
        panelInfo.add(new JLabel("Место: " + program.getLocationText()));
        card.add(panelInfo);

        card.add(Box.createVerticalStrut(8));

        try {
            List<ProgramExerciseInfo> exercises = dao.getExercisesForProgram(program.getId());
            int num = 1;

            for (ProgramExerciseInfo exInfo : exercises) {
                if (exInfo.getExercise() != null) {
                    JPanel exRow = new JPanel(new BorderLayout(5, 2));
                    exRow.setBackground(null);
                    exRow.setMaximumSize(new Dimension(720, 35));
                    exRow.setAlignmentX(Component.LEFT_ALIGNMENT);
                    exRow.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)),
                            BorderFactory.createEmptyBorder(4, 0, 4, 0)
                    ));

                    JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
                    left.setBackground(null);
                    JLabel name = new JLabel(num++ + ". " + exInfo.getExercise().getName());
                    name.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    name.setForeground(new Color(44, 62, 80));
                    left.add(name);

                    JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
                    right.setBackground(null);

                    int weight = calculateWeight(exInfo.getExercise().getName());
                    String reps = trainingContext.getStrategy().modifyExerciseReps(exInfo.getReps());

                    JLabel details = new JLabel(exInfo.getSets() + " x " + reps + " | " + weight + " кг | " + exInfo.getExercise().getMuscleGroup());
                    details.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    details.setForeground(new Color(80, 80, 80));
                    right.add(details);

                    exRow.add(left, BorderLayout.WEST);
                    exRow.add(right, BorderLayout.EAST);
                    card.add(exRow);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }

        mainContentPanel.add(card);
        mainContentPanel.add(Box.createVerticalStrut(15));
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

    private void openEditView() {
        try (Connection conn = DatabaseManager.getConnection()) {
            WorkoutProgramDAO programDAO = new WorkoutProgramDAO(conn);
            List<WorkoutProgram> allPrograms = programDAO.findAll();
            java.util.List<WorkoutProgram> userPrograms = new java.util.ArrayList<>();

            for (WorkoutProgram p : allPrograms) {
                if (p.getGoal().equals(currentUser.getGoal()) &&
                        p.getLocation().equals(currentUser.getLocation())) {
                    userPrograms.add(p);
                }
            }

            if (userPrograms.isEmpty()) return;

            String[] names = userPrograms.stream().map(WorkoutProgram::getName).toArray(String[]::new);
            String selected = (String) JOptionPane.showInputDialog(this, "Выберите:", "Редактирование",
                    JOptionPane.QUESTION_MESSAGE, null, names, names[0]);

            if (selected != null) {
                WorkoutProgram p = userPrograms.stream().filter(pr -> pr.getName().equals(selected)).findFirst().orElse(null);
                if (p != null) {
                    // 🔥 Закрываем текущее окно программы
                    this.dispose();
                    // 🔥 Открываем редактор
                    new EditWorkoutView(currentUser, p.getId(), null).setVisible(true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}