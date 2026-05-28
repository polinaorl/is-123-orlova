package com.workout.app.patterns.strategy;

import com.workout.app.model.User;

public interface TrainingStrategy {
    String getStrategyName();
    String getAdvice();
    int getRecommendedRestSeconds();
    String calculateIntensity(User user);

    // Движок меняет рекомендации по повторениям
    String modifyExerciseReps(String originalReps);
}