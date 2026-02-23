package dev.sanguine.bridge;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public final class SanguineMenuListener implements Listener {

    private final SanguineBridgePlugin plugin;

    public SanguineMenuListener(SanguineBridgePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (event.getView().getTitle() == null || !event.getView().getTitle().equals("Sanguine Bridge Control")) {
            return;
        }

        event.setCancelled(true);
        if (event.getCurrentItem() == null) {
            return;
        }

        Material type = event.getCurrentItem().getType();
        plugin.handleMenuClick(player, type);
    }
}
