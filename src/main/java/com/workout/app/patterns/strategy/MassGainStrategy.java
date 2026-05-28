package com.workout.app.patterns.strategy;

import com.workout.app.model.User;

//Движок 1: Классический (по цели)

public class MassGainStrategy implements TrainingStrategy {
    @Override public String getStrategyName() { return "1. Классический (по цели)"; }
    @Override public String getAdvice() { return "Стандартная программа под твою цель из базы данных."; }
    @Override public int getRecommendedRestSeconds() { return 120; }

    @Override
    public String calculateIntensity(User user) {
        return String.format("Рабочий вес: %.1f кг (70%% от текущего веса)", user.getWeight() * 0.7);
    }

    @Override
    public String modifyExerciseReps(String originalReps) {
        return originalReps + " [КЛАССИКА: стандартный объём]";
    }
}