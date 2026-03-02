package com.hazer.hazefishing.manager;

import com.hazer.hazefishing.HazerFishingPlugin;

public final class AnimationScheduler {
    private final HazerFishingPlugin plugin;

    public AnimationScheduler(HazerFishingPlugin plugin) {
        this.plugin = plugin;
    }

    public void startPerformanceSampler() {
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            long used = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            if (plugin.getAdminCommandManager().isDebugPerformance()) {
                plugin.getLogger().info("[Debug] Memory used: " + (used / 1024 / 1024) + " MB");
            }
        }, 20L, 100L);
    }
}
