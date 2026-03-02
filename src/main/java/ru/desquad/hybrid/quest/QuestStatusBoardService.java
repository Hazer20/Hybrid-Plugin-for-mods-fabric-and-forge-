package ru.desquad.hybrid.quest;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import ru.desquad.hybrid.DESHybridPlugin;

public class QuestStatusBoardService {

    private final DESHybridPlugin plugin;
    private final QuestManager questManager;
    private int taskId = -1;

    public QuestStatusBoardService(DESHybridPlugin plugin, QuestManager questManager) {
        this.plugin = plugin;
        this.questManager = questManager;
    }

    public void start() {
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::tick, 20L, 40L);
    }

    public void stop() {
        if (taskId != -1) Bukkit.getScheduler().cancelTask(taskId);
    }

    private void tick() {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) return;
        for (Player player : Bukkit.getOnlinePlayers()) {
            Scoreboard board = manager.getNewScoreboard();
            Objective obj = board.registerNewObjective("des_status", "dummy", "§6§lDES Статус");
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
            obj.getScore(" ").setScore(5);
            obj.getScore("§eБаланс: §6" + String.format("%.1f", plugin.getEconomyManager().getBalance(player.getUniqueId()))).setScore(4);
            obj.getScore(questManager.sidebarLine(player.getUniqueId())).setScore(3);
            obj.getScore("§7Открыть: §f/desquests").setScore(2);
            obj.getScore("§8de squad").setScore(1);
            player.setScoreboard(board);
        }
    }
}
