package ru.desquad.hybrid.util;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemBuilder {
    private final ItemStack item;

    public ItemBuilder(Material material) {
        this.item = new ItemStack(material);
    }

    public ItemBuilder amount(int amount) {
        this.item.setAmount(amount);
        return this;
    }

    public ItemBuilder name(String name) {
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Message.component(name));
        item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder lore(List<String> lore) {
        ItemMeta meta = item.getItemMeta();
        List<Component> lines = new ArrayList<>();
        for (String line : lore) {
            lines.add(Message.component(line));
        }
        meta.lore(lines);
        item.setItemMeta(meta);
        return this;
    }

    public ItemStack build() {
        return item;
    }
}
