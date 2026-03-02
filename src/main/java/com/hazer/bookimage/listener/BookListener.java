package com.hazer.bookimage.listener;

import com.hazer.bookimage.service.BookImageService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

/**
 * Handles edit/open events to keep book image content in sync for all viewers.
 */
public class BookListener implements Listener {

    private final BookImageService bookImageService;

    public BookListener(BookImageService bookImageService) {
        this.bookImageService = bookImageService;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBookEdit(PlayerEditBookEvent event) {
        BookMeta newMeta = event.getNewBookMeta();
        BookImageService.ConversionResult result = bookImageService.convertBookMeta(newMeta);
        event.setNewBookMeta(result.bookMeta());

        if (result.convertedPages() > 0) {
            event.getPlayer().sendMessage("§aURL автоматически преобразован в изображение в книге.");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBookOpen(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        Player player = event.getPlayer();
        if (item == null) {
            return;
        }

        if (item.getType() != Material.WRITTEN_BOOK && item.getType() != Material.WRITABLE_BOOK) {
            return;
        }

        if (!(item.getItemMeta() instanceof BookMeta meta)) {
            return;
        }

        BookImageService.ConversionResult result = bookImageService.convertBookMeta(meta);
        if (result.convertedPages() > 0) {
            item.setItemMeta(result.bookMeta());
            player.sendMessage("§aКнига обновлена: изображение(я) синхронизировано.");
        }
    }
}
