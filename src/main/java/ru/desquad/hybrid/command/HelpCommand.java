package ru.desquad.hybrid.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import ru.desquad.hybrid.DESHybridPlugin;

import java.util.List;

public class HelpCommand implements CommandExecutor {

    private final DESHybridPlugin plugin;

    public HelpCommand(DESHybridPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage("§6§l=== DES HYBRID HELP ===");
        sender.sendMessage("§e/descoin balance [игрок] §7- баланс DESCoin");
        sender.sendMessage("§e/descoin pay <игрок> <сумма> §7- перевод DESCoin");
        sender.sendMessage("§e/descoin give <игрок> <сумма> §7- выдать DESCoin (админ)");
        sender.sendMessage("§e/desquests §7- открыть мини-квесты");
        sender.sendMessage("§e/desmarket §7- открыть рынок");
        sender.sendMessage("§e/desnpccraft §7- открыть крафт призыва NPC");
        sender.sendMessage("§e/desnpccraft admin reset <игрок> §7- сброс лимита крафта (OP)");
        sender.sendMessage("§e/desnpccraft admin give <игрок> §7- выдать спавнер NPC (OP)");
        sender.sendMessage("§e/deshelp §7- это меню помощи");
        sender.sendMessage("§7Фразы для активации меню у NPC:");

        List<String> phrases = plugin.getConfig().getStringList("npc-builder.command-phrases");
        for (String phrase : phrases) {
            sender.sendMessage(" §8• §f" + phrase);
        }
        return true;
    }
}
