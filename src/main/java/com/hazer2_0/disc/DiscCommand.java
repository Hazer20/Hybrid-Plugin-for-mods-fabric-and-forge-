package com.hazer2_0.disc;

import com.hazer2_0.HazerPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DiscCommand implements CommandExecutor {
    private final HazerPlugin plugin;
    private final DiscManager discManager;

    public DiscCommand(HazerPlugin plugin, DiscManager discManager) {
        this.plugin = plugin;
        this.discManager = discManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        if (!player.hasPermission("hazer.disc.create")) {
            player.sendMessage("No permission.");
            return true;
        }
        if (args.length < 3 || !args[0].equalsIgnoreCase("create")) {
            player.sendMessage("Usage: /disc create <url> <name>");
            return true;
        }
        String url = args[1];
        String name = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));
        player.sendMessage("Downloading and processing disc asynchronously...");
        discManager.createDisc(player, url, name).whenComplete((item, ex) -> {
            if (ex != null) {
                player.sendMessage("Disc creation failed: " + ex.getCause().getMessage());
                plugin.getLogger().warning("Disc creation failed for " + player.getName() + ": " + ex.getMessage());
                return;
            }
            player.getInventory().addItem(item);
            player.sendMessage("Custom disc created: " + name);
        });
        return true;
    }
}
