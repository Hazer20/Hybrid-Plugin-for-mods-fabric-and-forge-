package com.hazer.lightblock.gui;

import com.hazer.lightblock.LightBlockPlugin;
import com.hazer.lightblock.item.LightBlockItemManager;
import com.hazer.lightblock.recipe.RecipeManager;
import com.hazer.lightblock.recipe.RecipeManager.RecipeRequirement;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.enchantments.Enchantment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LightBlockGUI implements Listener {

    private static final String GUI_TITLE_PREFIX = "Крафт светоблоков";

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
        int level = sanitizeLevel(initialLevel, true);
        selectedLevels.put(player, level);
        player.openInventory(buildInventory(player, level));
        player.playSound(player.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 0.7f, 1.3f);
    }

    private Inventory buildInventory(Player player, int level) {
        Inventory inv = Bukkit.createInventory(player, 54,
                Component.text(GUI_TITLE_PREFIX + " [" + itemManager.toRoman(level) + "]", NamedTextColor.DARK_AQUA));
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
        inv.setItem(SLOT_PREVIOUS, navButton("Предыдущий уровень", getPreviousLevel(level) != level));
        inv.setItem(SLOT_NEXT, navButton("Следующий уровень", getNextLevel(level) != level));

        if (level == 2) {
            inv.setItem(SLOT_CRAFT, disabledLevelInfo());
        }
        return inv;
    }

    private void fillBackground(Inventory inv) {
        ItemStack pane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        meta.displayName(Component.text(" "));
        pane.setItemMeta(meta);

        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, pane);
        }
        for (int slot : RECIPE_SLOTS) {
            ItemStack accent = new ItemStack(Material.CYAN_STAINED_GLASS_PANE);
            ItemMeta accentMeta = accent.getItemMeta();
            accentMeta.displayName(Component.text("Слот ингредиента", NamedTextColor.DARK_AQUA));
            accent.setItemMeta(accentMeta);
            inv.setItem(slot, accent);
        }

    }

    private ItemStack buildRequirementItem(RecipeRequirement requirement, boolean hasEnough) {
        ItemStack stack = requirement.prototype().clone();
        stack.setAmount(Math.min(64, requirement.amount()));

        ItemMeta meta = stack.getItemMeta();
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Нужно: " + requirement.amount(), NamedTextColor.YELLOW));
        lore.add(Component.text(hasEnough ? "Статус: ХВАТАЕТ" : "Статус: НЕ ХВАТАЕТ",
                hasEnough ? NamedTextColor.GREEN : NamedTextColor.RED));
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        stack.setItemMeta(meta);
        return stack;
    }

    private ItemStack buildQuantityInfo(int maxCraftable) {
        ItemStack info = new ItemStack(maxCraftable > 0 ? Material.LIME_DYE : Material.RED_DYE);
        ItemMeta meta = info.getItemMeta();
        meta.displayName(Component.text("Доступно для крафта", NamedTextColor.AQUA, TextDecoration.BOLD));
        meta.lore(List.of(
                Component.text("Можно создать: " + maxCraftable, NamedTextColor.WHITE),
                Component.text(maxCraftable > 0 ? "Нажмите кнопку ниже" : "Соберите больше ресурсов",
                        maxCraftable > 0 ? NamedTextColor.GREEN : NamedTextColor.RED)
        ));
        info.setItemMeta(meta);
        return info;
    }

    private ItemStack buildCraftButton(int level, int maxCraftable) {
        ItemStack craft = new ItemStack(maxCraftable > 0 ? Material.EMERALD : Material.BARRIER);
        ItemMeta meta = craft.getItemMeta();
        meta.displayName(Component.text("Создать светоблок " + itemManager.toRoman(level), NamedTextColor.GOLD, TextDecoration.BOLD));
        meta.lore(List.of(Component.text("Количество: " + maxCraftable, NamedTextColor.WHITE)));
        if (maxCraftable > 0) {
            meta.addEnchant(Enchantment.LURE, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        craft.setItemMeta(meta);
        return craft;
    }

    private ItemStack disabledLevelInfo() {
        ItemStack blocked = new ItemStack(Material.BARRIER);
        ItemMeta meta = blocked.getItemMeta();
        meta.displayName(Component.text("2-й уровень отключён", NamedTextColor.RED, TextDecoration.BOLD));
        meta.lore(List.of(Component.text("Этот уровень недоступен из-за найденного дюпа.", NamedTextColor.GRAY)));
        blocked.setItemMeta(meta);
        return blocked;
    }

    private ItemStack navButton(String label, boolean enabled) {
        ItemStack item = new ItemStack(enabled ? Material.ARROW : Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(label, NamedTextColor.YELLOW));
        if (!enabled) {
            meta.lore(List.of(Component.text("Недоступно", NamedTextColor.RED)));
        }
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onGuiClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player) || !selectedLevels.containsKey(player)) {
            return;
        }
        if (!isLightBlockView(event.getView().title())) {
            return;
        }

        event.setCancelled(true);

        if (event.getClickedInventory() == null || event.getClickedInventory() != event.getView().getTopInventory()) {
            return;
        }

        int level = selectedLevels.get(player);
        int slot = event.getRawSlot();

        if (slot == SLOT_PREVIOUS && level > 1) {
            int nextLevel = getPreviousLevel(level);
            selectedLevels.put(player, nextLevel);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 0.8f);
            player.openInventory(buildInventory(player, nextLevel));
            return;
        }
        if (slot == SLOT_NEXT && level < 15) {
            int nextLevel = getNextLevel(level);
            selectedLevels.put(player, nextLevel);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
            player.openInventory(buildInventory(player, nextLevel));
            return;
        }
        if (slot != SLOT_CRAFT) {
            return;
        }

        if (level == 2) {
            player.sendMessage(Component.text("2-й уровень отключён из-за проблем с безопасностью.", NamedTextColor.RED));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.6f);
            return;
        }

        int craftable = recipeManager.getCraftableAmount(player, level);
        if (craftable <= 0) {
            player.sendMessage(Component.text("Недостаточно ресурсов для крафта.", NamedTextColor.RED));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.8f);
            return;
        }

        if (!recipeManager.consumeForCraft(player, level, craftable)) {
            player.sendMessage(Component.text("Крафт не выполнен: инвентарь изменился. Попробуйте снова.", NamedTextColor.RED));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.7f);
            return;
        }

        ItemStack crafted = itemManager.createLightBlockItem(level, craftable);
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(crafted);
        leftover.values().forEach(item -> player.getWorld().dropItemNaturally(player.getLocation(), item));

        player.sendMessage(Component.text("Создано " + craftable + "x Световой блок " + itemManager.toRoman(level) + "!",
                NamedTextColor.GREEN));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.3f);
        player.spawnParticle(Particle.END_ROD, player.getLocation().add(0, 1, 0), 30, 0.4, 0.6, 0.4, 0.02);
        Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(buildInventory(player, level)));
    }

    @EventHandler
    public void onGuiDrag(InventoryDragEvent event) {
        if (isLightBlockView(event.getView().title())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player) {
            selectedLevels.remove(player);
        }
    }

    private boolean isLightBlockView(Component title) {
        return title != null && PlainTextComponentSerializer.plainText().serialize(title).contains(GUI_TITLE_PREFIX);
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

    private int sanitizeLevel(int level, boolean preferLowerWhenTwo) {
        int safeLevel = Math.max(1, Math.min(15, level));
        if (safeLevel == 2) {
            return preferLowerWhenTwo ? 1 : 3;
        }
        return safeLevel;
    }

    private int getPreviousLevel(int level) {
        if (level <= 1) {
            return 1;
        }
        return sanitizeLevel(level - 1, true);
    }

    private int getNextLevel(int level) {
        if (level >= 15) {
            return 15;
        }
        return sanitizeLevel(level + 1, false);
    }
}
