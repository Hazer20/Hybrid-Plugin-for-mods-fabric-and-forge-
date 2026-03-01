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
        int safeLevel = Math.max(1, Math.min(15, level));
        ItemStack item = new ItemStack(Material.LIGHT, Math.max(1, amount));
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("Light Block " + toRoman(safeLevel), NamedTextColor.GOLD));
        meta.lore(List.of(
                Component.text("Invisible light source", NamedTextColor.GRAY),
                Component.text("Emits light level: " + safeLevel, NamedTextColor.YELLOW)
        ));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        meta.getPersistentDataContainer().set(levelKey, PersistentDataType.INTEGER, safeLevel);

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
        if (!isLightBlockItem(item)) {
            return -1;
        }
        Integer level = item.getItemMeta().getPersistentDataContainer().get(levelKey, PersistentDataType.INTEGER);
        return level == null ? -1 : Math.max(1, Math.min(15, level));
    }

    public String toRoman(int number) {
        return switch (number) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            case 6 -> "VI";
            case 7 -> "VII";
            case 8 -> "VIII";
            case 9 -> "IX";
            case 10 -> "X";
            case 11 -> "XI";
            case 12 -> "XII";
            case 13 -> "XIII";
            case 14 -> "XIV";
            case 15 -> "XV";
            default -> String.valueOf(number);
        };
    }
}
