package dev.sanguine.engine.runtime;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class RuntimeItemBridgeListener implements Listener {
    private final NamespacedKey legacyFlag;

    public RuntimeItemBridgeListener(JavaPlugin plugin) {
        this.legacyFlag = new NamespacedKey(plugin, "legacy_sanguine_item");
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item != null) {
            bridgeLegacyItem(item);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            ItemStack hand = player.getInventory().getItemInMainHand();
            bridgeLegacyItem(hand);
        }
    }

    @EventHandler
    public void onCraft(PrepareItemCraftEvent event) {
        if (event.getInventory().getResult() != null) {
            bridgeLegacyItem(event.getInventory().getResult());
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();
        if (item != null) {
            bridgeLegacyItem(item);
        }
    }

    private void bridgeLegacyItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return;
        }
        ItemMeta meta = item.getItemMeta();

        if (!meta.getPersistentDataContainer().has(legacyFlag, PersistentDataType.BYTE)) {
            return;
        }

        if (meta.hasCustomModelData()) {
            int customModelData = meta.getCustomModelData();
            meta.setCustomModelData(customModelData);
        }

        item.setItemMeta(meta);
    }
}
