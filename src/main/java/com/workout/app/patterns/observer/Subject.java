package com.workout.app.patterns.observer;

// Интерфейс наблюдаемого (издателя)

public interface Subject {
    void attach(Observer observer);
    void detach(Observer observer);
    void notifyObservers(String eventType, Object data);
}