package com.workout.app.patterns.observer;

import javax.swing.*;

//Наблюдатель: показывает уведомления о достижениях

public class AchievementObserver implements Observer {
    private String userName;

    public AchievementObserver(String userName) {
        this.userName = userName;
    }

    @Override
    public void update(String eventType, Object data) {
        switch (eventType) {
            case "WORKOUT_COMPLETED" -> {
                String message = switch ((int) (Math.random() * 3)) {
                    case 0 -> " Отличная работа, " + userName + "!";
                    case 1 -> " Ты становишься сильнее!";
                    default -> " Продолжай в том же духе!";
                };
                System.out.println(" Achievement: " + message);
                // В реальном приложении: показать красивое уведомление
                // JOptionPane.showMessageDialog(null, message, "Достижение!", JOptionPane.INFORMATION_MESSAGE);
            }
            case "PROFILE_UPDATED" ->
                    System.out.println(" Профиль " + userName + " обновлён");
        }
    }
}