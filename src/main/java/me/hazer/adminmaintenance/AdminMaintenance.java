package me.hazer.adminmaintenance;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class AdminMaintenance extends JavaPlugin implements Listener {

    private static final String MAINTENANCE_MESSAGE = "§cСервер временно закрыт.\n"
            + "§7Сейчас проводятся технические работы.\n"
            + "§eПопробуйте зайти позже.";

    private boolean maintenanceEnabled = true;

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
        if (maintenanceEnabled && !event.getPlayer().isOp()) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, MAINTENANCE_MESSAGE);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("maintenance")) {
            return false;
        }

        if (!sender.isOp()) {
            sender.sendMessage("§cУ вас нет прав для этой команды.");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage("§eИспользование: /maintenance <on|off>");
            return true;
        }

        if (args[0].equalsIgnoreCase("on")) {
            maintenanceEnabled = true;
            sender.sendMessage("§aРежим техработ включен.");
            return true;
        }

        if (args[0].equalsIgnoreCase("off")) {
            maintenanceEnabled = false;
            sender.sendMessage("§aРежим техработ выключен.");
            return true;
        }

        sender.sendMessage("§eИспользование: /maintenance <on|off>");
        return true;
    }
}
