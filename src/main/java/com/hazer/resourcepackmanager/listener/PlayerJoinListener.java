package com.hazer.resourcepackmanager.listener;

import com.hazer.resourcepackmanager.service.PlayerPromptService;
import com.hazer.resourcepackmanager.util.PluginLogger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Objects;

/**
 * Listens for player join and starts resource pack offer flow.
 */
public final class PlayerJoinListener implements Listener {
    private final PlayerPromptService promptService;
    private final PluginLogger logger;

    /**
     * @param promptService prompt service
     * @param logger logger helper
     */
    public PlayerJoinListener(final PlayerPromptService promptService,
                              final PluginLogger logger) {
        this.promptService = Objects.requireNonNull(promptService, "promptService");
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    /**
     * Triggered after player joins; sends offer message.
     *
     * @param event join event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        logger.debug("PlayerJoinEvent: " + event.getPlayer().getName());
        promptService.promptPlayer(event.getPlayer());
    }
}
