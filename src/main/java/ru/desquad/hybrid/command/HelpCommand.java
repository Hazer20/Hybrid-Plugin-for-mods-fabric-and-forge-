package ru.desquad.hybrid.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class HelpCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage("§6§l=== DES HYBRID HELP ===");
        sender.sendMessage("§e/descoin balance [игрок] §7- баланс DESCoin");
        sender.sendMessage("§e/descoin pay <игрок> <сумма> §7- перевод DESCoin");
        sender.sendMessage("§e/descoin give <игрок> <сумма> §7- выдать DESCoin (админ)");
        sender.sendMessage("§e/desquests §7- открыть мини-квесты");
        sender.sendMessage("§e/desmarket §7- открыть рынок");
        sender.sendMessage("§e/desnpccraft §7- открыть крафт призыва NPC");
        sender.sendMessage("§e/deshelp §7- это меню помощи");
        sender.sendMessage("§7Чтобы запустить стройку: подойди к NPC и скажи фразу-приказ в чат.");
        return true;
    }
}
