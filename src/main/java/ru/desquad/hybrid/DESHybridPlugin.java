package ru.desquad.hybrid;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import ru.desquad.hybrid.command.DESCoinCommand;
import ru.desquad.hybrid.command.MarketCommand;
import ru.desquad.hybrid.command.QuestsCommand;
import ru.desquad.hybrid.economy.EconomyManager;
import ru.desquad.hybrid.gui.GUIListener;
import ru.desquad.hybrid.market.MarketManager;
import ru.desquad.hybrid.npc.BuilderNPCManager;
import ru.desquad.hybrid.npc.NPCChatListener;
import ru.desquad.hybrid.npc.NPCInteractListener;
import ru.desquad.hybrid.quest.QuestListener;
import ru.desquad.hybrid.quest.QuestManager;
import ru.desquad.hybrid.storage.DataStorage;
import ru.desquad.hybrid.storage.SQLiteStorage;
import ru.desquad.hybrid.storage.YamlStorage;
import ru.desquad.hybrid.util.Message;

/**
 * Главный класс плагина.
 *
 * Автор: Hazer_2_0
 * Версия: 1.0.0
 */
public class DESHybridPlugin extends JavaPlugin {

    private DataStorage dataStorage;
    private EconomyManager economyManager;
    private QuestManager questManager;
    private MarketManager marketManager;
    private BuilderNPCManager builderNPCManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        Message.init(this);

        String storageType = getConfig().getString("storage.type", "YAML").toUpperCase();
        if ("SQLITE".equals(storageType)) {
            dataStorage = new SQLiteStorage(this);
        } else {
            dataStorage = new YamlStorage(this);
        }
        dataStorage.init();

        economyManager = new EconomyManager(this, dataStorage);
        questManager = new QuestManager(this, dataStorage, economyManager);
        marketManager = new MarketManager(this, dataStorage, economyManager);
        builderNPCManager = new BuilderNPCManager(this, economyManager);

        registerCommands();
        registerListeners();

        builderNPCManager.spawnOrRespawnNPC();
        questManager.startDailyResetTask();
        getLogger().info("DESHybridPlugin успешно запущен!");
    }

    @Override
    public void onDisable() {
        if (questManager != null) {
            questManager.shutdown();
        }
        if (marketManager != null) {
            marketManager.shutdown();
        }
        if (builderNPCManager != null) {
            builderNPCManager.cleanup();
        }
        if (dataStorage != null) {
            dataStorage.close();
        }
        getLogger().info("DESHybridPlugin выключен.");
    }

    private void registerCommands() {
        getCommand("descoin").setExecutor(new DESCoinCommand(this, economyManager));
        getCommand("desquests").setExecutor(new QuestsCommand(questManager));
        getCommand("desmarket").setExecutor(new MarketCommand(marketManager));
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new QuestListener(this, questManager), this);
        Bukkit.getPluginManager().registerEvents(new GUIListener(this, questManager, marketManager, builderNPCManager), this);
        Bukkit.getPluginManager().registerEvents(new NPCChatListener(this, builderNPCManager), this);
        Bukkit.getPluginManager().registerEvents(new NPCInteractListener(this, builderNPCManager), this);
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public QuestManager getQuestManager() {
        return questManager;
    }

    public MarketManager getMarketManager() {
        return marketManager;
    }

    public BuilderNPCManager getBuilderNPCManager() {
        return builderNPCManager;
    }
}
