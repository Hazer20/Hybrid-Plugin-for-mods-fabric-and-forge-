package com.hazer.march8;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * /march8 command handler.
 */
public final class March8Command implements CommandExecutor, TabCompleter {

    private final March8Plugin plugin;
    private final ConfigManager configManager;
    private final GirlManager girlManager;
    private final GiftManager giftManager;
    private final ParticleManager particleManager;
    private final FireworkManager fireworkManager;

    public March8Command(@NotNull March8Plugin plugin,
                         @NotNull ConfigManager configManager,
                         @NotNull GirlManager girlManager,
                         @NotNull GiftManager giftManager,
                         @NotNull ParticleManager particleManager,
                         @NotNull FireworkManager fireworkManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.girlManager = girlManager;
        this.giftManager = giftManager;
        this.particleManager = particleManager;
        this.fireworkManager = fireworkManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {
        if (!sender.hasPermission("march8.admin")) {
            sender.sendMessage(configManager.getMessage("no-permission"));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(configManager.getMessage("usage"));
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        return switch (sub) {
            case "add" -> handleAdd(sender, args);
            case "remove" -> handleRemove(sender, args);
            case "list" -> handleList(sender);
            case "start" -> handleStart(sender);
            case "reload" -> handleReload(sender);
            default -> {
                sender.sendMessage(configManager.getMessage("usage"));
                yield true;
            }
        };
    }

    private boolean handleAdd(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length < 2) {
            sender.sendMessage(configManager.getMessage("usage"));
            return true;
        }
        String name = args[1];
        boolean added = girlManager.addGirl(name);
        if (added) {
            sender.sendMessage(configManager.getMessage("added", "player", name));
        } else {
            sender.sendMessage(configManager.getMessage("already-added", "player", name));
        }
        return true;
    }

    private boolean handleRemove(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length < 2) {
            sender.sendMessage(configManager.getMessage("usage"));
            return true;
        }
        String name = args[1];
        boolean removed = girlManager.removeGirl(name);
        if (removed) {
            sender.sendMessage(configManager.getMessage("removed", "player", name));
        } else {
            sender.sendMessage(configManager.getMessage("not-found", "player", name));
        }
        return true;
    }

    private boolean handleList(@NotNull CommandSender sender) {
        List<String> girls = girlManager.getGirls();
        sender.sendMessage(configManager.getMessage("list-header"));
        if (girls.isEmpty()) {
            sender.sendMessage(configManager.getMessage("list-empty"));
            return true;
        }

        for (String name : girls) {
            sender.sendMessage(configManager.getMessage("list-item", "player", name));
        }
        return true;
    }

    private boolean handleStart(@NotNull CommandSender sender) {
        List<Player> recipients = giftManager.congratulateOnlineGirls(girlManager, particleManager, fireworkManager);
        sender.sendMessage(configManager.getMessage("start-done"));
        sender.sendMessage(configManager.mm("<gray>Получателей онлайн: <white>" + recipients.size() + "</white></gray>"));
        return true;
    }

    private boolean handleReload(@NotNull CommandSender sender) {
        plugin.reloadPlugin();
        sender.sendMessage(configManager.getMessage("reload-done"));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                 @NotNull Command command,
                                                 @NotNull String alias,
                                                 @NotNull String[] args) {
        if (!sender.hasPermission("march8.admin")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            List<String> options = List.of("add", "remove", "list", "start", "reload");
            return partial(options, args[0]);
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase(Locale.ROOT);
            if (sub.equals("add")) {
                List<String> online = Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
                return partial(online, args[1]);
            }
            if (sub.equals("remove")) {
                return partial(girlManager.getGirls(), args[1]);
            }
        }

        return Collections.emptyList();
    }

    private List<String> partial(List<String> candidates, String input) {
        String lower = input.toLowerCase(Locale.ROOT);
        List<String> out = new ArrayList<>();
        for (String candidate : candidates) {
            if (candidate.toLowerCase(Locale.ROOT).startsWith(lower)) {
                out.add(candidate);
            }
        }
        return out;
    }
}
