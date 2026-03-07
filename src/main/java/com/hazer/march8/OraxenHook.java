package com.hazer.march8;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

/**
 * Runtime integration point with Oraxen.
 *
 * <p>Implementation uses reflection to keep compilation safe in environments
 * where Oraxen API dependency is not present at compile time.
 */
public final class OraxenHook {

    private final March8Plugin plugin;
    private final ConfigManager configManager;
    private boolean oraxenInstalled;

    public OraxenHook(@NotNull March8Plugin plugin, @NotNull ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        refreshState();
    }

    public void refreshState() {
        Plugin oraxen = Bukkit.getPluginManager().getPlugin("Oraxen");
        this.oraxenInstalled = oraxen != null && oraxen.isEnabled();
        plugin.getLogger().info("Oraxen check -> " + (oraxenInstalled ? "enabled" : "absent/disabled"));
    }

    public boolean isOraxenInstalled() {
        if (!configManager.getBoolean("oraxen.enabled-check", true)) {
            return false;
        }
        return oraxenInstalled;
    }

    /**
     * Attempt to build item through Oraxen API:
     * ItemBuilder item = OraxenItems.getItemById("love_blade").build();
     */
    public @Nullable ItemStack createOraxenItem(@NotNull String itemId) {
        if (!isOraxenInstalled()) {
            return null;
        }

        try {
            Class<?> oraxenItemsClass = Class.forName("io.th0rgal.oraxen.api.OraxenItems");
            Method getItemById = oraxenItemsClass.getMethod("getItemById", String.class);
            Object itemBuilder = getItemById.invoke(null, itemId);

            if (itemBuilder == null) {
                plugin.getLogger().warning("Oraxen item id not found: " + itemId);
                return null;
            }

            Method build = itemBuilder.getClass().getMethod("build");
            Object built = build.invoke(itemBuilder);
            if (built instanceof ItemStack stack) {
                return stack;
            }

            plugin.getLogger().warning("Oraxen build() did not return ItemStack for id: " + itemId);
            return null;
        } catch (ClassNotFoundException e) {
            plugin.getLogger().warning("Oraxen API classes unavailable at runtime.");
            return null;
        } catch (ReflectiveOperationException e) {
            plugin.getLogger().warning("Failed to build Oraxen item '" + itemId + "': " + e.getMessage());
            return null;
        }
    }

    public @NotNull ItemStack createSpringBladeFallback() {
        String fallbackName = configManager.getString("gifts.sword.name", "<pink><bold>Клинок Весны</bold></pink>");
        List<String> fallbackLore = configManager.getStringList("gifts.sword.lore");
        String materialName = configManager.getString("gifts.sword.fallback-material", "DIAMOND_SWORD");

        Material material = Optional.ofNullable(Material.matchMaterial(materialName)).orElse(Material.DIAMOND_SWORD);
        ItemStack sword = new ItemStack(material, 1);
        ItemMeta meta = sword.getItemMeta();
        if (meta != null) {
            Component displayName = configManager.mm(fallbackName);
            meta.displayName(displayName);
            meta.lore(configManager.mmList(fallbackLore));
            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
            sword.setItemMeta(meta);
        }

        sword.addUnsafeEnchantment(Enchantment.SHARPNESS, 5);
        return sword;
    }

    public @NotNull ItemStack createSpringBlade() {
        String id = configManager.getString("oraxen.item-id", "love_blade");
        ItemStack oraxenItem = createOraxenItem(id);
        if (oraxenItem != null) {
            return applyUniversalSwordMeta(oraxenItem.clone());
        }
        return createSpringBladeFallback();
    }

    private @NotNull ItemStack applyUniversalSwordMeta(@NotNull ItemStack sword) {
        ItemMeta meta = sword.getItemMeta();
        if (meta == null) {
            return sword;
        }

        String name = configManager.getString("gifts.sword.name", "<pink><bold>Клинок Весны</bold></pink>");
        List<String> lore = configManager.getStringList("gifts.sword.lore");

        meta.displayName(configManager.mm(name));
        meta.lore(configManager.mmList(lore));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        sword.setItemMeta(meta);
        return sword;
    }
}
