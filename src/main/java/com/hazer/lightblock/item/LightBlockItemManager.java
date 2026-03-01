package com.hazer.lightblock.item;

import com.hazer.lightblock.LightBlockPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class LightBlockItemManager {

    private final NamespacedKey levelKey;

    public LightBlockItemManager(LightBlockPlugin plugin) {
        this.levelKey = new NamespacedKey(plugin, "light_level");
    }

    public ItemStack createLightBlockItem(int level, int amount) {
        ItemStack item = new ItemStack(Material.LIGHT, Math.max(1, amount));
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("Световой блок I", NamedTextColor.GOLD));
        meta.lore(List.of(
                Component.text("Невидимый источник света", NamedTextColor.GRAY),
                Component.text("Уровень света: 1", NamedTextColor.YELLOW)
        ));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        meta.getPersistentDataContainer().set(levelKey, PersistentDataType.INTEGER, 1);

        item.setItemMeta(meta);
        return item;
    }

    public boolean isLightBlockItem(ItemStack item) {
        if (item == null || item.getType() != Material.LIGHT || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().has(levelKey, PersistentDataType.INTEGER);
    }

    public int getLightLevel(ItemStack item) {
        return isLightBlockItem(item) ? 1 : -1;
    }

    public String toRoman(int number) {
        return "I";
    }
}
