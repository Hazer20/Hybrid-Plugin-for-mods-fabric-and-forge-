package ru.hybridplugin.securityprefix.service;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WhitelistService {

    private final JavaPlugin plugin;
    private final File file;
    private YamlConfiguration config;

    public WhitelistService(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "whitelist.yml");
        load();
    }

    public synchronized void load() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new IllegalStateException("Не удалось создать whitelist.yml", e);
            }
        }

        this.config = YamlConfiguration.loadConfiguration(file);
        if (!config.contains("players")) {
            config.set("players", new ArrayList<String>());
            save();
        }
    }

    public synchronized boolean isWhitelisted(String nickname) {
        return getPlayers().contains(normalize(nickname));
    }

    public synchronized boolean addPlayer(String nickname) {
        String normalized = normalize(nickname);
        List<String> players = getPlayers();
        if (players.contains(normalized)) {
            return false;
        }
        players.add(normalized);
        config.set("players", players);
        save();
        return true;
    }

    public synchronized boolean removePlayer(String nickname) {
        String normalized = normalize(nickname);
        List<String> players = getPlayers();
        if (!players.remove(normalized)) {
            return false;
        }
        config.set("players", players);
        save();
        return true;
    }

    public synchronized List<String> getPlayers() {
        List<String> raw = config.getStringList("players");
        List<String> normalized = new ArrayList<>();
        for (String entry : raw) {
            normalized.add(normalize(entry));
        }
        return normalized;
    }

    private String normalize(String nickname) {
        return nickname.toLowerCase(Locale.ROOT);
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось сохранить whitelist.yml", e);
        }
    }
}
