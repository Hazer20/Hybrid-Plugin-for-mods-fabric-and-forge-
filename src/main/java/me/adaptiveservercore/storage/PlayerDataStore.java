package me.adaptiveservercore.storage;

import me.adaptiveservercore.AdaptiveServerCore;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataStore {

    private final AdaptiveServerCore plugin;
    private final File file;
    private YamlConfiguration configuration;

    public PlayerDataStore(AdaptiveServerCore plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "playerdata.yml");
    }

    public void load() {
        if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
            plugin.getLogger().warning("Не удалось создать папку плагина для playerdata.yml");
        }
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    plugin.getLogger().warning("Не удалось создать playerdata.yml");
                }
            } catch (IOException e) {
                plugin.getLogger().severe("Ошибка создания playerdata.yml: " + e.getMessage());
            }
        }
        this.configuration = YamlConfiguration.loadConfiguration(file);
    }

    public Map<UUID, Double> readHeights() {
        Map<UUID, Double> heights = new HashMap<>();
        if (configuration == null) {
            load();
        }

        if (!configuration.isConfigurationSection("игроки")) {
            return heights;
        }

        for (String key : configuration.getConfigurationSection("игроки").getKeys(false)) {
            String path = "игроки." + key + ".рост";
            if (!configuration.isDouble(path)) {
                continue;
            }
            try {
                heights.put(UUID.fromString(key), configuration.getDouble(path));
            } catch (IllegalArgumentException ignored) {
                plugin.getLogger().warning("Некорректный UUID в playerdata.yml: " + key);
            }
        }
        return heights;
    }

    public synchronized void writeHeights(Map<UUID, Double> heights) {
        if (configuration == null) {
            load();
        }
        configuration.set("игроки", null);
        heights.forEach((uuid, height) -> configuration.set("игроки." + uuid + ".рост", height));
        saveNow();
    }

    public synchronized void saveNow() {
        if (configuration == null) {
            return;
        }
        try {
            configuration.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Ошибка сохранения playerdata.yml: " + e.getMessage());
        }
    }
}
