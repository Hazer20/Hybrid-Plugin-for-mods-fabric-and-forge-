package dev.hazer.universe.lore;

import dev.hazer.universe.FracturedUniverse;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LoreManager {
    private final FracturedUniverse plugin;
    private final File loreFile;
    private YamlConfiguration loreData;

    public LoreManager(FracturedUniverse plugin) {
        this.plugin = plugin;
        this.loreFile = new File(plugin.getDataFolder(), "player_lore.yml");
    }

    public void load() {
        if (!loreFile.exists()) {
            try {
                loreFile.getParentFile().mkdirs();
                loreFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("Cannot create player_lore.yml: " + e.getMessage());
            }
        }
        loreData = YamlConfiguration.loadConfiguration(loreFile);
    }

    public void save() {
        try {
            loreData.save(loreFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Cannot save player_lore.yml: " + e.getMessage());
        }
    }

    public void addEntry(Player player, String entry) {
        int max = plugin.getConfig().getInt("lore.max-entry-length", 320);
        String safe = entry.length() > max ? entry.substring(0, max) : entry;

        UUID id = player.getUniqueId();
        String path = "players." + id + ".entries";
        List<String> entries = new ArrayList<>(loreData.getStringList(path));
        entries.add("[" + System.currentTimeMillis() + "] " + safe);
        loreData.set(path, entries);
        save();
    }

    public List<String> getEntries(Player player) {
        return loreData.getStringList("players." + player.getUniqueId() + ".entries");
    }
}
