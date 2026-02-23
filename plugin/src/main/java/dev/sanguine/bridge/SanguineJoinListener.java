package dev.sanguine.bridge;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class SanguineJoinListener implements Listener {

    private final SanguineBridgePlugin plugin;

    public SanguineJoinListener(SanguineBridgePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.enableTriggerFor(event.getPlayer());
    }
}
