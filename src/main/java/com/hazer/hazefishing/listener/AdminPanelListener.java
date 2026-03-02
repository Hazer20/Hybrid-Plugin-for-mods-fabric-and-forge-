package com.hazer.hazefishing.listener;

import com.hazer.hazefishing.HazerFishingPlugin;
import com.hazer.hazefishing.gui.AdminPanel;
import com.hazer.hazefishing.model.NFTFish;
import com.hazer.hazefishing.model.NFTRod;
import com.hazer.hazefishing.model.Rarity;
import com.hazer.hazefishing.model.RodTier;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public final class AdminPanelListener implements Listener {
    private final HazerFishingPlugin plugin;

    public AdminPanelListener(HazerFishingPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!AdminPanel.TITLE.equals(event.getView().getTitle())) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null) return;

        Material type = event.getCurrentItem().getType();
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 1.8f);

        switch (type) {
            case NETHER_STAR -> {
                NFTRod rod = plugin.getNftRodManager().mint(player, RodTier.NFT_ROD);
                player.getInventory().addItem(plugin.getNftRodManager().toItem(rod));
                player.sendMessage("§aNFT rod minted from panel: " + rod.serial());
            }
            case BELL -> {
                NFTFish fish = plugin.getNftFishManager().generate(player, Rarity.NFT);
                player.getInventory().addItem(plugin.getNftFishManager().toItem(fish));
                plugin.getEventManager().broadcastNftCatch(player, fish);
            }
            case DRAGON_HEAD -> {
                player.getWorld().strikeLightningEffect(player.getLocation());
                player.sendMessage("§5Boss event test started.");
            }
            case COD -> {
                NFTFish fish = plugin.getNftFishManager().generate(player, Rarity.LEGENDARY);
                player.getInventory().addItem(plugin.getNftFishManager().toItem(fish));
                player.sendMessage("§bLegendary fish issued to inventory.");
            }
            case COMPARATOR -> player.sendMessage("§6Chance editor: use /hf chance ... (WIP)");
            case REDSTONE_TORCH -> {
                boolean state = plugin.getAdminCommandManager().toggleDebug();
                player.sendMessage(state ? "§aDebug enabled" : "§cDebug disabled");
            }
            case REPEATER -> plugin.getAdminCommandManager().runQuickSimulation(player, 1000);
            case BOOK -> player.sendMessage("§fRegistry entries: " + plugin.getNftFishManager().topRarest(5).size());
            default -> player.sendMessage("§7Действие подтверждено: " + type);
        }
    }
}
