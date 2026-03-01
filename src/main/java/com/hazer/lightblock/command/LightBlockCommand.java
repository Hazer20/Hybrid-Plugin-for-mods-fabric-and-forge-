package com.hazer.lightblock.command;

import com.hazer.lightblock.gui.LightBlockGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

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

        if (args.length > 0) {
            player.sendMessage(Component.text("Уровни удалены. Доступен только Световой блок I.", NamedTextColor.YELLOW));
        }

        gui.open(player, 1);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}
