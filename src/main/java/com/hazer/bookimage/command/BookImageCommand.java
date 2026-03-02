package com.hazer.bookimage.command;

import com.hazer.bookimage.service.BookImageService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BookImageCommand implements CommandExecutor, TabCompleter {

    private final BookImageService bookImageService;

    public BookImageCommand(BookImageService bookImageService) {
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

        if (args.length == 0) {
            // Legacy behavior: convert URL text in currently held book.
            bookImageService.convertBookInHandAsync(player, null);
            return true;
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("create")) {
            String url = args[1];
            bookImageService.giveImageBookFromUrlAsync(player, url);
            return true;
        }

        player.sendMessage("§eИспользование:");
        player.sendMessage("§7/bookimage §f- конвертировать URL в книге в руке");
        player.sendMessage("§7/bookimage create <url> §f- создать готовую книгу с картинкой (использует 1 книгу с пером)");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("create");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("create")) {
            return Collections.singletonList("https://example.com/image.png");
        }
        return new ArrayList<>();
    }
}
