package dev.sanguine.engine;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Legacy entry kept only for backward compatibility with older jars/configs.
 */
public class SanguineCompatibilityEnginePlugin extends JavaPlugin {
    private PackConversionEngine engine;

    @Override
    public void onEnable() {
        engine = new PackConversionEngine(this);
        engine.prepareDirectories();
        engine.convertAllAsync(getServer().getConsoleSender());
        getLogger().warning("Using legacy main class. Please switch to HybridConverterPlugin.");
    }

    @Override
    public void onDisable() {
        if (engine != null) {
            engine.shutdown();
        }
        Bukkit.getScheduler().cancelTasks(this);
    }
}
