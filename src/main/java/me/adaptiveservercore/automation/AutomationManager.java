package me.adaptiveservercore.automation;

import me.adaptiveservercore.AdaptiveServerCore;
import me.adaptiveservercore.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class AutomationManager {

    private final AdaptiveServerCore plugin;
    private final BackupManager backupManager;
    private final List<BukkitTask> tasks = new ArrayList<>();

    public AutomationManager(AdaptiveServerCore plugin) {
        this.plugin = plugin;
        this.backupManager = new BackupManager(plugin);
    }

    public void start() {
        scheduleIntervalRestart();
        scheduleDailyWeekdayShutdown();
        logStartupWindowInfo();
    }

    public void shutdown() {
        tasks.forEach(BukkitTask::cancel);
        tasks.clear();
    }

    private void scheduleIntervalRestart() {
        int hours = Math.max(1, plugin.getConfig().getInt("рестарт.интервал-часов", 5));
        long intervalTicks = hours * 60L * 60L * 20L;

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, this::announceAndRestart, intervalTicks, intervalTicks);
        tasks.add(task);
    }

    private void announceAndRestart() {
        announceLater("§6Сервер будет перезапущен через 10 минут", 0L);
        announceLater("§6Сервер будет перезапущен через 5 минут", 5L * 60L * 20L);
        announceLater("§6Сервер будет перезапущен через 1 минуту", 9L * 60L * 20L);
        announceLater("§cСервер перезапускается через 10 секунд", 9L * 60L * 20L + 50L);

        BukkitTask restartTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            backupManager.createBackupSync("restart");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "restart");
        }, 10L * 60L * 20L);
        tasks.add(restartTask);
    }

    private void scheduleDailyWeekdayShutdown() {
        ZoneId zoneId = ZoneId.of(plugin.getConfig().getString("timezone", "Europe/Moscow"));
        LocalTime shutdownTime = TimeUtil.parseHm(plugin.getConfig().getString("выключение.время", "00:30"), LocalTime.of(0, 30));
        boolean weekdaysOnly = plugin.getConfig().getBoolean("выключение.только-будни", true);

        long delayTicks = TimeUtil.secondsUntilNext(shutdownTime, zoneId) * 20L;
        BukkitTask dailyTask = Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                DayOfWeek day = java.time.ZonedDateTime.now(zoneId).getDayOfWeek();
                boolean weekday = day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;

                if (!weekdaysOnly || weekday) {
                    Bukkit.broadcastMessage("§cСервер выключается по расписанию.");
                    backupManager.createBackupSync("shutdown");
                    Bukkit.shutdown();
                }

                // Перепланируем задачу на следующий день.
                scheduleDailyWeekdayShutdown();
            }
        }, delayTicks);
        tasks.add(dailyTask);
    }

    private void announceLater(String message, long delayTicks) {
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> Bukkit.broadcastMessage(message), delayTicks);
        tasks.add(task);
    }

    private void logStartupWindowInfo() {
        ZoneId zoneId = ZoneId.of(plugin.getConfig().getString("timezone", "Europe/Moscow"));
        LocalTime startTime = TimeUtil.parseHm(plugin.getConfig().getString("запуск.время", "08:00"), LocalTime.of(8, 0));
        LocalTime now = LocalTime.now(zoneId);
        if (now.isBefore(startTime)) {
            plugin.getLogger().info("Сервер запущен раньше рекомендованного времени запуска: " + startTime);
        }
    }
}
