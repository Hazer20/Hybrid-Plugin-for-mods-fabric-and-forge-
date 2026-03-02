package ru.desquad.hybrid.quest;

public enum QuestDifficulty {
    EASY("Лёгкая", "easy"),
    MEDIUM("Средняя", "medium"),
    HARD("Сложная", "hard");

    private final String display;
    private final String key;

    QuestDifficulty(String display, String key) {
        this.display = display;
        this.key = key;
    }

    public String getDisplay() {
        return display;
    }

    public String getKey() {
        return key;
    }
}
