package com.workout.app.patterns.observer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Наблюдатель: логирует события в консоль
public class ProgressLoggerObserver implements Observer {
    private static final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    @Override
    public void update(String eventType, Object data) {
        String timestamp = LocalDateTime.now().format(formatter);

        switch (eventType) {
            case "WEIGHT_CHANGED" -> {
                System.out.println(" [" + timestamp + "] Вес изменён: " + data + " кг");
            }
            case "GOAL_CHANGED" -> {
                System.out.println(" [" + timestamp + "] Цель изменена: " + data);
            }
            case "LOCATION_CHANGED" -> {
                System.out.println(" [" + timestamp + "] Место изменено: " + data);
            }
            case "PROFILE_UPDATED" -> {
                System.out.println(" [" + timestamp + "] Профиль обновлён: " + data);
            }
            case "WORKOUT_COMPLETED" -> {
                System.out.println(" [" + timestamp + "] Тренировка завершена: " + data);
            }
            default -> {
                System.out.println("[" + timestamp + "] " + eventType + " → " + data);
            }
        }
    }
}