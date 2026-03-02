package ru.desquad.hybrid.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import ru.desquad.hybrid.DESHybridPlugin;
import ru.desquad.hybrid.schematic.ExternalSchematicRepository;

public class SchematicCommand implements CommandExecutor {

    private final DESHybridPlugin plugin;
    private final ExternalSchematicRepository repo;

    public SchematicCommand(DESHybridPlugin plugin, ExternalSchematicRepository repo) {
        this.plugin = plugin;
        this.repo = repo;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("desschematic.use") && !sender.isOp()) {
            sender.sendMessage("§cНет прав: desschematic.use");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§e/desschematic list");
            sender.sendMessage("§e/desschematic addurl <имя> <url_до_.schematic/.litematic>");
            sender.sendMessage("§eПапка схем: " + repo.getDir());
            return true;
        }

        if (args[0].equalsIgnoreCase("list")) {
            sender.sendMessage("§6Схемы в папке:");
            repo.listKeys().forEach(k -> sender.sendMessage(" §8• §f" + k));
            return true;
        }

        if (args[0].equalsIgnoreCase("addurl")) {
            if (args.length < 3) {
                sender.sendMessage("§cИспользование: /desschematic addurl <имя> <url>");
                return true;
            }
            String name = args[1];
            String url = args[2];
            sender.sendMessage("§eЗагрузка схемы... " + name);

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    repo.downloadFromUrl(name, url);
                    sender.sendMessage("§aСхема загружена: ext:" + name.toLowerCase());
                } catch (Exception e) {
                    sender.sendMessage("§cОшибка загрузки схемы: " + e.getMessage());
                }
            });
            return true;
        }

        sender.sendMessage("§cНеизвестная подкоманда.");
        return true;
    }
}
