package com.hazer.resourcepackmanager;

import com.hazer.resourcepackmanager.command.ResourcePackCommand;
import com.hazer.resourcepackmanager.listener.PlayerJoinListener;
import com.hazer.resourcepackmanager.listener.PlayerSessionListener;
import com.hazer.resourcepackmanager.listener.ServerLoadListener;
import com.hazer.resourcepackmanager.manager.ResourcePackManagerService;
import com.hazer.resourcepackmanager.model.PackAvailability;
import com.hazer.resourcepackmanager.model.PluginState;
import com.hazer.resourcepackmanager.service.PlayerPromptService;
import com.hazer.resourcepackmanager.util.PluginLogger;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

/**
 * Main plugin class.
 * <p>
 * This plugin automatically discovers resource pack zip files in the server root
 * {@code resourcepacks} directory and selects the newest one as active.
 * Players are prompted on join to accept or decline the pack.
 *
 * <h2>Lifecycle summary</h2>
 * <ol>
 *     <li>onLoad: set state and prepare defaults.</li>
 *     <li>onEnable: load config, initialize services, register commands/listeners,
 *     ensure folder exists and initial scan.</li>
 *     <li>ServerLoadEvent: re-validate and reselect active pack after server fully loads.</li>
 *     <li>onDisable: cleanup and set final state.</li>
 * </ol>
 */
public final class ResourcePackManagerPlugin extends JavaPlugin {
    private volatile PluginState pluginState = PluginState.BOOTSTRAPPING;

    private PluginLogger logger;
    private ResourcePackManagerService packManagerService;
    private PlayerPromptService playerPromptService;

    /**
     * Called by Bukkit before plugin enabling.
     */
    @Override
    public void onLoad() {
        pluginState = PluginState.BOOTSTRAPPING;
    }

    /**
     * Performs full plugin initialization.
     */
    @Override
    public void onEnable() {
        pluginState = PluginState.INITIALIZING;

        saveDefaultConfig();
        reloadConfig();

        this.logger = new PluginLogger(this, getConfig().getBoolean("logging.debug", true));
        logger.info("Запуск ResourcePackManagerPlugin v" + getDescription().getVersion());

        this.packManagerService = new ResourcePackManagerService(this, logger);
        this.playerPromptService = new PlayerPromptService(this, packManagerService, logger);

        final boolean folderReady = packManagerService.ensureResourcepacksDirectory();
        final PackAvailability initialAvailability = folderReady
                ? packManagerService.reload(true)
                : PackAvailability.DIRECTORY_READ_ERROR;

        registerCommand();
        registerListeners();

        pluginState = switch (initialAvailability) {
            case AVAILABLE -> PluginState.ACTIVE;
            case NO_PACKS_FOUND -> PluginState.ACTIVE_WITHOUT_PACK;
            case DIRECTORY_READ_ERROR, URL_CONFIGURATION_ERROR -> PluginState.ERROR;
        };

        logger.info("Плагин включен. Состояние: " + pluginState);
    }

    /**
     * Called when plugin is disabled.
     */
    @Override
    public void onDisable() {
        pluginState = PluginState.SHUTTING_DOWN;
        if (logger != null) {
            logger.info("Отключение ResourcePackManagerPlugin...");
        }
        pluginState = PluginState.DISABLED;
    }

    /**
     * Returns current plugin state.
     *
     * @return plugin state enum
     */
    public PluginState getPluginState() {
        return pluginState;
    }

    /**
     * @return resource pack manager service
     */
    public ResourcePackManagerService getPackManagerService() {
        return Objects.requireNonNull(packManagerService, "packManagerService");
    }

    /**
     * @return player prompt service
     */
    public PlayerPromptService getPlayerPromptService() {
        return Objects.requireNonNull(playerPromptService, "playerPromptService");
    }

    /**
     * @return plugin logger wrapper
     */
    public PluginLogger getPluginLogger() {
        return Objects.requireNonNull(logger, "logger");
    }

    private void registerCommand() {
        final PluginCommand command = getCommand("resourcepack");
        if (command == null) {
            if (logger != null) {
                logger.error("Команда /resourcepack не найдена в plugin.yml! Плагин будет отключен.");
            }
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        final ResourcePackCommand executor = new ResourcePackCommand(packManagerService, playerPromptService, logger);
        command.setExecutor(executor);
        command.setTabCompleter(executor);
        logger.debug("Команда /resourcepack успешно зарегистрирована.");
    }

    private void registerListeners() {
        final PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerJoinListener(playerPromptService, logger), this);
        pm.registerEvents(new ServerLoadListener(packManagerService, logger), this);
        pm.registerEvents(new PlayerSessionListener(playerPromptService, logger), this);
        logger.debug("Слушатели событий зарегистрированы.");
    }
}
