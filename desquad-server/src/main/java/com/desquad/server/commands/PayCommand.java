package com.desquad.server.commands;

import com.desquad.api.DESquadAPI;
import java.math.BigDecimal;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Команда перевода DE Coins между игроками. */
public final class PayCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use /pay");
            return true;
        }
        if (args.length < 2) {
            player.sendMessage("§cUsage: /pay <player> <amount>");
            return true;
        }
        var target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            player.sendMessage("§cPlayer not found");
            return true;
        }
        final BigDecimal amount;
        try {
            amount = new BigDecimal(args[1]);
        } catch (NumberFormatException ex) {
            player.sendMessage("§cAmount must be numeric");
            return true;
        }
        if (amount.signum() <= 0) {
            player.sendMessage("§cAmount must be > 0");
            return true;
        }
        var economy = DESquadAPI.getEconomy();
        if (economy.getBalance(player.getUniqueId()).compareTo(amount) < 0) {
            player.sendMessage("§cNot enough DE Coins");
            return true;
        }
        economy.take(player.getUniqueId(), amount);
        economy.give(target.getUniqueId(), amount);
        player.sendMessage("§aTransferred " + amount + " DE Coins to " + target.getName());
        target.sendMessage("§aYou received " + amount + " DE Coins from " + player.getName());
        return true;
    }
}
