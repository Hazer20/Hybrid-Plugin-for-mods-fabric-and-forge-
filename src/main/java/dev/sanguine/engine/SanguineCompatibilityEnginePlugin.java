package dev.sanguine.engine;

import dev.sanguine.engine.runtime.RuntimeItemBridgeListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class SanguineCompatibilityEnginePlugin extends JavaPlugin {
    private PackConversionEngine engine;

    @Override
    public void onEnable() {
        try {
            saveDefaultConfig();
            engine = new PackConversionEngine(this);
            engine.prepareDirectories();
            engine.runConversion();

            getServer().getPluginManager().registerEvents(new RuntimeItemBridgeListener(this), this);
            getLogger().info("SanguineCompatibilityEngine enabled.");
        } catch (Exception ex) {
            getLogger().severe("SanguineCompatibilityEngine startup protection caught error: " + ex.getMessage());
        }
    }

    @Override
    public void onDisable() {
        if (engine != null) {
            engine.shutdown();
        }
        Bukkit.getScheduler().cancelTasks(this);
    }
}
