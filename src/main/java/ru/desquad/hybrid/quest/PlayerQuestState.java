package ru.desquad.hybrid.quest;

import java.util.ArrayList;
import java.util.List;

public class PlayerQuestState {
    private long lastResetEpochDay;
    private int completedToday;
    private List<String> assignedQuestIds = new ArrayList<>();

    public long getLastResetEpochDay() {
        return lastResetEpochDay;
    }

    public void setLastResetEpochDay(long lastResetEpochDay) {
        this.lastResetEpochDay = lastResetEpochDay;
    }

    public int getCompletedToday() {
        return completedToday;
    }

    public void setCompletedToday(int completedToday) {
        this.completedToday = completedToday;
    }

    public List<String> getAssignedQuestIds() {
        return assignedQuestIds;
    }

    public void setAssignedQuestIds(List<String> assignedQuestIds) {
        this.assignedQuestIds = assignedQuestIds;
    }
}
