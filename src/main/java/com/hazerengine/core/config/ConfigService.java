package com.hazerengine.core.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ConfigService {
    private final JavaPlugin plugin;
    private final Map<String, FileConfiguration> loaded = new HashMap<>();

    public ConfigService(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadDefaults() {
        plugin.getDataFolder().mkdirs();
        for (String file : new String[]{"config.yml", "items.yml", "abilities.yml", "recipes.yml", "mobs.yml", "quests.yml"}) {
            plugin.saveResource(file, false);
            File cfg = new File(plugin.getDataFolder(), file);
            loaded.put(file, YamlConfiguration.loadConfiguration(cfg));
        }
    }

    public Map<String, FileConfiguration> all() {
        return loaded;
    }
}
