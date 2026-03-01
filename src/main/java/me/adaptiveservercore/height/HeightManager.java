package me.adaptiveservercore.height;

import me.adaptiveservercore.AdaptiveServerCore;
import me.adaptiveservercore.api.HeightService;
import me.adaptiveservercore.event.PlayerHeightChangeEvent;
import me.adaptiveservercore.storage.PlayerDataStore;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class HeightManager implements HeightService {

    public static final double DEFAULT_HEIGHT = 1.8D;

    private final AdaptiveServerCore plugin;
    private final PlayerDataStore playerDataStore;

    private final Map<UUID, Double> heights = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> animationTasks = new ConcurrentHashMap<>();
    private final Map<String, Double> registeredTypes = new ConcurrentHashMap<>();

    private final double minHeight;
    private final double maxHeight;
    private final boolean animationEnabled;
    private final int animationDurationTicks;

    private final Attribute entityInteractionRangeAttribute;
    private final Attribute blockInteractionRangeAttribute;

    public HeightManager(AdaptiveServerCore plugin, PlayerDataStore playerDataStore) {
        this.plugin = plugin;
        this.playerDataStore = playerDataStore;

        this.minHeight = plugin.getConfig().getDouble("ограничения-роста.минимум", 0.01D);
        this.maxHeight = plugin.getConfig().getDouble("ограничения-роста.максимум", 5.0D);
        this.animationEnabled = plugin.getConfig().getBoolean("анимация-роста.включена", true);
        this.animationDurationTicks = Math.max(1, plugin.getConfig().getInt("анимация-роста.длительность-тиков", 20));

        this.entityInteractionRangeAttribute = resolveAttribute("ENTITY_INTERACTION_RANGE", "PLAYER_ENTITY_INTERACTION_RANGE");
        this.blockInteractionRangeAttribute = resolveAttribute("BLOCK_INTERACTION_RANGE", "PLAYER_BLOCK_INTERACTION_RANGE");

        this.heights.putAll(playerDataStore.readHeights());

        registerHeightType("маленький", 0.5D);
        registerHeightType("средний", 1.0D);
        registerHeightType("обычный", 1.8D);
        registerHeightType("большой", 2.5D);
    }

    public void initializeOnlinePlayers() {
        Bukkit.getOnlinePlayers().forEach(player -> applyHeightInstant(player, getHeight(player)));
    }

    @Override
    public boolean setHeight(Player player, double targetHeight) {
        double clamped = clamp(targetHeight);
        double oldHeight = getHeight(player);

        PlayerHeightChangeEvent event = new PlayerHeightChangeEvent(player, oldHeight, clamped);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }

        clamped = clamp(event.getNewHeight());
        heights.put(player.getUniqueId(), clamped);

        if (animationEnabled && player.isOnline()) {
            animateHeight(player, oldHeight, clamped);
        } else {
            applyHeightInstant(player, clamped);
        }
        persistAsync();
        return true;
    }

    @Override
    public double getHeight(Player player) {
        return heights.getOrDefault(player.getUniqueId(), DEFAULT_HEIGHT);
    }

    @Override
    public boolean resetHeight(Player player) {
        return setHeight(player, DEFAULT_HEIGHT);
    }

    @Override
    public void registerHeightType(String name, double value) {
        registeredTypes.put(name.toLowerCase(Locale.ROOT), clamp(value));
    }

    @Override
    public boolean isHeightAnimating(Player player) {
        return animationTasks.containsKey(player.getUniqueId());
    }

    public Double resolveNamedHeight(String key) {
        return registeredTypes.get(key.toLowerCase(Locale.ROOT));
    }

    public double getMinHeight() {
        return minHeight;
    }

    public double getMaxHeight() {
        return maxHeight;
    }

    public double getAttackReach(Player player) {
        AttributeInstance attribute = getAttributeInstance(player, entityInteractionRangeAttribute);
        return attribute != null ? attribute.getBaseValue() : 3.0D;
    }

    public void onJoin(Player player) {
        applyHeightInstant(player, getHeight(player));
    }

    public void onQuit(Player player) {
        BukkitTask task = animationTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
        persistAsync();
    }

    public int getTowerDepth(Player player) {
        int depth = 1;
        Player current = player;
        while (!current.getPassengers().isEmpty() && current.getPassengers().getFirst() instanceof Player passenger) {
            depth++;
            current = passenger;
        }
        return depth;
    }

    private void animateHeight(Player player, double from, double to) {
        UUID uuid = player.getUniqueId();
        BukkitTask oldTask = animationTasks.remove(uuid);
        if (oldTask != null) {
            oldTask.cancel();
        }

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            int tick = 0;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    BukkitTask running = animationTasks.remove(uuid);
                    if (running != null) {
                        running.cancel();
                    }
                    return;
                }
                tick++;
                double progress = Math.min(1.0D, tick / (double) animationDurationTicks);
                double value = from + ((to - from) * progress);
                applyHeightInstant(player, value);
                if (progress >= 1.0D) {
                    BukkitTask running = animationTasks.remove(uuid);
                    if (running != null) {
                        running.cancel();
                    }
                }
            }
        }, 0L, 1L);

        animationTasks.put(uuid, task);
    }

    private void applyHeightInstant(Player player, double height) {
        double scale = Math.max(0.05D, height / DEFAULT_HEIGHT);
        AttributeInstance scaleAttribute = player.getAttribute(Attribute.SCALE);
        if (scaleAttribute != null) {
            scaleAttribute.setBaseValue(scale);
        }

        // Изменяем дальность атаки в зависимости от масштаба игрока.
        AttributeInstance attackRange = getAttributeInstance(player, entityInteractionRangeAttribute);
        if (attackRange != null) {
            attackRange.setBaseValue(Math.max(1.5D, 3.0D * scale));
        }

        // Поддерживаем дальность взаимодействия с блоками, чтобы ощущения были согласованы.
        AttributeInstance blockRange = getAttributeInstance(player, blockInteractionRangeAttribute);
        if (blockRange != null) {
            blockRange.setBaseValue(Math.max(2.0D, 4.5D * scale));
        }

        // Авто-ползание для очень маленького роста.
        player.setSwimming(height < 1.0D);
    }

    private Attribute resolveAttribute(String primary, String fallback) {
        try {
            return Attribute.valueOf(primary);
        } catch (IllegalArgumentException ignored) {
            try {
                return Attribute.valueOf(fallback);
            } catch (IllegalArgumentException ignoredAgain) {
                return null;
            }
        }
    }

    private AttributeInstance getAttributeInstance(Player player, Attribute attribute) {
        return attribute == null ? null : player.getAttribute(attribute);
    }

    private double clamp(double value) {
        return Math.max(minHeight, Math.min(maxHeight, value));
    }

    private void persistAsync() {
        Map<UUID, Double> snapshot = new HashMap<>(heights);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> playerDataStore.writeHeights(snapshot));
    }

    public void shutdown() {
        animationTasks.values().forEach(BukkitTask::cancel);
        animationTasks.clear();
        playerDataStore.writeHeights(new HashMap<>(heights));
    }
}
