package me.adaptiveservercore;

import me.adaptiveservercore.api.HeightAPI;
import me.adaptiveservercore.automation.AutomationManager;
import me.adaptiveservercore.height.command.HeightCommand;
import me.adaptiveservercore.height.gui.HeightGui;
import me.adaptiveservercore.height.listener.HeightGameplayListener;
import me.adaptiveservercore.height.listener.HeightInputListener;
import me.adaptiveservercore.height.HeightManager;
import me.adaptiveservercore.placeholder.AdaptivePlaceholderExpansion;
import me.adaptiveservercore.storage.PlayerDataStore;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class AdaptiveServerCore extends JavaPlugin {

    private HeightManager heightManager;
    private HeightGui heightGui;
    private PlayerDataStore playerDataStore;
    private AutomationManager automationManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.playerDataStore = new PlayerDataStore(this);
        this.playerDataStore.load();

        this.heightManager = new HeightManager(this, playerDataStore);
        this.heightGui = new HeightGui(this, heightManager);
        this.automationManager = new AutomationManager(this);

        HeightAPI.initialize(heightManager);

        HeightCommand heightCommand = new HeightCommand(this, heightManager, heightGui);
        if (getCommand("height") != null) {
            getCommand("height").setExecutor(heightCommand);
            getCommand("height").setTabCompleter(heightCommand);
        }

        Bukkit.getPluginManager().registerEvents(new HeightGameplayListener(this, heightManager), this);
        Bukkit.getPluginManager().registerEvents(new HeightInputListener(this, heightManager, heightGui), this);

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new AdaptivePlaceholderExpansion(this, heightManager).register();
            getLogger().info("Поддержка PlaceholderAPI успешно включена.");
        }

        heightManager.initializeOnlinePlayers();
        automationManager.start();
        getLogger().info("AdaptiveServerCore успешно включён.");
    }

    @Override
    public void onDisable() {
        if (automationManager != null) {
            automationManager.shutdown();
        }
        if (heightManager != null) {
            heightManager.shutdown();
        }
        if (playerDataStore != null) {
            playerDataStore.saveNow();
        }
        getLogger().info("AdaptiveServerCore выключен.");
    }

    public HeightManager getHeightManager() {
        return heightManager;
    }
}
