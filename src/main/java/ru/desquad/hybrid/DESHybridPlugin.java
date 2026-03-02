package ru.desquad.hybrid;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import ru.desquad.hybrid.command.DESCoinCommand;
import ru.desquad.hybrid.command.MarketCommand;
import ru.desquad.hybrid.command.QuestsCommand;
import ru.desquad.hybrid.command.SchematicCommand;
import ru.desquad.hybrid.command.NPCCraftCommand;
import ru.desquad.hybrid.command.HelpCommand;
import ru.desquad.hybrid.economy.EconomyManager;
import ru.desquad.hybrid.gui.GUIListener;
import ru.desquad.hybrid.market.MarketManager;
import ru.desquad.hybrid.npc.BuilderNPCManager;
import ru.desquad.hybrid.npc.NPCChatListener;
import ru.desquad.hybrid.npc.NPCInteractListener;
import ru.desquad.hybrid.npc.NPCTokenUseListener;
import ru.desquad.hybrid.quest.QuestListener;
import ru.desquad.hybrid.quest.QuestManager;
import ru.desquad.hybrid.quest.QuestStatusBoardService;
import ru.desquad.hybrid.storage.DataStorage;
import ru.desquad.hybrid.storage.SQLiteStorage;
import ru.desquad.hybrid.storage.YamlStorage;
import ru.desquad.hybrid.util.Message;
import ru.desquad.hybrid.schematic.ExternalSchematicRepository;

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
    private QuestStatusBoardService questStatusBoardService;
    private ExternalSchematicRepository schematicRepository;

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

        schematicRepository = new ExternalSchematicRepository(this);
        schematicRepository.init();

        economyManager = new EconomyManager(this, dataStorage);
        questManager = new QuestManager(this, dataStorage, economyManager);
        marketManager = new MarketManager(this, dataStorage, economyManager);
        builderNPCManager = new BuilderNPCManager(this, economyManager, dataStorage, schematicRepository);
        questStatusBoardService = new QuestStatusBoardService(this, questManager);

        registerCommands();
        registerListeners();

        builderNPCManager.spawnOrRespawnNPC();
        questManager.startDailyResetTask();
        questStatusBoardService.start();
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
        if (questStatusBoardService != null) {
            questStatusBoardService.stop();
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
        getCommand("desnpccraft").setExecutor(new NPCCraftCommand(this, builderNPCManager));
        getCommand("deshelp").setExecutor(new HelpCommand(this));
        getCommand("desschematic").setExecutor(new SchematicCommand(this, schematicRepository));
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new QuestListener(questManager), this);
        Bukkit.getPluginManager().registerEvents(new GUIListener(this, questManager, marketManager, builderNPCManager), this);
        Bukkit.getPluginManager().registerEvents(new NPCChatListener(this, builderNPCManager), this);
        Bukkit.getPluginManager().registerEvents(new NPCInteractListener(this, builderNPCManager), this);
        Bukkit.getPluginManager().registerEvents(new NPCTokenUseListener(builderNPCManager), this);
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
