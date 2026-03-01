package ru.hybridplugin.securityprefix.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.hybridplugin.securityprefix.service.WhitelistService;

import java.util.ArrayList;
import java.util.List;

public class CustomWhitelistCommand implements CommandExecutor, TabCompleter {

    private final WhitelistService whitelistService;

    public CustomWhitelistCommand(WhitelistService whitelistService) {
        this.whitelistService = whitelistService;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§e/cwhitelist add <ник>");
            sender.sendMessage("§e/cwhitelist remove <ник>");
            sender.sendMessage("§e/cwhitelist list");
            sender.sendMessage("§e/cwhitelist reload");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "add" -> {
                if (args.length < 2) {
                    sender.sendMessage("§cУкажите ник.");
                    return true;
                }
                boolean added = whitelistService.addPlayer(args[1]);
                sender.sendMessage(added ? "§aИгрок добавлен в custom whitelist." : "§eИгрок уже есть в whitelist.");
            }
            case "remove" -> {
                if (args.length < 2) {
                    sender.sendMessage("§cУкажите ник.");
                    return true;
                }
                boolean removed = whitelistService.removePlayer(args[1]);
                sender.sendMessage(removed ? "§aИгрок удален из custom whitelist." : "§eИгрока нет в whitelist.");
            }
            case "list" -> sender.sendMessage("§bWhitelist: " + String.join(", ", whitelistService.getPlayers()));
            case "reload" -> {
                whitelistService.load();
                sender.sendMessage("§aCustom whitelist перезагружен.");
            }
            default -> sender.sendMessage("§cНеизвестная подкоманда.");
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("add", "remove", "list", "reload");
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("add"))) {
            return new ArrayList<>(whitelistService.getPlayers());
        }

        return List.of();
    }
}
