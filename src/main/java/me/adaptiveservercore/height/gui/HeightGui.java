package me.adaptiveservercore.height.gui;

import me.adaptiveservercore.AdaptiveServerCore;
import me.adaptiveservercore.height.HeightManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class HeightGui {

    public static final String TITLE = "§8Выбор роста";

    private final HeightManager heightManager;
    private final Map<UUID, Boolean> awaitingChatInput = new HashMap<>();

    public HeightGui(AdaptiveServerCore plugin, HeightManager heightManager) {
        this.heightManager = heightManager;
    }

    public void open(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 9, TITLE);
        inventory.setItem(1, createButton(Material.SMALL_AMETHYST_BUD, "§aМаленький", "§7Рост: §f0.5", 0.5D));
        inventory.setItem(3, createButton(Material.IRON_INGOT, "§eСредний", "§7Рост: §f1.0", 1.0D));
        inventory.setItem(5, createButton(Material.PLAYER_HEAD, "§fОбычный", "§7Рост: §f1.8", 1.8D));
        inventory.setItem(7, createButton(Material.AMETHYST_CLUSTER, "§dБольшой", "§7Рост: §f2.5", 2.5D));
        inventory.setItem(8, createCustomInputButton());
        player.openInventory(inventory);
    }

    public boolean handleClick(Player player, ItemStack clickedItem) {
        if (clickedItem == null || clickedItem.getType() == Material.AIR || !clickedItem.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null) {
            return false;
        }

        List<String> lore = meta.getLore();
        if (lore != null && !lore.isEmpty() && lore.getFirst().contains("Рост:")) {
            String raw = lore.getFirst().replace("§7Рост: §f", "").trim();
            try {
                double value = Double.parseDouble(raw);
                boolean changed = heightManager.setHeight(player, value);
                if (changed) {
                    player.sendMessage("§aВаш рост изменён на §f" + String.format(Locale.US, "%.2f", value));
                }
            } catch (NumberFormatException ignored) {
                return false;
            }
            return true;
        }

        if (meta.getDisplayName().contains("Свое значение")) {
            awaitingChatInput.put(player.getUniqueId(), true);
            player.closeInventory();
            player.sendMessage("§eВведите значение роста в чат (например: 1.35). Для отмены напишите §cотмена§e.");
            return true;
        }

        return false;
    }

    public boolean isAwaitingChatInput(Player player) {
        return awaitingChatInput.containsKey(player.getUniqueId());
    }

    public void clearAwaiting(Player player) {
        awaitingChatInput.remove(player.getUniqueId());
    }

    private ItemStack createButton(Material material, String name, String loreText, double value) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(List.of(loreText, "§8Нажмите для выбора"));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createCustomInputButton() {
        ItemStack item = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§bСвое значение");
            meta.setLore(Arrays.asList("§7Введите любое число", "§7в пределах ограничений"));
            item.setItemMeta(meta);
        }
        return item;
    }
}
