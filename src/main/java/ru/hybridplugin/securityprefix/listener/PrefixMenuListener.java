package ru.hybridplugin.securityprefix.listener;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import ru.hybridplugin.securityprefix.service.PrefixService;

public class PrefixMenuListener implements Listener {

    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();
    private final PrefixService prefixService;

    public PrefixMenuListener(PrefixService prefixService) {
        this.prefixService = prefixService;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        prefixService.updateDisplayName(event.getPlayer());
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getView().title() == null) {
            return;
        }

        String title = PLAIN.serialize(event.getView().title());
        if (!title.equals("Префиксы")) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player) || event.getCurrentItem() == null) {
            return;
        }

        Material type = event.getCurrentItem().getType();
        if (type == Material.NAME_TAG) {
            player.closeInventory();
            player.sendMessage("§eВведите обычный префикс: /prefix set <текст>");
        } else if (type == Material.EMERALD) {
            player.closeInventory();
            player.sendMessage("§eВведите премиум префикс: /prefix premium <текст>");
        } else if (type == Material.BARRIER) {
            prefixService.clearPrefix(player);
            player.sendMessage("§aПрефикс сброшен.");
            player.closeInventory();
        }
    }
}
