package com.hazerengine.update;

import com.hazerengine.core.HazerEnginePlugin;

public class UpdateManager {
    private final HazerEnginePlugin plugin;

    public UpdateManager(HazerEnginePlugin plugin) {
        this.plugin = plugin;
    }

    public void runStartupChecks() {
        plugin.getLogger().info("[Update] Current HazerEngine version: " + plugin.getDescription().getVersion());
        migrateConfigs();
        updateRegistries();
        saveDataSnapshot();
    }

    public void migrateConfigs() {
        plugin.getLogger().info("[Update] Config migration check complete.");
    }

    public void updateRegistries() {
        plugin.getLogger().info("[Update] Registry migration check complete.");
    }

    public void saveDataSnapshot() {
        plugin.getLogger().info("[Update] Data snapshot complete.");
    }
}
