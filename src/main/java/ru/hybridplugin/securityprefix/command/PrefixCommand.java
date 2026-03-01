package ru.hybridplugin.securityprefix.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.hybridplugin.securityprefix.service.PrefixService;

import java.util.Arrays;

public class PrefixCommand implements CommandExecutor {

    private final PrefixService prefixService;

    public PrefixCommand(PrefixService prefixService) {
        this.prefixService = prefixService;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Эта команда только для игроков.");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("menu")) {
            prefixService.openMenu(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("set")) {
            if (args.length < 2) {
                player.sendMessage("§cИспользование: /prefix set <текст>");
                return true;
            }

            String text = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            boolean ok = prefixService.setCustomPrefix(player, text);
            player.sendMessage(ok ? "§aОбычный префикс установлен." : "§cПрефикс слишком длинный.");
            return true;
        }

        if (args[0].equalsIgnoreCase("premium")) {
            if (args.length < 2) {
                player.sendMessage("§cИспользование: /prefix premium <текст>");
                return true;
            }

            String text = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            String result = prefixService.buyAndSetPremium(player, text);
            switch (result) {
                case "ok" -> player.sendMessage("§aПремиум префикс установлен.");
                case "no_permission" -> player.sendMessage("§cУ вас нет доступа к премиум-префиксу.");
                default -> player.sendMessage("§cПрефикс слишком длинный.");
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("clear")) {
            prefixService.clearPrefix(player);
            player.sendMessage("§aПрефикс сброшен.");
            return true;
        }

        player.sendMessage("§e/prefix menu");
        player.sendMessage("§e/prefix set <текст>");
        player.sendMessage("§e/prefix premium <текст>");
        player.sendMessage("§e/prefix clear");
        return true;
    }
}
