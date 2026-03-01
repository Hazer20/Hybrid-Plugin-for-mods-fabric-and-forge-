package ru.hybridplugin.securityprefix.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import ru.hybridplugin.securityprefix.service.AuthService;

import java.util.Set;

public class AuthProtectionListener implements Listener {

    private static final Set<String> ALLOWED_COMMANDS = Set.of("/login", "/register");
    private final AuthService authService;

    public AuthProtectionListener(AuthService authService) {
        this.authService = authService;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        authService.markNeedsAuth(event.getPlayer().getUniqueId());
        event.getPlayer().setInvulnerable(true);
        if (authService.isRegistered(event.getPlayer().getUniqueId())) {
            event.getPlayer().sendMessage("§eВведите /login <пароль> для входа.");
        } else {
            event.getPlayer().sendMessage("§eДобро пожаловать! Зарегистрируйтесь: /register <пароль> <пароль>");
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        authService.logout(event.getPlayer().getUniqueId());
        event.getPlayer().setInvulnerable(false);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof org.bukkit.entity.Player player)) {
            return;
        }

        if (!authService.isLoggedIn(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!authService.isLoggedIn(event.getPlayer().getUniqueId())
                && event.getFrom().toVector().distanceSquared(event.getTo().toVector()) > 0) {
            event.setTo(event.getFrom());
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (!authService.isLoggedIn(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cСначала войдите: /login <пароль>");
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (authService.isLoggedIn(event.getPlayer().getUniqueId())) {
            return;
        }

        String message = event.getMessage().toLowerCase();
        boolean allowed = ALLOWED_COMMANDS.stream().anyMatch(message::startsWith);
        if (!allowed) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cПока вы не вошли, доступны только /register и /login.");
        }
    }
}
