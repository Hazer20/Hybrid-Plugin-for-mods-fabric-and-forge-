package com.hazer.lightblock.gui;

import com.hazer.lightblock.LightBlockPlugin;
import com.hazer.lightblock.item.LightBlockItemManager;
import com.hazer.lightblock.recipe.RecipeManager;
import com.hazer.lightblock.recipe.RecipeManager.RecipeRequirement;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LightBlockGUI implements Listener {

    private static final int SLOT_PREVIOUS = 45;
    private static final int SLOT_CRAFT = 49;
    private static final int SLOT_NEXT = 53;
    private static final int SLOT_RESULT = 13;
    private static final int SLOT_QUANTITY_INFO = 31;
    private static final int[] RECIPE_SLOTS = {20, 21, 22, 23, 24, 29, 30, 32, 33};

    private final LightBlockPlugin plugin;
    private final LightBlockItemManager itemManager;
    private final RecipeManager recipeManager;
    private final Map<Player, Integer> selectedLevels = new HashMap<>();

    public LightBlockGUI(LightBlockPlugin plugin, LightBlockItemManager itemManager, RecipeManager recipeManager) {
        this.plugin = plugin;
        this.itemManager = itemManager;
        this.recipeManager = recipeManager;
    }

    public void open(Player player, int initialLevel) {
        int level = Math.max(1, Math.min(15, initialLevel));
        selectedLevels.put(player, level);
        player.openInventory(buildInventory(player, level));
    }

    private Inventory buildInventory(Player player, int level) {
        Inventory inv = Bukkit.createInventory(player, 54,
                Component.text("Light Block Crafting [" + itemManager.toRoman(level) + "]", NamedTextColor.DARK_AQUA));
        fillBackground(inv);
        inv.setItem(SLOT_RESULT, itemManager.createLightBlockItem(level, 1));

        List<RecipeRequirement> requirements = recipeManager.getRequirements(level);
        for (int i = 0; i < Math.min(requirements.size(), RECIPE_SLOTS.length); i++) {
            RecipeRequirement requirement = requirements.get(i);
            boolean hasEnough = countInInventory(player, requirement.prototype()) >= requirement.amount();
            inv.setItem(RECIPE_SLOTS[i], buildRequirementItem(requirement, hasEnough));
        }

        int maxCraftable = recipeManager.getCraftableAmount(player, level);
        inv.setItem(SLOT_QUANTITY_INFO, buildQuantityInfo(maxCraftable));
        inv.setItem(SLOT_CRAFT, buildCraftButton(level, maxCraftable));
        inv.setItem(SLOT_PREVIOUS, navButton("Previous Level", level > 1));
        inv.setItem(SLOT_NEXT, navButton("Next Level", level < 15));
        return inv;
    }

    private void fillBackground(Inventory inv) {
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        meta.displayName(Component.text(" "));
        pane.setItemMeta(meta);

        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, pane);
        }
    }

    private ItemStack buildRequirementItem(RecipeRequirement requirement, boolean hasEnough) {
        ItemStack stack = requirement.prototype().clone();
        stack.setAmount(Math.min(64, requirement.amount()));

        ItemMeta meta = stack.getItemMeta();
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Required: " + requirement.amount(), NamedTextColor.YELLOW));
        lore.add(Component.text(hasEnough ? "Status: READY" : "Status: MISSING",
                hasEnough ? NamedTextColor.GREEN : NamedTextColor.RED));
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        stack.setItemMeta(meta);
        return stack;
    }

    private ItemStack buildQuantityInfo(int maxCraftable) {
        ItemStack info = new ItemStack(maxCraftable > 0 ? Material.LIME_DYE : Material.RED_DYE);
        ItemMeta meta = info.getItemMeta();
        meta.displayName(Component.text("Craftable Quantity", NamedTextColor.AQUA));
        meta.lore(List.of(Component.text("You can craft: " + maxCraftable, NamedTextColor.WHITE)));
        info.setItemMeta(meta);
        return info;
    }

    private ItemStack buildCraftButton(int level, int maxCraftable) {
        ItemStack craft = new ItemStack(maxCraftable > 0 ? Material.EMERALD : Material.BARRIER);
        ItemMeta meta = craft.getItemMeta();
        meta.displayName(Component.text("Craft Light Block " + itemManager.toRoman(level), NamedTextColor.GOLD));
        meta.lore(List.of(Component.text("Will craft: " + maxCraftable, NamedTextColor.WHITE)));
        craft.setItemMeta(meta);
        return craft;
    }

    private ItemStack navButton(String label, boolean enabled) {
        ItemStack item = new ItemStack(enabled ? Material.ARROW : Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(label, NamedTextColor.YELLOW));
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onGuiClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player) || !selectedLevels.containsKey(player)) {
            return;
        }
        if (event.getClickedInventory() == null || event.getClickedInventory() != event.getView().getTopInventory()) {
            return;
        }

        event.setCancelled(true);
        int level = selectedLevels.get(player);
        int slot = event.getRawSlot();

        if (slot == SLOT_PREVIOUS && level > 1) {
            selectedLevels.put(player, level - 1);
            player.openInventory(buildInventory(player, level - 1));
            return;
        }
        if (slot == SLOT_NEXT && level < 15) {
            selectedLevels.put(player, level + 1);
            player.openInventory(buildInventory(player, level + 1));
            return;
        }
        if (slot != SLOT_CRAFT) {
            return;
        }

        int craftable = recipeManager.getCraftableAmount(player, level);
        if (craftable <= 0) {
            player.sendMessage(Component.text("You do not have enough resources.", NamedTextColor.RED));
            return;
        }

        if (!recipeManager.consumeForCraft(player, level, craftable)) {
            player.sendMessage(Component.text("Crafting failed due to inventory changes. Try again.", NamedTextColor.RED));
            return;
        }

        ItemStack crafted = itemManager.createLightBlockItem(level, craftable);
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(crafted);
        leftover.values().forEach(item -> player.getWorld().dropItemNaturally(player.getLocation(), item));

        player.sendMessage(Component.text("Crafted " + craftable + "x Light Block " + itemManager.toRoman(level) + "!",
                NamedTextColor.GREEN));
        Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(buildInventory(player, level)));
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player) {
            selectedLevels.remove(player);
        }
    }

    private int countInInventory(Player player, ItemStack prototype) {
        int amount = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType() != prototype.getType()) {
                continue;
            }
            if (itemManager.isLightBlockItem(prototype)
                    && (!itemManager.isLightBlockItem(item)
                    || itemManager.getLightLevel(item) != itemManager.getLightLevel(prototype))) {
                continue;
            }
            amount += item.getAmount();
        }
        return amount;
    }
}
