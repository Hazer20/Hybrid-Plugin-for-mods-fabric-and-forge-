package ru.hybridplugin.securityprefix.listener;

import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import ru.hybridplugin.securityprefix.service.WhitelistService;

public class CustomWhitelistListener implements Listener {

    private final WhitelistService whitelistService;

    public CustomWhitelistListener(WhitelistService whitelistService) {
        this.whitelistService = whitelistService;
    }

    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (!whitelistService.isWhitelisted(event.getName())) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST,
                    Component.text("§cВы не в whitelist сервера. Обратитесь к администрации."));
        }
    }
}
