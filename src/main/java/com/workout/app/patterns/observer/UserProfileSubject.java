package com.workout.app.patterns.observer;

import java.util.ArrayList;
import java.util.List;

// Уведомляет наблюдателей об изменениях

public class UserProfileSubject implements Subject {
    private List<Observer> observers = new ArrayList<>();
    private String userId;

    public UserProfileSubject(String userId) {
        this.userId = userId;
    }

    @Override
    public void attach(Observer observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
            System.out.println("Наблюдатель подключён: " + observer.getClass().getSimpleName());
        }
    }

    @Override
    public void detach(Observer observer) {
        observers.remove(observer);
        System.out.println(" Наблюдатель отключён: " + observer.getClass().getSimpleName());
    }

    @Override
    public void notifyObservers(String eventType, Object data) {
        System.out.println(" Событие: " + eventType + " | Данные: " + data);
        for (Observer observer : observers) {
            try {
                observer.update(eventType, data);
            } catch (Exception e) {
                System.err.println("⚠Ошибка уведомления: " + e.getMessage());
            }
        }
    }

    //  Методы для вызова событий
    public void onGoalChanged(String newGoal) {
        notifyObservers("GOAL_CHANGED", newGoal);
    }

    public void onLocationChanged(String newLocation) {
        notifyObservers("LOCATION_CHANGED", newLocation);
    }

    public void onProfileUpdated() {
        notifyObservers("PROFILE_UPDATED", userId);
    }

    public void onWorkoutCompleted() {
        notifyObservers("WORKOUT_COMPLETED", userId);
    }
    //Уведомление об изменении веса
    public void onWeightChanged(double newWeight) {
        notifyObservers("WEIGHT_CHANGED", newWeight);
    }

    public String getUserId() {
        return userId;
    }
}