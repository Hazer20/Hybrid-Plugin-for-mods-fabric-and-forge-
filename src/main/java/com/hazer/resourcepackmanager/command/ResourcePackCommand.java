package com.hazer.resourcepackmanager.command;

import com.hazer.resourcepackmanager.manager.ResourcePackManagerService;
import com.hazer.resourcepackmanager.model.PackAvailability;
import com.hazer.resourcepackmanager.model.PackInfo;
import com.hazer.resourcepackmanager.service.PlayerPromptService;
import com.hazer.resourcepackmanager.util.PluginLogger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Handles /resourcepack command and subcommands.
 * <p>
 * Supported subcommands:
 * <ul>
 *     <li>reload</li>
 *     <li>info</li>
 *     <li>accept</li>
 *     <li>decline</li>
 *     <li>status</li>
 *     <li>list</li>
 *     <li>help</li>
 * </ul>
 */
public final class ResourcePackCommand implements CommandExecutor, TabCompleter {
    private final ResourcePackManagerService packManager;
    private final PlayerPromptService promptService;
    private final PluginLogger logger;

    /**
     * @param packManager manager service
     * @param promptService player prompt service
     * @param logger plugin logger
     */
    public ResourcePackCommand(final ResourcePackManagerService packManager,
                               final PlayerPromptService promptService,
                               final PluginLogger logger) {
        this.packManager = Objects.requireNonNull(packManager, "packManager");
        this.promptService = Objects.requireNonNull(promptService, "promptService");
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    /**
     * Executes command logic.
     */
    @Override
    public boolean onCommand(final CommandSender sender,
                             final Command command,
                             final String label,
                             final String[] args) {
        if (args.length == 0) {
            sendHelp(sender, label);
            return true;
        }

        final String sub = args[0].toLowerCase(Locale.ROOT);
        return switch (sub) {
            case "reload" -> handleReload(sender);
            case "info" -> handleInfo(sender);
            case "accept" -> handleAccept(sender);
            case "decline" -> handleDecline(sender);
            case "status" -> handleStatus(sender);
            case "list" -> handleList(sender);
            case "help" -> {
                sendHelp(sender, label);
                yield true;
            }
            default -> {
                sender.sendMessage(Component.text("Неизвестная подкоманда. Используйте /" + label + " help", NamedTextColor.RED));
                yield true;
            }
        };
    }

    private boolean handleReload(final CommandSender sender) {
        if (!sender.hasPermission("resourcepackmanager.admin")) {
            sender.sendMessage(Component.text("Недостаточно прав.", NamedTextColor.RED));
            return true;
        }

        final PackAvailability result = packManager.reload(true);
        promptService.announceActivePackChange();
        sender.sendMessage(Component.text("Ресурс-паки перечитаны. Состояние: " + result, NamedTextColor.GREEN));
        logger.info(sender.getName() + " выполнил /resourcepack reload, результат: " + result);
        return true;
    }

    private boolean handleInfo(final CommandSender sender) {
        if (!sender.hasPermission("resourcepackmanager.info")) {
            sender.sendMessage(Component.text("Недостаточно прав.", NamedTextColor.RED));
            return true;
        }

        final Optional<PackInfo> active = packManager.getActivePack();
        if (active.isEmpty()) {
            sender.sendMessage(Component.text("Активный ресурс-пак отсутствует.", NamedTextColor.YELLOW));
            return true;
        }

        sender.sendMessage(Component.text("=== Активный ресурс-пак ===", NamedTextColor.AQUA));
        for (String line : active.get().asDescription().split("\\n")) {
            sender.sendMessage(Component.text(line, NamedTextColor.GRAY));
        }
        return true;
    }

    private boolean handleAccept(final CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Эта команда доступна только игрокам.", NamedTextColor.RED));
            return true;
        }

        final boolean sent = promptService.acceptPack(player);
        if (!sent) {
            sender.sendMessage(Component.text("Не удалось отправить ресурс-пак. Проверьте /resourcepack info", NamedTextColor.RED));
        }
        return true;
    }

    private boolean handleDecline(final CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Эта команда доступна только игрокам.", NamedTextColor.RED));
            return true;
        }

        promptService.declinePack(player);
        return true;
    }

    private boolean handleStatus(final CommandSender sender) {
        final PackAvailability status = packManager.getAvailability();
        sender.sendMessage(Component.text("Состояние менеджера: " + status, NamedTextColor.YELLOW));
        packManager.getActivePack()
                .map(PackInfo::fileName)
                .ifPresentOrElse(
                        name -> sender.sendMessage(Component.text("Активный: " + name, NamedTextColor.GREEN)),
                        () -> sender.sendMessage(Component.text("Активный: <none>", NamedTextColor.RED))
                );
        return true;
    }

    private boolean handleList(final CommandSender sender) {
        final List<PackInfo> packs = packManager.listAllPacks();
        if (packs.isEmpty()) {
            sender.sendMessage(Component.text("Пакеты не найдены.", NamedTextColor.YELLOW));
            return true;
        }

        sender.sendMessage(Component.text("Найдено паков: " + packs.size(), NamedTextColor.AQUA));
        packs.stream()
                .sorted(Comparator.comparing(PackInfo::lastModified).reversed())
                .limit(15)
                .forEach(info -> sender.sendMessage(Component.text(
                        "- " + info.fileName() + " | " + info.formattedSize() + " | " + info.formattedLastModified(),
                        NamedTextColor.GRAY)));
        return true;
    }

    private void sendHelp(final CommandSender sender, final String label) {
        sender.sendMessage(Component.text("=== ResourcePackManager команды ===", NamedTextColor.AQUA));
        sender.sendMessage(Component.text("/" + label + " reload - перечитать resourcepacks", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("/" + label + " info - информация об активном паке", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("/" + label + " list - список найденных паков", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("/" + label + " status - текущее состояние", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("/" + label + " accept - принять загрузку", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("/" + label + " decline - отказаться", NamedTextColor.GRAY));
    }

    /**
     * Tab completion for all subcommands.
     */
    @Override
    public List<String> onTabComplete(final CommandSender sender,
                                      final Command command,
                                      final String alias,
                                      final String[] args) {
        if (args.length == 1) {
            final List<String> all = List.of("reload", "info", "accept", "decline", "status", "list", "help");
            final String prefix = args[0].toLowerCase(Locale.ROOT);
            return all.stream()
                    .filter(name -> name.startsWith(prefix))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
