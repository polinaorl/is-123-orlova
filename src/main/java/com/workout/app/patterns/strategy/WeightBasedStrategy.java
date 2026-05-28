package com.workout.app.patterns.strategy;

import com.workout.app.model.User;

//Движок 2: Расчёт на основе веса и роста (ИМТ)

public class WeightBasedStrategy implements TrainingStrategy {
    @Override public String getStrategyName() { return "2. По весу/росту (ИМТ)"; }
    @Override public String getAdvice() { return "Математический расчёт нагрузки на основе ИМТ."; }
    @Override public int getRecommendedRestSeconds() { return 90; }

    @Override
    public String calculateIntensity(User user) {
        // имт Вес / (Рост в метрах)^2
        double heightInMeters = user.getHeight() / 100.0;
        double bmi = user.getWeight() / (heightInMeters * heightInMeters);

        // Идеальный вес (формула Кетле)
        double idealWeight = 22 * (heightInMeters * heightInMeters);
        int workingWeight = (int) (idealWeight * 0.65); // Рабочий вес 65% от идеального

        String category = bmi < 18.5 ? "Дефицит массы" :
                bmi < 25 ? "Норма" : "Избыток массы";

        return String.format("ИМТ: %.1f (%s) | Рабочий вес: %d кг", bmi, category, workingWeight);
    }

    @Override
    public String modifyExerciseReps(String originalReps) {
        return originalReps + " [ИМТ-режим: больше повторов, легче вес]";
    }
}