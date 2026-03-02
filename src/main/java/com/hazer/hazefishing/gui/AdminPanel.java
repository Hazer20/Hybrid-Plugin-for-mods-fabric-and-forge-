package com.hazer.hazefishing.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public final class AdminPanel {
    public static final String TITLE = "HazerFishing Admin Panel";

    public void open(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 27, TITLE);
        inventory.setItem(10, named(Material.NETHER_STAR, "§dСоздать NFT"));
        inventory.setItem(11, named(Material.BELL, "§eЗапустить событие"));
        inventory.setItem(12, named(Material.DRAGON_HEAD, "§5Спавн босса"));
        inventory.setItem(13, named(Material.COD, "§bВыдать рыбу"));
        inventory.setItem(14, named(Material.COMPARATOR, "§6Изменить шансы"));
        inventory.setItem(15, named(Material.REDSTONE_TORCH, "§cВключить debug"));
        inventory.setItem(16, named(Material.REPEATER, "§aСимулятор"));
        inventory.setItem(22, named(Material.BOOK, "§fУправление реестром"));
        player.openInventory(inventory);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.2f);
    }

    private ItemStack named(Material material, String name) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(List.of("§7Подтверждение требуется"));
        stack.setItemMeta(meta);
        return stack;
    }
}
