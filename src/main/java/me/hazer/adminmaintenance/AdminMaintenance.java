package me.hazer.adminmaintenance;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class AdminMaintenance extends JavaPlugin implements Listener {

    private static final String MAINTENANCE_MESSAGE = "§cСервер временно закрыт.\n"
            + "§7Сейчас проводятся технические работы.\n"
            + "§eПопробуйте зайти позже.";

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("AdminMaintenance enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("AdminMaintenance disabled.");
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (!event.getPlayer().isOp()) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, MAINTENANCE_MESSAGE);
        }
    }
}
