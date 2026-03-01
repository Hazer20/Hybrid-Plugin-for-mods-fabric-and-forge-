package com.hazer.resourcepackmanager.service;

import com.hazer.resourcepackmanager.manager.ResourcePackManagerService;
import com.hazer.resourcepackmanager.model.PackAvailability;
import com.hazer.resourcepackmanager.model.PackInfo;
import com.hazer.resourcepackmanager.util.PluginLogger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles interactive user flow for accepting or declining resource pack download.
 * <p>
 * The service keeps a lightweight pending-choice map keyed by UUID. This map allows
 * explicit accept/decline commands and future extension (timeouts, analytics, etc.).
 */
public final class PlayerPromptService {
    private final JavaPlugin plugin;
    private final ResourcePackManagerService packManager;
    private final PluginLogger logger;
    private final Map<UUID, PromptState> pendingPrompts;

    /**
     * Creates prompt service.
     *
     * @param plugin plugin reference
     * @param packManager pack manager service
     * @param logger logging helper
     */
    public PlayerPromptService(final JavaPlugin plugin,
                               final ResourcePackManagerService packManager,
                               final PluginLogger logger) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.packManager = Objects.requireNonNull(packManager, "packManager");
        this.logger = Objects.requireNonNull(logger, "logger");
        this.pendingPrompts = new ConcurrentHashMap<>();
    }

    /**
     * Sends initial interactive prompt to player, if a pack is available.
     *
     * @param player target player
     */
    public void promptPlayer(final Player player) {
        Objects.requireNonNull(player, "player");

        if (packManager.getAvailability() != PackAvailability.AVAILABLE) {
            final String unavailableMessage = plugin.getConfig().getString("messages.unavailable", "Ресурс-пак недоступен.");
            player.sendMessage(Component.text(unavailableMessage, NamedTextColor.RED));
            logger.debug("Игроку " + player.getName() + " отправлено уведомление о недоступности ресурс-пака.");
            return;
        }

        pendingPrompts.put(player.getUniqueId(), PromptState.WAITING_FOR_RESPONSE);

        final FileConfiguration cfg = plugin.getConfig();
        final String ask = cfg.getString("messages.ask", "Хотите скачать текстур пак? [Да / Нет]");
        final String yes = cfg.getString("messages.yes", "Да");
        final String no = cfg.getString("messages.no", "Нет");

        final Component yesButton = Component.text("[" + yes + "]", NamedTextColor.GREEN)
                .hoverEvent(HoverEvent.showText(Component.text("Нажмите, чтобы принять ресурс-пак", NamedTextColor.GRAY)))
                .clickEvent(ClickEvent.runCommand("/resourcepack accept"));

        final Component noButton = Component.text("[" + no + "]", NamedTextColor.RED)
                .hoverEvent(HoverEvent.showText(Component.text("Нажмите, чтобы отказаться", NamedTextColor.GRAY)))
                .clickEvent(ClickEvent.runCommand("/resourcepack decline"));

        final Component message = Component.text(ask + " ", NamedTextColor.YELLOW)
                .append(yesButton)
                .append(Component.space())
                .append(noButton);

        player.sendMessage(message);
        logger.info("Отправлен запрос на загрузку ресурс-пака игроку: " + player.getName());
    }

    /**
     * Handles explicit accept action from player or command sender.
     *
     * @param player target player
     * @return true when pack was attempted to be sent
     */
    public boolean acceptPack(final Player player) {
        Objects.requireNonNull(player, "player");

        final PromptState previous = pendingPrompts.getOrDefault(player.getUniqueId(), PromptState.NOT_ASKED);
        pendingPrompts.put(player.getUniqueId(), PromptState.ACCEPTED);

        final Optional<PackInfo> active = packManager.getActivePack();
        if (active.isEmpty()) {
            final String unavailable = plugin.getConfig().getString("messages.unavailable", "Ресурс-пак недоступен.");
            player.sendMessage(Component.text(unavailable, NamedTextColor.RED));
            logger.warn("Игрок " + player.getName() + " нажал принять, но активного пакета нет.");
            return false;
        }

        final PackInfo info = active.get();
        if (info.packUrl().isEmpty()) {
            player.sendMessage(Component.text("URL ресурс-пака не настроен. Сообщите администратору.", NamedTextColor.RED));
            logger.warn("Игрок " + player.getName() + " не получил пак: URL отсутствует.");
            return false;
        }

        final String acceptedMsg = plugin.getConfig().getString("messages.accepted", "Вы приняли ресурс-пак. Отправляю загрузку...");
        player.sendMessage(Component.text(acceptedMsg, NamedTextColor.GREEN));

        final boolean force = plugin.getConfig().getBoolean("force-resource-pack", false);
        final String packUrl = info.packUrl().orElseThrow();
        final byte[] hashBytes = info.sha1Hex()
                .map(hex -> hexStringToBytes(hex))
                .orElse(new byte[0]);

        final Component prompt = Component.text("Сервер предлагает активный текстур-пак: " + info.fileName(), NamedTextColor.AQUA);

        if (hashBytes.length == 20) {
            player.setResourcePack(packUrl, hashBytes, prompt, force);
            logger.info("Игроку " + player.getName() + " отправлен ресурс-пак с SHA1: " + info.fileName());
        } else {
            player.setResourcePack(packUrl, null, prompt, force);
            logger.warn("Игроку " + player.getName() + " отправлен ресурс-пак без SHA1: " + info.fileName());
        }

        if (previous == PromptState.NOT_ASKED) {
            logger.debug("Игрок " + player.getName() + " принял пакет без предварительного prompt (возможно ручная команда).");
        }

        return true;
    }

    /**
     * Handles explicit decline action.
     *
     * @param player target player
     */
    public void declinePack(final Player player) {
        Objects.requireNonNull(player, "player");
        pendingPrompts.put(player.getUniqueId(), PromptState.DECLINED);

        final String declinedMessage = plugin.getConfig().getString("messages.declined", "Вы отказались от загрузки ресурс-пака.");
        player.sendMessage(Component.text(declinedMessage, NamedTextColor.GRAY));
        logger.info("Игрок " + player.getName() + " отказался от ресурс-пака.");
    }

    /**
     * Returns prompt status of a player.
     *
     * @param playerId player UUID
     * @return state
     */
    public PromptState getPromptState(final UUID playerId) {
        return pendingPrompts.getOrDefault(playerId, PromptState.NOT_ASKED);
    }

    /**
     * Clears stored state for offline player.
     *
     * @param playerId player UUID
     */
    public void clearPlayer(final UUID playerId) {
        pendingPrompts.remove(playerId);
    }

    /**
     * Sends announcement to all online players that current pack changed.
     */
    public void announceActivePackChange() {
        final Optional<PackInfo> active = packManager.getActivePack();
        final String loadedTemplate = plugin.getConfig().getString("messages.loaded", "Активный ресурс-пак: %pack%");
        final String message = loadedTemplate.replace("%pack%", active.map(PackInfo::fileName).orElse("<none>"));

        Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(Component.text(message, NamedTextColor.GOLD)));
    }

    private byte[] hexStringToBytes(final String hex) {
        try {
            if (hex.length() % 2 != 0) {
                return new byte[0];
            }
            final byte[] data = new byte[hex.length() / 2];
            for (int i = 0; i < hex.length(); i += 2) {
                data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                        + Character.digit(hex.charAt(i + 1), 16));
            }
            return data;
        } catch (final RuntimeException runtimeException) {
            logger.warn("Не удалось преобразовать SHA1 hex в bytes: " + runtimeException.getMessage());
            return new byte[0];
        }
    }

    /**
     * Enum storing per-player prompt flow status.
     */
    public enum PromptState {
        /**
         * Player has not seen current prompt yet.
         */
        NOT_ASKED,

        /**
         * Prompt was shown and user has not decided.
         */
        WAITING_FOR_RESPONSE,

        /**
         * Player accepted loading of resource pack.
         */
        ACCEPTED,

        /**
         * Player declined loading of resource pack.
         */
        DECLINED
    }
}
