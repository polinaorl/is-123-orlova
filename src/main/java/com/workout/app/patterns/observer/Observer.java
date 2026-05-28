package com.workout.app.patterns.observer;

// Интерфейс наблюдателя

public interface Observer {
    void update(String eventType, Object data);
}