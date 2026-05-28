package com.workout.app.patterns.strategy;

import com.workout.app.model.User;

// Движок 3: Умная рекомендация (имитация AI)

public class SmartRecommendationStrategy implements TrainingStrategy {
    @Override public String getStrategyName() { return "3. Умная рекомендация (AI)"; }
    @Override public String getAdvice() { return "Анализ данных и подбор оптимальной нагрузки (AI)."; }
    @Override public int getRecommendedRestSeconds() { return 60; }

    @Override
    public String calculateIntensity(User user) {
        int maxHeartRate = 220 - user.getAge();
        int targetHR = (int) (maxHeartRate * 0.85); // Зона высокой интенсивности
        return String.format("AI-расчёт: Целевой пульс %d уд/мин, Интенсивность 85%%", targetHR);
    }

    @Override
    public String modifyExerciseReps(String originalReps) {

        String aiReps = originalReps.replace("8-10", "6-8").replace("10-12", "8-10").replace("12-15", "10-12");
        return aiReps + " [AI-режим: выше интенсивность, меньше повторений]";
    }
}