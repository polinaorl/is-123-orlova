package com.workout.app.patterns.observer;

import com.workout.app.model.User;

// Наблюдатель: автоматически обновляет программу тренировок

public class TrainingProgramObserver implements Observer {
    private User user;
    private Runnable onProgramUpdate;

    public TrainingProgramObserver(User user, Runnable onProgramUpdate) {
        this.user = user;
        this.onProgramUpdate = onProgramUpdate;
    }

    @Override
    public void update(String eventType, Object data) {
        switch (eventType) {
            case "GOAL_CHANGED", "LOCATION_CHANGED" -> {
                System.out.println(" Пересчёт программы для пользователя: " + user.getEmail());
                // Обновляем данные пользователя
                if ("GOAL_CHANGED".equals(eventType)) {
                    user.setGoal((String) data);
                } else if ("LOCATION_CHANGED".equals(eventType)) {
                    user.setLocation((String) data);
                }
                // Запускаем пересчёт
                if (onProgramUpdate != null) {
                    onProgramUpdate.run();
                }
            }
        }
    }
}