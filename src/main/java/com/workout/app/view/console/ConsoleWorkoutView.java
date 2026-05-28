package com.workout.app.view.console;

import com.workout.app.dao.ProgramExerciseDAO;
import com.workout.app.dao.ProgramExerciseDAO.ProgramExerciseInfo;
import com.workout.app.dao.WorkoutProgramDAO;
import com.workout.app.database.DatabaseManager;
import com.workout.app.model.User;
import com.workout.app.model.WorkoutProgram;
import com.workout.app.patterns.strategy.*;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class ConsoleWorkoutView {
    private User user;
    private TrainingContext ctx;

    public ConsoleWorkoutView(User user) {
        this.user = user;
        this.ctx = new TrainingContext();
        ctx.selectStrategyByIndex(0, user);
    }

    public void show() {
        Scanner sc = new Scanner(System.in);
        loadProgram();

        while (true) {
            System.out.println("\n═══════════════════════════════════════");
            System.out.println("    ВЫБЕРИ ДВИЖОК:");
            System.out.println("═══════════════════════════════════════");
            System.out.println("1. Классический (по цели)");
            System.out.println("2. По весу/росту (ИМТ)");
            System.out.println("3. Умная рекомендация (AI)");
            System.out.println("0. <- Назад");
            System.out.print("\nВыбор: ");

            String ch = sc.nextLine();
            switch (ch) {
                case "1" -> { ctx.selectStrategyByIndex(0, user); loadProgram(); }
                case "2" -> { ctx.selectStrategyByIndex(1, user); loadProgram(); }
                case "3" -> { ctx.selectStrategyByIndex(2, user); loadProgram(); }
                case "0" -> { return; }
                default -> System.out.println(" Неверный выбор!");
            }
        }
    }

    private void loadProgram() {
        try (Connection c = DatabaseManager.getConnection()) {
            WorkoutProgramDAO pd = new WorkoutProgramDAO(c);
            ProgramExerciseDAO ed = new ProgramExerciseDAO(c);
            Optional<WorkoutProgram> o = pd.findByGoalAndLocation(user.getGoal(), user.getLocation());

            if (o.isEmpty()) {
                System.out.println("\n Программа не найдена!");
                System.out.println("Цель: " + user.getGoalText());
                System.out.println("Место: " + user.getLocationText());
                return;
            }

            WorkoutProgram p = o.get();

            System.out.println("\n═══════════════════════════════════════");
            System.out.println("   " + ctx.getCurrentStrategyName());
            System.out.println("═══════════════════════════════════════");
            System.out.println(ctx.getStrategyInfo(user));

            System.out.println("\n═══════════════════════════════════════");
            System.out.println("   " + p.getName());
            System.out.println("═══════════════════════════════════════");
            System.out.println(" Цель: " + p.getGoalText());
            System.out.println(" Место: " + p.getLocationText());
            System.out.println(" " + p.getDescription());

            System.out.println("\n───────────────────────────────────────");
            System.out.println("           УПРАЖНЕНИЯ");
            System.out.println("───────────────────────────────────────\n");

            List<ProgramExerciseInfo> ex = ed.getExercisesForProgram(p.getId());
            int n = 1;
            for (ProgramExerciseInfo i : ex) {
                if (i.getExercise() != null) {
                    System.out.println(n++ + ". " + i.getExercise().getName());
                    System.out.println("    Подходы: " + i.getSets());
                    System.out.println("    Повторения: " + ctx.getStrategy().modifyExerciseReps(i.getReps()));
                    System.out.println("    Группа: " + i.getExercise().getMuscleGroup());
                    System.out.println("    Инвентарь: " + i.getExercise().getEquipment());
                    System.out.println();
                }
            }
        } catch (Exception e) {
            System.out.println(" Ошибка: " + e.getMessage());
        }
    }
}