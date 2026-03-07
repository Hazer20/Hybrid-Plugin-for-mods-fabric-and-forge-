package dev.hazer.universe.commands;

import dev.hazer.universe.FracturedUniverse;
import dev.hazer.universe.bosses.FinalBossManager;
import dev.hazer.universe.effects.PlayerInstabilityManager;
import dev.hazer.universe.lore.LoreManager;
import dev.hazer.universe.portals.FissureManager;
import dev.hazer.universe.portals.PortalRitualManager;
import dev.hazer.universe.systems.*;
import dev.hazer.universe.worlds.WorldManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class UniverseCommand implements CommandExecutor, TabCompleter {
    private final FracturedUniverse plugin;
    private final EventPhaseManager phaseManager;
    private final EventScheduler eventScheduler;
    private final FissureManager fissureManager;
    private final PlayerInstabilityManager instabilityManager;
    private final LoreManager loreManager;
    private final UniverseStabilityManager stabilityManager;
    private final ArgEventManager argEventManager;
    private final PortalRitualManager portalRitualManager;
    private final FinalBossManager finalBossManager;
    private final ModelRegistryService modelRegistryService;

    public UniverseCommand(FracturedUniverse plugin,
                           EventPhaseManager phaseManager,
                           EventScheduler eventScheduler,
                           FissureManager fissureManager,
                           PlayerInstabilityManager instabilityManager,
                           WorldManager worldManager,
                           LoreManager loreManager,
                           UniverseStabilityManager stabilityManager,
                           ArgEventManager argEventManager,
                           PortalRitualManager portalRitualManager,
                           FinalBossManager finalBossManager,
                           ModelRegistryService modelRegistryService) {
        this.plugin = plugin;
        this.phaseManager = phaseManager;
        this.eventScheduler = eventScheduler;
        this.fissureManager = fissureManager;
        this.instabilityManager = instabilityManager;
        this.loreManager = loreManager;
        this.stabilityManager = stabilityManager;
        this.argEventManager = argEventManager;
        this.portalRitualManager = portalRitualManager;
        this.finalBossManager = finalBossManager;
        this.modelRegistryService = modelRegistryService;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.GRAY + "/universe <start|stop|phase|starfall|fissure|debug|lore|portals|arg|final>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "start" -> {
                stabilityManager.start();
                phaseManager.startEvent();
                eventScheduler.scheduleStarfall();
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "function fractured_universe:sky/enable");
                sender.sendMessage(ChatColor.GREEN + "Событие запущено.");
            }
            case "stop" -> {
                argEventManager.stopEvent();
                phaseManager.stopEvent();
                stabilityManager.stop();
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "function fractured_universe:sky/disable");
                sender.sendMessage(ChatColor.YELLOW + "Событие остановлено.");
            }
            case "phase" -> handlePhase(sender, args);
            case "starfall" -> {
                fissureManager.triggerStarfall();
                sender.sendMessage(ChatColor.GOLD + "Падение звезды принудительно запущено.");
            }
            case "fissure" -> handleFissure(sender, args);
            case "debug" -> {
                sender.sendMessage(ChatColor.AQUA + "Текущая фаза: " + phaseManager.getPhase());
                sender.sendMessage(ChatColor.AQUA + "Стабильность: " + stabilityManager.getStability() + "%");
                sender.sendMessage(ChatColor.AQUA + "Моделей обнаружено: " + modelRegistryService.getDetectedModels().size());
                sender.sendMessage(ChatColor.AQUA + "Миров загружено: " + Bukkit.getWorlds().size());
            }
            case "lore" -> handleLore(sender, args);
            case "portals" -> handlePortals(sender, args);
            case "arg" -> handleArg(sender, args);
            case "final" -> handleFinal(sender);
            default -> sender.sendMessage(ChatColor.RED + "Неизвестная подкоманда.");
        }
        return true;
    }

    private void handlePhase(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.GRAY + "Использование: /universe phase <0-4>");
            return;
        }
        int value;
        try {
            value = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            sender.sendMessage(ChatColor.RED + "Фаза должна быть числом 0-4");
            return;
        }
        if (value < 0 || value > 4) {
            sender.sendMessage(ChatColor.RED + "Фаза должна быть между 0 и 4");
            return;
        }
        phaseManager.setPhase(EventPhase.values()[value]);
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "Фаза изменена: " + phaseManager.getPhase());
    }

    private void handleFissure(CommandSender sender, String[] args) {
        if (args.length >= 2 && args[1].equalsIgnoreCase("disable")) {
            plugin.getConfig().set("разломы.включены", false);
            plugin.saveConfig();
            sender.sendMessage(ChatColor.YELLOW + "Генерация разломов и аномалий отключена.");
            return;
        }
        if (args.length >= 2 && args[1].equalsIgnoreCase("enable")) {
            plugin.getConfig().set("разломы.включены", true);
            plugin.saveConfig();
            sender.sendMessage(ChatColor.GREEN + "Генерация разломов и аномалий включена.");
            return;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Только игрок может создать разлом в своей точке.");
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(ChatColor.GRAY + "Использование: /universe fissure <small|great|living|disable|repair|enable>");
            return;
        }

        Location location = player.getLocation();
        if (args[1].equalsIgnoreCase("repair")) {
            boolean repaired = fissureManager.repairNearestFissure(location, 8.0);
            sender.sendMessage(repaired ? ChatColor.GREEN + "Разлом стабилизирован и закрыт." : ChatColor.RED + "Рядом нет активного разлома для починки.");
            return;
        }

        switch (args[1].toLowerCase()) {
            case "small" -> fissureManager.spawnSmallFissure(location);
            case "great" -> fissureManager.spawnGreatFissure(location);
            case "living" -> fissureManager.spawnLivingFissure(location);
            default -> sender.sendMessage(ChatColor.RED + "Неизвестный тип разлома.");
        }
    }

    private void handleLore(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Команда доступна только игроку.");
            return;
        }
        if (args.length >= 2 && args[1].equalsIgnoreCase("add")) {
            if (args.length < 3) {
                sender.sendMessage(ChatColor.GRAY + "Использование: /universe lore add <текст>");
                return;
            }
            loreManager.addEntry(player, String.join(" ", Arrays.copyOfRange(args, 2, args.length)));
            sender.sendMessage(ChatColor.GREEN + "Лор-запись сохранена.");
            return;
        }

        if (args.length >= 2 && args[1].equalsIgnoreCase("instability")) {
            instabilityManager.applyInstability(player);
            sender.sendMessage(ChatColor.YELLOW + "Нестабильность добавлена вручную.");
            return;
        }

        sender.sendMessage(ChatColor.DARK_PURPLE + "=== Ваши записи ARG ===");
        for (String line : loreManager.getEntries(player)) {
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "- " + line);
        }
    }

    private void handlePortals(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.GRAY + "Использование: /universe portals <destroy|wait>");
            return;
        }
        switch (args[1].toLowerCase()) {
            case "destroy" -> {
                portalRitualManager.destroyAllPortals();
                sender.sendMessage(ChatColor.RED + "Все порталы разрушены.");
            }
            case "wait" -> {
                portalRitualManager.setWaitingNetherActivation(true);
                sender.sendMessage(ChatColor.LIGHT_PURPLE + "Ожидается активация Nether-портала.");
            }
            default -> sender.sendMessage(ChatColor.RED + "Неизвестный режим для portals.");
        }
    }

    private void handleArg(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.GRAY + "Использование: /universe arg <start|random|stop|list> [номер]");
            return;
        }

        switch (args[1].toLowerCase()) {
            case "start" -> {
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Укажите номер события: /universe arg start <номер>");
                    return;
                }
                try {
                    int id = Integer.parseInt(args[2]);
                    argEventManager.startEvent(id);
                    sender.sendMessage(ChatColor.GREEN + "Событие ARG запущено: " + id);
                } catch (NumberFormatException ex) {
                    sender.sendMessage(ChatColor.RED + "Номер события должен быть числом.");
                }
            }
            case "random" -> {
                argEventManager.startRandom();
                sender.sendMessage(ChatColor.GREEN + "Запущено случайное ARG событие.");
            }
            case "stop" -> {
                argEventManager.stopEvent();
                sender.sendMessage(ChatColor.YELLOW + "ARG событие остановлено.");
            }
            case "list" -> {
                sender.sendMessage(ChatColor.DARK_PURPLE + "=== Список ARG событий (30) ===");
                for (Map.Entry<Integer, String> entry : argEventManager.getEvents().entrySet()) {
                    sender.sendMessage(ChatColor.LIGHT_PURPLE + String.valueOf(entry.getKey()) + " — " + entry.getValue());
                }
            }
            default -> sender.sendMessage(ChatColor.RED + "Неизвестная команда arg.");
        }
    }

    private void handleFinal(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Финального босса можно вызвать только в мире игрока.");
            return;
        }
        finalBossManager.spawnFinalBoss(player.getLocation().add(0, 2, 0));
        stabilityManager.setStability(0);
        sender.sendMessage(ChatColor.DARK_RED + "Финал сезона активирован.");
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("start", "stop", "phase", "starfall", "fissure", "debug", "lore", "portals", "arg", "final");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("fissure")) {
            return List.of("small", "great", "living", "disable", "enable", "repair");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("lore")) {
            return List.of("add", "instability");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("phase")) {
            return List.of("0", "1", "2", "3", "4");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("portals")) {
            return List.of("destroy", "wait");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("arg")) {
            return List.of("start", "random", "stop", "list");
        }
        return List.of();
    }
}
