package com.hazer.hazefishing.listener;

import com.hazer.hazefishing.gui.AdminPanel;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public final class AdminPanelListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!AdminPanel.TITLE.equals(event.getView().getTitle())) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null) return;
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 1.8f);
        player.sendMessage("§7Action confirmed: " + event.getCurrentItem().getType());
    }
}
