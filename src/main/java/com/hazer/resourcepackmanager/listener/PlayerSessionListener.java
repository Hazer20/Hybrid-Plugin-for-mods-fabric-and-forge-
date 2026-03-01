package com.hazer.resourcepackmanager.listener;

import com.hazer.resourcepackmanager.service.PlayerPromptService;
import com.hazer.resourcepackmanager.util.PluginLogger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

import java.util.Objects;

/**
 * Tracks player session-related events for cleanup and diagnostics.
 */
public final class PlayerSessionListener implements Listener {
    private final PlayerPromptService promptService;
    private final PluginLogger logger;

    /**
     * Constructs listener.
     *
     * @param promptService prompt service
     * @param logger logger
     */
    public PlayerSessionListener(final PlayerPromptService promptService,
                                 final PluginLogger logger) {
        this.promptService = Objects.requireNonNull(promptService, "promptService");
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    /**
     * Clears player prompt state on disconnect to avoid stale map entries.
     *
     * @param event quit event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(final PlayerQuitEvent event) {
        promptService.clearPlayer(event.getPlayer().getUniqueId());
        logger.debug("Очищено состояние prompt для игрока: " + event.getPlayer().getName());
    }

    /**
     * Logs client-side resource pack status updates.
     *
     * @param event resource pack status event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onResourcePackStatus(final PlayerResourcePackStatusEvent event) {
        logger.info("Статус ресурс-пака от игрока " + event.getPlayer().getName() + ": " + event.getStatus());

        switch (event.getStatus()) {
            case ACCEPTED -> event.getPlayer().sendMessage(Component.text("Клиент принял ресурс-пак.", NamedTextColor.GREEN));
            case DECLINED -> event.getPlayer().sendMessage(Component.text("Клиент отклонил ресурс-пак.", NamedTextColor.YELLOW));
            case FAILED_DOWNLOAD -> event.getPlayer().sendMessage(Component.text("Не удалось скачать ресурс-пак.", NamedTextColor.RED));
            case SUCCESSFULLY_LOADED -> event.getPlayer().sendMessage(Component.text("Ресурс-пак успешно загружен!", NamedTextColor.AQUA));
            default -> logger.debug("Получен дополнительный статус: " + event.getStatus());
        }
    }
}
