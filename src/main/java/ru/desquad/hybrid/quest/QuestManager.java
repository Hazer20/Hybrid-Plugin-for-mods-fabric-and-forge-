package ru.desquad.hybrid.quest;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
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
    private final Random random = new Random();

    private final Map<UUID, PlayerQuestState> cache = new ConcurrentHashMap<>();

    public QuestManager(DESHybridPlugin plugin, DataStorage storage, EconomyManager economy) {
        this.plugin = plugin;
        this.storage = storage;
        this.economy = economy;
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

    public List<ActiveQuest> getAssignedQuests(UUID uuid) {
        List<ActiveQuest> list = new ArrayList<>();
        for (String raw : getState(uuid).getAssignedQuestIds()) {
            ActiveQuest q = ActiveQuest.deserialize(raw);
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
            state.setAssignedQuestIds(generateDailyQuests(player.getUniqueId()));
            storage.saveQuestState(player.getUniqueId(), state);
        } else if (state.getAssignedQuestIds().isEmpty() || getAssignedQuests(player.getUniqueId()).isEmpty()) {
            state.setAssignedQuestIds(generateDailyQuests(player.getUniqueId()));
            storage.saveQuestState(player.getUniqueId(), state);
        }
    }

    public boolean tryClaimQuest(Player player, String questId) {
        PlayerQuestState state = getState(player.getUniqueId());
        List<ActiveQuest> quests = getAssignedQuests(player.getUniqueId());
        ActiveQuest quest = quests.stream().filter(q -> q.getId().equals(questId)).findFirst().orElse(null);
        if (quest == null || quest.isClaimed() || !quest.isCompleted()) return false;

        int max = plugin.getConfig().getInt("quests.max-per-day", 3);
        if (state.getCompletedToday() >= max) return false;

        quest.setClaimed(true);
        state.setCompletedToday(state.getCompletedToday() + 1);
        economy.add(player.getUniqueId(), quest.getReward());
        persistActive(player.getUniqueId(), quests);

        player.sendMessage("§aКвест подтверждён: §f" + quest.getDescription() + " §7(+" + quest.getReward() + " DESCoin)");
        player.playSound(player.getLocation(), plugin.getConfig().getString("npc-builder.sounds.coin", "ENTITY_EXPERIENCE_ORB_PICKUP"), 1f, 1.2f);
        return true;
    }

    public void onBlockBreak(Player player, Material material) {
        updateProgress(player.getUniqueId(), q -> q.getObjectiveType() == QuestObjectiveType.BREAK_BLOCK
                && q.getObjectiveKey().equals(material.name()));
    }

    public void onMobKill(Player player, EntityType type) {
        updateProgress(player.getUniqueId(), q -> q.getObjectiveType() == QuestObjectiveType.KILL_MOB
                && q.getObjectiveKey().equals(type.name()));
    }

    public void onFishCatch(Player player) {
        updateProgress(player.getUniqueId(), q -> q.getObjectiveType() == QuestObjectiveType.CATCH_FISH);
    }

    public String sidebarLine(UUID player) {
        List<ActiveQuest> quests = getAssignedQuests(player);
        long done = quests.stream().filter(ActiveQuest::isClaimed).count();
        long active = quests.stream().filter(q -> !q.isClaimed()).count();
        return "§eКвесты: §a" + done + "§7/3 §8(" + active + " акт.)";
    }

    private void updateProgress(UUID uuid, java.util.function.Predicate<ActiveQuest> predicate) {
        List<ActiveQuest> quests = getAssignedQuests(uuid);
        boolean changed = false;
        for (ActiveQuest quest : quests) {
            if (quest.isClaimed() || quest.isCompleted()) continue;
            if (predicate.test(quest)) {
                quest.addProgress(1);
                changed = true;
            }
        }
        if (changed) persistActive(uuid, quests);
    }

    private void persistActive(UUID uuid, List<ActiveQuest> quests) {
        PlayerQuestState state = getState(uuid);
        List<String> serialized = new ArrayList<>();
        for (ActiveQuest q : quests) serialized.add(q.serialize());
        state.setAssignedQuestIds(serialized);
        storage.saveQuestState(uuid, state);
    }

    private List<String> generateDailyQuests(UUID uuid) {
        List<ActiveQuest> quests = List.of(randomEasy(), randomMedium(), randomHard());
        List<String> serialized = new ArrayList<>();
        for (ActiveQuest q : quests) serialized.add(q.serialize());
        return serialized;
    }

    private ActiveQuest randomEasy() {
        double reward = plugin.getConfig().getDouble("quests.rewards.easy", 40);
        return switch (random.nextInt(3)) {
            case 0 -> make("easy", QuestDifficulty.EASY, reward, QuestObjectiveType.BREAK_BLOCK, Material.COBBLESTONE.name(), rand(16, 36), "Сломай %d булыжника");
            case 1 -> make("easy", QuestDifficulty.EASY, reward, QuestObjectiveType.BREAK_BLOCK, Material.OAK_LOG.name(), rand(12, 28), "Добудь %d дубовых брёвен");
            default -> make("easy", QuestDifficulty.EASY, reward, QuestObjectiveType.CATCH_FISH, "FISH", rand(4, 10), "Поймай %d рыб");
        };
    }

    private ActiveQuest randomMedium() {
        double reward = plugin.getConfig().getDouble("quests.rewards.medium", 90);
        return switch (random.nextInt(3)) {
            case 0 -> make("medium", QuestDifficulty.MEDIUM, reward, QuestObjectiveType.KILL_MOB, EntityType.ZOMBIE.name(), rand(8, 16), "Убей %d зомби");
            case 1 -> make("medium", QuestDifficulty.MEDIUM, reward, QuestObjectiveType.BREAK_BLOCK, Material.IRON_ORE.name(), rand(10, 20), "Добудь %d железной руды");
            default -> make("medium", QuestDifficulty.MEDIUM, reward, QuestObjectiveType.KILL_MOB, EntityType.SKELETON.name(), rand(7, 14), "Убей %d скелетов");
        };
    }

    private ActiveQuest randomHard() {
        double reward = plugin.getConfig().getDouble("quests.rewards.hard", 180);
        return switch (random.nextInt(3)) {
            case 0 -> make("hard", QuestDifficulty.HARD, reward, QuestObjectiveType.KILL_MOB, EntityType.ENDERMAN.name(), rand(2, 5), "Убей %d эндерменов");
            case 1 -> make("hard", QuestDifficulty.HARD, reward, QuestObjectiveType.BREAK_BLOCK, Material.GOLD_ORE.name(), rand(12, 28), "Добудь %d золотой руды");
            default -> make("hard", QuestDifficulty.HARD, reward, QuestObjectiveType.KILL_MOB, EntityType.BLAZE.name(), rand(4, 8), "Убей %d ифритов");
        };
    }

    private ActiveQuest make(String pref, QuestDifficulty difficulty, double reward, QuestObjectiveType type,
                             String key, int target, String fmt) {
        String id = pref + "_" + UUID.randomUUID().toString().substring(0, 8);
        return new ActiveQuest(id, fmt.formatted(target), difficulty, reward, type, key, target, 0, false);
    }

    private int rand(int min, int max) { return random.nextInt(max - min + 1) + min; }

    private void resetIfNeededOnline() {
        for (Player player : Bukkit.getOnlinePlayers()) ensureQuests(player);
    }

    private long currentEpochDay() {
        return LocalDate.now(ZoneId.systemDefault()).toEpochDay();
    }
}
