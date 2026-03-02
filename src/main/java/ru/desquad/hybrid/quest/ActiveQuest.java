package ru.desquad.hybrid.quest;

public class ActiveQuest {
    private final String id;
    private final String description;
    private final QuestDifficulty difficulty;
    private final double reward;
    private final QuestObjectiveType objectiveType;
    private final String objectiveKey;
    private final int target;
    private int progress;
    private boolean claimed;

    public ActiveQuest(String id, String description, QuestDifficulty difficulty, double reward,
                       QuestObjectiveType objectiveType, String objectiveKey, int target, int progress, boolean claimed) {
        this.id = id;
        this.description = description;
        this.difficulty = difficulty;
        this.reward = reward;
        this.objectiveType = objectiveType;
        this.objectiveKey = objectiveKey;
        this.target = target;
        this.progress = progress;
        this.claimed = claimed;
    }

    public String serialize() {
        return String.join("|", id, escape(description), difficulty.name(), String.valueOf(reward),
                objectiveType.name(), objectiveKey, String.valueOf(target), String.valueOf(progress), String.valueOf(claimed));
    }

    public static ActiveQuest deserialize(String line) {
        String[] p = line.split("\\|", 9);
        if (p.length < 9) return null;
        return new ActiveQuest(
                p[0], unescape(p[1]), QuestDifficulty.valueOf(p[2]), Double.parseDouble(p[3]),
                QuestObjectiveType.valueOf(p[4]), p[5], Integer.parseInt(p[6]), Integer.parseInt(p[7]), Boolean.parseBoolean(p[8])
        );
    }

    private static String escape(String s) { return s.replace("|", "¦"); }
    private static String unescape(String s) { return s.replace("¦", "|"); }

    public boolean isCompleted() { return progress >= target; }
    public void addProgress(int value) { this.progress = Math.min(target, progress + value); }

    public String getId() { return id; }
    public String getDescription() { return description; }
    public QuestDifficulty getDifficulty() { return difficulty; }
    public double getReward() { return reward; }
    public QuestObjectiveType getObjectiveType() { return objectiveType; }
    public String getObjectiveKey() { return objectiveKey; }
    public int getTarget() { return target; }
    public int getProgress() { return progress; }
    public boolean isClaimed() { return claimed; }
    public void setClaimed(boolean claimed) { this.claimed = claimed; }
}
