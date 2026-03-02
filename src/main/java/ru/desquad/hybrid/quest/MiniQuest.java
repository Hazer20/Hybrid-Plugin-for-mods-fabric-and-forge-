package ru.desquad.hybrid.quest;

public class MiniQuest {
    private final String id;
    private final String description;
    private final QuestDifficulty difficulty;
    private final double reward;

    public MiniQuest(String id, String description, QuestDifficulty difficulty, double reward) {
        this.id = id;
        this.description = description;
        this.difficulty = difficulty;
        this.reward = reward;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public QuestDifficulty getDifficulty() {
        return difficulty;
    }

    public double getReward() {
        return reward;
    }
}
