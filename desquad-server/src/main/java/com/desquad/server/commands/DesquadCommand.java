package com.desquad.server.commands;

import com.desquad.api.DESquadAPI;
import com.desquad.engine.tick.DesquadTickLoop;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Главная команда ядра DESquadCore. */
public final class DesquadCommand implements CommandExecutor {
    private final DesquadTickLoop tickLoop;

    public DesquadCommand(DesquadTickLoop tickLoop) {
        this.tickLoop = tickLoop;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("info")) {
            sender.sendMessage("§6DESquadCore §f1.21.8 Patch 1.0.3");
            sender.sendMessage("§7Items: " + DESquadAPI.getItemRegistry().allItems().size());
            sender.sendMessage("§7NPC: " + DESquadAPI.getNPCManager().all().size());
            sender.sendMessage("§7Modules: " + DESquadAPI.getModuleManager().enabledModules().size());
            sender.sendMessage("§7Perf pools: " + DESquadAPI.getPerformanceService().poolSizes());
            sender.sendMessage("§7Tick subsystems: " + tickLoop.subsystemCount() + ", current tick: " + tickLoop.currentTick());
            return true;
        }
        if (args[0].equalsIgnoreCase("reload")) {
            DESquadAPI.getModuleManager().reload();
            sender.sendMessage("§aDESquadCore modules reloaded.");
            return true;
        }
        if (args[0].equalsIgnoreCase("item") && args.length >= 2) {
            if (args[1].equalsIgnoreCase("list")) {
                sender.sendMessage("§eRegistered items:");
                DESquadAPI.getItemRegistry().allItems().forEach(i -> sender.sendMessage(" - " + i.id()));
                return true;
            }
            if (args[1].equalsIgnoreCase("give") && args.length >= 4) {
                var target = Bukkit.getPlayerExact(args[2]);
                if (target == null) {
                    sender.sendMessage("§cPlayer not found");
                    return true;
                }
                var item = DESquadAPI.getItemRegistry().getItem(args[3]);
                if (item.isEmpty()) {
                    sender.sendMessage("§cUnknown item id");
                    return true;
                }
                target.getInventory().addItem(item.get().createStack());
                sender.sendMessage("§aGiven " + args[3] + " to " + target.getName());
                return true;
            }
        }
        if (args[0].equalsIgnoreCase("gui") && args.length >= 3 && args[1].equalsIgnoreCase("open") && sender instanceof Player player) {
            DESquadAPI.getGuiService().create(args[2], "DESquad GUI: " + args[2], 27).open(player);
            return true;
        }
        if (args[0].equalsIgnoreCase("module") && args.length >= 2) {
            if (args[1].equalsIgnoreCase("list")) {
                sender.sendMessage("§eEnabled modules: " + DESquadAPI.getModuleManager().enabledModules().stream().sorted().collect(Collectors.joining(", ")));
                return true;
            }
            if (args.length >= 3 && args[1].equalsIgnoreCase("enable")) {
                sender.sendMessage(DESquadAPI.getModuleManager().enable(args[2]) ? "§aModule enabled" : "§cUnable to enable module");
                return true;
            }
            if (args.length >= 3 && args[1].equalsIgnoreCase("disable")) {
                sender.sendMessage(DESquadAPI.getModuleManager().disable(args[2]) ? "§aModule disabled" : "§cUnable to disable module");
                return true;
            }
        }
        if (args[0].equalsIgnoreCase("perf") && args.length >= 2) {
            if (args[1].equalsIgnoreCase("info")) {
                sender.sendMessage("§bPools: " + DESquadAPI.getPerformanceService().poolSizes());
                return true;
            }
            if (args[1].equalsIgnoreCase("stress") && args.length >= 3) {
                int count;
                try {
                    count = Integer.parseInt(args[2]);
                } catch (NumberFormatException ex) {
                    sender.sendMessage("§cCount must be numeric");
                    return true;
                }
                for (int i = 0; i < count; i++) {
                    DESquadAPI.getPerformanceService().submit("ai", () -> Math.sqrt(System.nanoTime()));
                }
                sender.sendMessage("§aScheduled " + count + " async tasks");
                return true;
            }
        }
        sender.sendMessage("§cUsage: /desquad info|reload|item list|item give <player> <item_id>|gui open <id>|module list|module enable <id>|module disable <id>|perf info|perf stress <count>");
        return true;
    }
}
