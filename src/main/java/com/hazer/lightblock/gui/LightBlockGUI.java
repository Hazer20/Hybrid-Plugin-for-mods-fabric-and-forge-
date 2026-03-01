package com.hazer.lightblock.gui;

import com.hazer.lightblock.LightBlockPlugin;
import com.hazer.lightblock.item.LightBlockItemManager;
import com.hazer.lightblock.recipe.RecipeManager;
import com.hazer.lightblock.recipe.RecipeManager.RecipeRequirement;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LightBlockGUI implements Listener {

    private static final String GUI_TITLE_PREFIX = "Крафт светоблока I";
    private static final int SLOT_CRAFT = 49;
    private static final int SLOT_RESULT = 13;
    private static final int SLOT_QUANTITY_INFO = 31;
    private static final int[] RECIPE_SLOTS = {20, 21, 22, 23, 24, 29, 30, 32, 33};

    private final LightBlockPlugin plugin;
    private final LightBlockItemManager itemManager;
    private final RecipeManager recipeManager;
    private final Map<Player, Boolean> viewers = new HashMap<>();

    public LightBlockGUI(LightBlockPlugin plugin, LightBlockItemManager itemManager, RecipeManager recipeManager) {
        this.plugin = plugin;
        this.itemManager = itemManager;
        this.recipeManager = recipeManager;
    }

    public void open(Player player, int ignoredLevel) {
        viewers.put(player, true);
        player.openInventory(buildInventory(player));
        player.playSound(player.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 0.7f, 1.3f);
    }

    private Inventory buildInventory(Player player) {
        Inventory inv = Bukkit.createInventory(player, 54,
                Component.text(GUI_TITLE_PREFIX, NamedTextColor.DARK_AQUA, TextDecoration.BOLD));
        fillBackground(inv);
        inv.setItem(SLOT_RESULT, itemManager.createLightBlockItem(1, 1));

        List<RecipeRequirement> requirements = recipeManager.getRequirements(1);
        for (int i = 0; i < Math.min(requirements.size(), RECIPE_SLOTS.length); i++) {
            RecipeRequirement requirement = requirements.get(i);
            boolean hasEnough = countInInventory(player, requirement.prototype()) >= requirement.amount();
            inv.setItem(RECIPE_SLOTS[i], buildRequirementItem(requirement, hasEnough));
        }

        int maxCraftable = recipeManager.getCraftableAmount(player, 1);
        inv.setItem(SLOT_QUANTITY_INFO, buildQuantityInfo(maxCraftable));
        inv.setItem(SLOT_CRAFT, buildCraftButton(maxCraftable));
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
        meta.lore(List.of(Component.text("Можно создать: " + maxCraftable, NamedTextColor.WHITE)));
        info.setItemMeta(meta);
        return info;
    }

    private ItemStack buildCraftButton(int maxCraftable) {
        ItemStack craft = new ItemStack(maxCraftable > 0 ? Material.EMERALD : Material.BARRIER);
        ItemMeta meta = craft.getItemMeta();
        meta.displayName(Component.text("Создать Световой блок I", NamedTextColor.GOLD, TextDecoration.BOLD));
        meta.lore(List.of(Component.text("Количество: " + maxCraftable, NamedTextColor.WHITE)));
        craft.setItemMeta(meta);
        return craft;
    }

    @EventHandler
    public void onGuiClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player) || !viewers.containsKey(player)) {
            return;
        }
        if (!isLightBlockView(event.getView().title())) {
            return;
        }

        event.setCancelled(true);

        if (event.getClickedInventory() == null || event.getClickedInventory() != event.getView().getTopInventory()) {
            return;
        }

        if (event.getRawSlot() != SLOT_CRAFT) {
            return;
        }

        int craftable = recipeManager.getCraftableAmount(player, 1);
        if (craftable <= 0) {
            player.sendMessage(Component.text("Недостаточно ресурсов для крафта.", NamedTextColor.RED));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.8f);
            return;
        }

        if (!recipeManager.consumeForCraft(player, 1, craftable)) {
            player.sendMessage(Component.text("Крафт не выполнен: инвентарь изменился. Попробуйте снова.", NamedTextColor.RED));
            return;
        }

        ItemStack crafted = itemManager.createLightBlockItem(1, craftable);
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(crafted);
        leftover.values().forEach(item -> player.getWorld().dropItemNaturally(player.getLocation(), item));

        player.sendMessage(Component.text("Создано " + craftable + "x Световой блок I!", NamedTextColor.GREEN));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.3f);
        player.spawnParticle(Particle.END_ROD, player.getLocation().add(0, 1, 0), 20, 0.3, 0.5, 0.3, 0.01);
        Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(buildInventory(player)));
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
            viewers.remove(player);
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
            amount += item.getAmount();
        }
        return amount;
    }
}
