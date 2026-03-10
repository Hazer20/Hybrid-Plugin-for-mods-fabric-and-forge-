package com.desquad.server.commands;

import com.desquad.api.DESquadAPI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Команда просмотра баланса DE Coins. */
public final class BalanceCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use /balance");
            return true;
        }
        var balance = DESquadAPI.getEconomy().getBalance(player.getUniqueId());
        player.sendMessage("§6DE Coins: §f" + balance);
        return true;
    }
}
