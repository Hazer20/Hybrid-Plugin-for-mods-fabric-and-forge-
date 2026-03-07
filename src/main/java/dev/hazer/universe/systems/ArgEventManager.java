package dev.hazer.universe.systems;

import dev.hazer.universe.FracturedUniverse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class ArgEventManager {
    private final FracturedUniverse plugin;
    private final Map<Integer, String> events = new LinkedHashMap<>();
    private Integer runningEventId;

    public ArgEventManager(FracturedUniverse plugin) {
        this.plugin = plugin;
        registerDefaults();
    }

    public Map<Integer, String> getEvents() {
        return events;
    }

    public Integer getRunningEventId() {
        return runningEventId;
    }

    public void startEvent(int id) {
        String name = events.get(id);
        if (name == null) {
            return;
        }
        runningEventId = id;
        Bukkit.broadcastMessage(ChatColor.DARK_PURPLE + "[ARG] " + ChatColor.LIGHT_PURPLE + "Событие #" + id + ": " + name);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), Sound.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM, 0.7f, 0.4f);
            player.sendActionBar(ChatColor.DARK_GRAY + "A.R.C.H.I.V.E // сигнал принят");
        }
    }

    public void stopEvent() {
        if (runningEventId != null) {
            Bukkit.broadcastMessage(ChatColor.GRAY + "[ARG] Активное событие остановлено.");
        }
        runningEventId = null;
    }

    public void startRandom() {
        int id = ThreadLocalRandom.current().nextInt(1, events.size() + 1);
        startEvent(id);
    }

    private void registerDefaults() {
        events.put(1, "Шёпот из пустоты");
        events.put(2, "Наблюдатель на горизонте");
        events.put(3, "Трещина в небе");
        events.put(4, "Сломанное солнце");
        events.put(5, "Временная петля");
        events.put(6, "Гравитационная аномалия");
        events.put(7, "Портал в никуда");
        events.put(8, "Исчезновение мобов");
        events.put(9, "Двойная тень игрока");
        events.put(10, "Лаборатория ARCHIVE");
        events.put(11, "Аварийное сообщение системы");
        events.put(12, "Метеоритный дождь");
        events.put(13, "Сбой чанков");
        events.put(14, "Появление случайных порталов");
        events.put(15, "Животные смотрят в небо");
        events.put(16, "Черный дождь");
        events.put(17, "Двойник игрока");
        events.put(18, "Голос системы");
        events.put(19, "Портал в небе");
        events.put(20, "Глючащие блоки");
        events.put(21, "Искажённая музыка");
        events.put(22, "Исчезновение луны");
        events.put(23, "Сломанные координаты");
        events.put(24, "Заморозка времени");
        events.put(25, "Разлом в шахтах");
        events.put(26, "Невидимые шаги рядом");
        events.put(27, "Мигающий свет факелов");
        events.put(28, "Разлом в океане");
        events.put(29, "Ошибка системы");
        events.put(30, "Предвестник Архитектора");
    }
}
