package com.hazer.lightblock.command;

import com.hazer.lightblock.gui.LightBlockGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LightBlockCommand implements CommandExecutor, TabCompleter {

    private final LightBlockGUI gui;

    public LightBlockCommand(LightBlockGUI gui) {
        this.gui = gui;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Только игрок может использовать эту команду.");
            return true;
        }

        if (!player.hasPermission("lightblock.use")) {
            player.sendMessage(Component.text("У вас нет прав на эту команду.", NamedTextColor.RED));
            return true;
        }

        int level = 1;
        if (args.length > 0) {
            try {
                level = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
                player.sendMessage(Component.text("Неверный уровень. Используйте числа от 1 до 15.", NamedTextColor.RED));
                return true;
            }
        }

        if (level < 1 || level > 15) {
            player.sendMessage(Component.text("Уровень должен быть от 1 до 15.", NamedTextColor.RED));
            return true;
        }

        gui.open(player, level);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> levels = new ArrayList<>();
            for (int i = 1; i <= 15; i++) {
                String level = String.valueOf(i);
                if (level.startsWith(args[0])) {
                    levels.add(level);
                }
            }
            return levels;
        }
        return Collections.emptyList();
    }
}
