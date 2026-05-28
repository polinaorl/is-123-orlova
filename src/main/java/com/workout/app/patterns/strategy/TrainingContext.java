package com.workout.app.patterns.strategy;

import com.workout.app.model.User;

public class TrainingContext {
    private TrainingStrategy strategy;

    public void setStrategy(TrainingStrategy strategy) {
        this.strategy = strategy;
    }

    public TrainingStrategy getStrategy() {
        return strategy;
    }

    public String getStrategyInfo(User user) {
        if (strategy == null) return "Стратегия не выбрана";
        return " " + strategy.getStrategyName() + "\n" +
                " Совет: " + strategy.getAdvice() + "\n" +
                " Отдых: " + strategy.getRecommendedRestSeconds() + " сек\n" +
                " Расчет: " + strategy.calculateIntensity(user);
    }

    public int getRestTime() {
        return strategy != null ? strategy.getRecommendedRestSeconds() : 60;
    }

    public String getCurrentStrategyName() {
        return strategy != null ? strategy.getStrategyName() : "Не выбрана";
    }

    // 🔥 Теперь переключает только между 3 движками
    public void selectStrategyByIndex(int index, User user) {
        switch (index) {
            case 0 -> setStrategy(new MassGainStrategy());
            case 1 -> setStrategy(new WeightBasedStrategy());
            case 2 -> setStrategy(new SmartRecommendationStrategy());
            default -> setStrategy(new MassGainStrategy());
        }
    }
}