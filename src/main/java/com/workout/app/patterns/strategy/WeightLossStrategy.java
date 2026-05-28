package com.workout.app.patterns.strategy;

import com.workout.app.model.User;

public class WeightLossStrategy implements TrainingStrategy {
    @Override public String getStrategyName() { return " Классический (по цели)"; }
    @Override public String getAdvice() { return "Высокий темп, короткие паузы. Акцент на жиросжигание."; }
    @Override public int getRecommendedRestSeconds() { return 45; }
    @Override public String calculateIntensity(User user) {
        int hr = (int) ((220 - user.getAge()) * 0.7);
        return String.format("Целевой пульс: %d уд/мин (зона жиросжигания)", hr);
    }
    @Override public String modifyExerciseReps(String originalReps) {
        return originalReps.replace("-", "+5") + " (высокий темп, больше повторов)";
    }
}