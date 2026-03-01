package ru.hybridplugin.securityprefix.service;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import ru.hybridplugin.securityprefix.util.PasswordUtil;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AuthService {

    private final JavaPlugin plugin;
    private final File file;
    private YamlConfiguration config;
    private final Set<UUID> loggedInPlayers = new HashSet<>();

    public AuthService(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "users.yml");
        load();
    }

    public synchronized void load() {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new IllegalStateException("Не удалось создать users.yml", e);
            }
        }
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public synchronized boolean isRegistered(UUID uuid) {
        return config.isConfigurationSection(path(uuid));
    }

    public synchronized boolean register(Player player, String password) {
        UUID uuid = player.getUniqueId();
        if (isRegistered(uuid)) {
            return false;
        }

        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hash(password, salt);
        String path = path(uuid);

        config.set(path + ".name", player.getName());
        config.set(path + ".salt", salt);
        config.set(path + ".hash", hash);
        config.set(path + ".registeredAt", System.currentTimeMillis());
        config.set(path + ".lastLogin", System.currentTimeMillis());
        save();

        loggedInPlayers.add(uuid);
        return true;
    }

    public synchronized boolean login(Player player, String password) {
        UUID uuid = player.getUniqueId();
        ConfigurationSection section = config.getConfigurationSection(path(uuid));
        if (section == null) {
            return false;
        }

        String salt = section.getString("salt", "");
        String expected = section.getString("hash", "");
        String actual = PasswordUtil.hash(password, salt);
        if (!actual.equals(expected)) {
            return false;
        }

        config.set(path(uuid) + ".lastLogin", System.currentTimeMillis());
        save();
        loggedInPlayers.add(uuid);
        return true;
    }

    public synchronized boolean isLoggedIn(UUID uuid) {
        return loggedInPlayers.contains(uuid);
    }

    public synchronized void markNeedsAuth(UUID uuid) {
        loggedInPlayers.remove(uuid);
    }

    public synchronized void logout(UUID uuid) {
        loggedInPlayers.remove(uuid);
    }

    private String path(UUID uuid) {
        return "users." + uuid;
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось сохранить users.yml", e);
        }
    }
}
