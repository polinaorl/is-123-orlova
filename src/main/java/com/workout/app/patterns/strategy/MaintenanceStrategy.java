package com.workout.app.patterns.strategy;

import com.workout.app.model.User;

public class MaintenanceStrategy implements TrainingStrategy {
    @Override public String getStrategyName() { return "Классический (по цели)"; }
    @Override public String getAdvice() { return "Сбалансированная нагрузка. Фокус на технике и восстановлении."; }
    @Override public int getRecommendedRestSeconds() { return 75; }
    @Override public String calculateIntensity(User user) {
        return "Умеренная интенсивность. Поддерживай текущий тонус.";
    }
    @Override public String modifyExerciseReps(String originalReps) {
        return originalReps + " (средний режим, техника в приоритете)";
    }
}