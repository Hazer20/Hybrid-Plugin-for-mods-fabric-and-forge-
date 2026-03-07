package com.hazer.march8;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.logging.Level;

/**
 * Main plugin entry point.
 *
 * <p>This plugin was designed for Paper 1.21.8 and Java 21.
 * It provides a full "8 March" celebration workflow:
 * - managing a list of players receiving congratulation gifts
 * - integrating with Oraxen for custom gifts
 * - graceful fallback to Bukkit items when Oraxen is absent
 * - visual effects and timed server-wide announcements
 *
 * <p>Author: Hazer_2_0
 */
public final class March8Plugin extends JavaPlugin {

    private ConfigManager configManager;
    private GirlManager girlManager;
    private OraxenHook oraxenHook;
    private ParticleManager particleManager;
    private FireworkManager fireworkManager;
    private GiftManager giftManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResourceIfMissing("messages.yml");

        this.configManager = new ConfigManager(this);
        this.configManager.reloadAll();

        this.girlManager = new GirlManager(this, configManager);
        this.oraxenHook = new OraxenHook(this, configManager);
        this.particleManager = new ParticleManager(this, configManager);
        this.fireworkManager = new FireworkManager(this, configManager);
        this.giftManager = new GiftManager(this, configManager, oraxenHook);

        registerCommandAndListeners();
        logStartupSummary();
    }

    @Override
    public void onDisable() {
        if (particleManager != null) {
            particleManager.shutdown();
        }
        getLogger().info("March8Plugin disabled. Happy International Women's Day!");
    }

    private void registerCommandAndListeners() {
        March8Command command = new March8Command(this, configManager, girlManager, giftManager, particleManager, fireworkManager);
        Objects.requireNonNull(getCommand("march8"), "Command march8 is not defined in plugin.yml").setExecutor(command);
        Objects.requireNonNull(getCommand("march8"), "Command march8 is not defined in plugin.yml").setTabCompleter(command);

        Bukkit.getPluginManager().registerEvents(giftManager, this);
        Bukkit.getPluginManager().registerEvents(particleManager, this);
    }

    private void saveResourceIfMissing(@NotNull String path) {
        if (getResource(path) == null) {
            getLogger().warning("Embedded resource not found: " + path);
            return;
        }

        try {
            saveResource(path, false);
        } catch (IllegalArgumentException ignored) {
            // Already exists, that is expected for normal startups.
        }
    }

    private void logStartupSummary() {
        String sep = "====================================================";
        getLogger().info(sep);
        getLogger().info("March8Plugin enabled!");
        getLogger().info("Author: Hazer_2_0");
        getLogger().info("Server version: " + Bukkit.getVersion());
        getLogger().info("Oraxen detected: " + (oraxenHook.isOraxenInstalled() ? "yes" : "no (fallback mode)"));
        getLogger().info("Configured girls count: " + girlManager.getGirls().size());
        getLogger().info(sep);
    }

    /**
     * Reload plugin runtime systems.
     */
    public void reloadPlugin() {
        try {
            reloadConfig();
            configManager.reloadAll();
            girlManager.reloadFromConfig();
            oraxenHook.refreshState();
            particleManager.reloadConfiguration();
            fireworkManager.reloadConfiguration();
            giftManager.reloadConfiguration();
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, "Failed to reload plugin cleanly", ex);
            throw ex;
        }
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public GirlManager getGirlManager() {
        return girlManager;
    }

    public OraxenHook getOraxenHook() {
        return oraxenHook;
    }

    public ParticleManager getParticleManager() {
        return particleManager;
    }

    public FireworkManager getFireworkManager() {
        return fireworkManager;
    }

    public GiftManager getGiftManager() {
        return giftManager;
    }
}
