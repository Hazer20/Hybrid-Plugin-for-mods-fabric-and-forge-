package com.hazer.resourcepackmanager.listener;

import com.hazer.resourcepackmanager.manager.ResourcePackManagerService;
import com.hazer.resourcepackmanager.model.PackAvailability;
import com.hazer.resourcepackmanager.util.PluginLogger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;

import java.util.Objects;

/**
 * Reloads and validates resource packs when server has finished loading.
 */
public final class ServerLoadListener implements Listener {
    private final ResourcePackManagerService packManager;
    private final PluginLogger logger;

    /**
     * @param packManager service
     * @param logger logger
     */
    public ServerLoadListener(final ResourcePackManagerService packManager,
                              final PluginLogger logger) {
        this.packManager = Objects.requireNonNull(packManager, "packManager");
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    /**
     * Called after worlds/plugins are loaded.
     *
     * @param event event
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onServerLoad(final ServerLoadEvent event) {
        logger.info("ServerLoadEvent: " + event.getType() + ". Проверяем ресурс-паки...");
        final PackAvailability availability = packManager.reload(true);
        logger.info("Загрузка ресурс-паков завершена. Состояние: " + availability);
    }
}
