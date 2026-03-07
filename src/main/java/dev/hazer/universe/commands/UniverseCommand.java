package dev.hazer.universe.commands;

import dev.hazer.universe.FracturedUniverse;
import dev.hazer.universe.effects.PlayerInstabilityManager;
import dev.hazer.universe.lore.LoreManager;
import dev.hazer.universe.portals.FissureManager;
import dev.hazer.universe.systems.EventPhase;
import dev.hazer.universe.systems.EventPhaseManager;
import dev.hazer.universe.systems.EventScheduler;
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

import java.util.List;

public class UniverseCommand implements CommandExecutor, TabCompleter {
    private final FracturedUniverse plugin;
    private final EventPhaseManager phaseManager;
    private final EventScheduler eventScheduler;
    private final FissureManager fissureManager;
    private final PlayerInstabilityManager instabilityManager;
    private final WorldManager worldManager;
    private final LoreManager loreManager;

    public UniverseCommand(FracturedUniverse plugin,
                           EventPhaseManager phaseManager,
                           EventScheduler eventScheduler,
                           FissureManager fissureManager,
                           PlayerInstabilityManager instabilityManager,
                           WorldManager worldManager,
                           LoreManager loreManager) {
        this.plugin = plugin;
        this.phaseManager = phaseManager;
        this.eventScheduler = eventScheduler;
        this.fissureManager = fissureManager;
        this.instabilityManager = instabilityManager;
        this.worldManager = worldManager;
        this.loreManager = loreManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.GRAY + "/universe <start|stop|phase|starfall|fissure|debug|lore>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "start" -> {
                phaseManager.startEvent();
                eventScheduler.scheduleStarfall();
                sender.sendMessage(ChatColor.GREEN + "Fractured Universe started.");
            }
            case "stop" -> {
                phaseManager.stopEvent();
                sender.sendMessage(ChatColor.YELLOW + "Fractured Universe stopped.");
            }
            case "phase" -> handlePhase(sender, args);
            case "starfall" -> {
                fissureManager.triggerStarfall();
                sender.sendMessage(ChatColor.GOLD + "Starfall triggered.");
            }
            case "fissure" -> handleFissure(sender, args);
            case "debug" -> {
                sender.sendMessage(ChatColor.AQUA + "Current phase: " + phaseManager.getPhase());
                sender.sendMessage(ChatColor.AQUA + "Worlds loaded: " + Bukkit.getWorlds().size());
            }
            case "lore" -> handleLore(sender, args);
            default -> sender.sendMessage(ChatColor.RED + "Unknown subcommand.");
        }

        return true;
    }

    private void handlePhase(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.GRAY + "Usage: /universe phase <0-4>");
            return;
        }
        int value;
        try {
            value = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            sender.sendMessage(ChatColor.RED + "Phase must be number 0-4");
            return;
        }
        if (value < 0 || value > 4) {
            sender.sendMessage(ChatColor.RED + "Phase must be between 0 and 4");
            return;
        }
        phaseManager.setPhase(EventPhase.values()[value]);
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "Phase changed to " + phaseManager.getPhase());
    }

    private void handleFissure(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can spawn fissures at location.");
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.GRAY + "Usage: /universe fissure <small|great|living>");
            return;
        }

        Location location = player.getLocation();
        switch (args[1].toLowerCase()) {
            case "small" -> fissureManager.spawnSmallFissure(location);
            case "great" -> fissureManager.spawnGreatFissure(location);
            case "living" -> fissureManager.spawnLivingFissure(location);
            default -> {
                sender.sendMessage(ChatColor.RED + "Unknown fissure type.");
                return;
            }
        }
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "Fissure created.");
    }

    private void handleLore(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use lore actions.");
            return;
        }

        if (args.length >= 2 && args[1].equalsIgnoreCase("add")) {
            if (args.length < 3) {
                sender.sendMessage(ChatColor.GRAY + "Usage: /universe lore add <text>");
                return;
            }
            String entry = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));
            loreManager.addEntry(player, entry);
            sender.sendMessage(ChatColor.GREEN + "Lore entry saved.");
            return;
        }

        if (args.length >= 2 && args[1].equalsIgnoreCase("instability")) {
            instabilityManager.applyInstability(player);
            sender.sendMessage(ChatColor.YELLOW + "Instability forced for testing.");
            return;
        }

        List<String> entries = loreManager.getEntries(player);
        sender.sendMessage(ChatColor.DARK_PURPLE + "=== Your Fractured Lore ===");
        if (entries.isEmpty()) {
            sender.sendMessage(ChatColor.GRAY + "No entries yet. Use /universe lore add <text>");
            return;
        }

        for (String entry : entries) {
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "- " + entry);
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("start", "stop", "phase", "starfall", "fissure", "debug", "lore");
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("fissure")) {
            return List.of("small", "great", "living");
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("lore")) {
            return List.of("add", "instability");
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("phase")) {
            return List.of("0", "1", "2", "3", "4");
        }

        return List.of();
    }
}
