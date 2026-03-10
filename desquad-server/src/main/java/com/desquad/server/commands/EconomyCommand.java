package com.desquad.server.commands;

import com.desquad.api.DESquadAPI;
import java.math.BigDecimal;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/** Админ-команды экономики DE Coins. */
public final class EconomyCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§cUsage: /economy give|take|set <player> <amount>");
            return true;
        }
        var player = Bukkit.getOfflinePlayer(args[1]);
        final BigDecimal amount;
        try {
            amount = new BigDecimal(args[2]);
        } catch (NumberFormatException ex) {
            sender.sendMessage("§cAmount must be numeric");
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "give" -> DESquadAPI.getEconomy().give(player.getUniqueId(), amount);
            case "take" -> DESquadAPI.getEconomy().take(player.getUniqueId(), amount);
            case "set" -> DESquadAPI.getEconomy().setBalance(player.getUniqueId(), amount);
            default -> {
                sender.sendMessage("§cUnknown action");
                return true;
            }
        }
        sender.sendMessage("§aUpdated balance for " + player.getName());
        return true;
    }
}
