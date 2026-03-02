package com.hazer.hazefishing.command;

import com.hazer.hazefishing.HazerFishingPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;

public final class HFAdminCommand implements CommandExecutor, TabCompleter {
    private final HazerFishingPlugin plugin;

    public HFAdminCommand(HazerFishingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return plugin.getAdminCommandManager().handle(sender, args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return plugin.getAdminCommandManager().tabComplete(args);
    }
}
