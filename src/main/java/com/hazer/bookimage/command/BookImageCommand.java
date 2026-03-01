package com.hazer.bookimage.command;

import com.hazer.bookimage.BookImagePlugin;
import com.hazer.bookimage.service.BookImageService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class BookImageCommand implements CommandExecutor, TabCompleter {

    private final BookImageService bookImageService;

    public BookImageCommand(BookImagePlugin plugin, BookImageService bookImageService) {
        this.bookImageService = bookImageService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Только игрок может использовать эту команду.");
            return true;
        }

        if (!player.hasPermission("bookimage.use")) {
            player.sendMessage("§cУ вас нет прав на использование /bookimage.");
            return true;
        }

        bookImageService.convertBookInHandAsync(player, null);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}
