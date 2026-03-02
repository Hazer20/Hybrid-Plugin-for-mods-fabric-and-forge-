package ru.desquad.hybrid.quest;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.desquad.hybrid.DESHybridPlugin;
import ru.desquad.hybrid.economy.EconomyManager;
import ru.desquad.hybrid.storage.DataStorage;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class QuestManager {

    private final DESHybridPlugin plugin;
    private final DataStorage storage;
    private final EconomyManager economy;

    private final Map<UUID, PlayerQuestState> cache = new ConcurrentHashMap<>();
    private final Map<String, MiniQuest> allQuests = new HashMap<>();

    public QuestManager(DESHybridPlugin plugin, DataStorage storage, EconomyManager economy) {
        this.plugin = plugin;
        this.storage = storage;
        this.economy = economy;
        loadQuestPool();
    }

    public void startDailyResetTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, this::resetIfNeededOnline, 20L * 60, 20L * 60 * 10);
    }

    public void shutdown() {
        for (Map.Entry<UUID, PlayerQuestState> e : cache.entrySet()) {
            storage.saveQuestState(e.getKey(), e.getValue());
        }
    }

    public PlayerQuestState getState(UUID uuid) {
        return cache.computeIfAbsent(uuid, storage::getQuestState);
    }

    public List<MiniQuest> getAssignedQuests(UUID uuid) {
        PlayerQuestState state = getState(uuid);
        List<MiniQuest> list = new ArrayList<>();
        for (String id : state.getAssignedQuestIds()) {
            MiniQuest q = allQuests.get(id);
            if (q != null) list.add(q);
        }
        return list;
    }

    public void ensureQuests(Player player) {
        PlayerQuestState state = getState(player.getUniqueId());
        long today = currentEpochDay();
        if (state.getLastResetEpochDay() != today) {
            state.setLastResetEpochDay(today);
            state.setCompletedToday(0);
            assignDailyQuests(state);
            storage.saveQuestState(player.getUniqueId(), state);
        } else if (state.getAssignedQuestIds().isEmpty()) {
            assignDailyQuests(state);
            storage.saveQuestState(player.getUniqueId(), state);
        }
    }

    public boolean completeQuest(Player player, String questId) {
        PlayerQuestState state = getState(player.getUniqueId());
        if (!state.getAssignedQuestIds().contains(questId)) {
            return false;
        }
        int max = plugin.getConfig().getInt("quests.max-per-day", 3);
        if (state.getCompletedToday() >= max) {
            return false;
        }

        MiniQuest quest = allQuests.get(questId);
        if (quest == null) {
            return false;
        }

        state.getAssignedQuestIds().remove(questId);
        state.setCompletedToday(state.getCompletedToday() + 1);
        economy.add(player.getUniqueId(), quest.getReward());

        storage.saveQuestState(player.getUniqueId(), state);
        player.sendMessage("§aКвест выполнен: §f" + quest.getDescription() + " §7(+" + quest.getReward() + " DESCoin)");
        player.playSound(player.getLocation(), plugin.getConfig().getString("npc-builder.sounds.coin", "ENTITY_EXPERIENCE_ORB_PICKUP"), 1f, 1.2f);
        return true;
    }

    public List<MiniQuest> getAllQuests() {
        return new ArrayList<>(allQuests.values());
    }

    private void loadQuestPool() {
        allQuests.clear();
        addDifficultyPool(QuestDifficulty.EASY);
        addDifficultyPool(QuestDifficulty.MEDIUM);
        addDifficultyPool(QuestDifficulty.HARD);
    }

    private void addDifficultyPool(QuestDifficulty difficulty) {
        List<String> pool = plugin.getConfig().getStringList("quests.pool." + difficulty.getKey());
        double reward = plugin.getConfig().getDouble("quests.rewards." + difficulty.getKey(), 0);
        int index = 1;
        for (String description : pool) {
            String id = difficulty.getKey() + "_" + index;
            allQuests.put(id, new MiniQuest(id, description, difficulty, reward));
            index++;
        }
    }

    private void assignDailyQuests(PlayerQuestState state) {
        List<MiniQuest> easy = byDiff(QuestDifficulty.EASY);
        List<MiniQuest> medium = byDiff(QuestDifficulty.MEDIUM);
        List<MiniQuest> hard = byDiff(QuestDifficulty.HARD);

        state.getAssignedQuestIds().clear();
        state.getAssignedQuestIds().add(random(easy).getId());
        state.getAssignedQuestIds().add(random(medium).getId());
        state.getAssignedQuestIds().add(random(hard).getId());
    }

    private List<MiniQuest> byDiff(QuestDifficulty difficulty) {
        List<MiniQuest> list = new ArrayList<>();
        for (MiniQuest q : allQuests.values()) {
            if (q.getDifficulty() == difficulty) {
                list.add(q);
            }
        }
        return list;
    }

    private MiniQuest random(List<MiniQuest> list) {
        if (list.isEmpty()) {
            return new MiniQuest("fallback", "Сломай 1 блок", QuestDifficulty.EASY, 1);
        }
        return list.get(new Random().nextInt(list.size()));
    }

    private void resetIfNeededOnline() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            ensureQuests(player);
        }
    }

    private long currentEpochDay() {
        return LocalDate.now(ZoneId.systemDefault()).toEpochDay();
    }
}
