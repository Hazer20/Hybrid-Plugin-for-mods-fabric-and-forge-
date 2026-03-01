package ru.hybridplugin.securityprefix.service;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import ru.hybridplugin.securityprefix.model.PrefixType;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class PrefixService {

    private static final String MENU_TITLE = "§8Префиксы";
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    private final JavaPlugin plugin;
    private final File file;
    private YamlConfiguration config;

    public PrefixService(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "prefixes.yml");
        load();
    }

    public synchronized void load() {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new IllegalStateException("Не удалось создать prefixes.yml", e);
            }
        }
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public void openMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 27, Component.text(MENU_TITLE));

        inventory.setItem(11, buildItem(Material.NAME_TAG, "§aСвой обычный префикс",
                List.of("§7Клик: подсказка по команде", "§e/prefix set <текст>")));

        inventory.setItem(13, buildItem(Material.EMERALD, "§6Премиум префикс",
                List.of("§7Доступен по permission:", "§ehybrid.prefix.premium", "§e/prefix premium <текст>")));

        inventory.setItem(15, buildItem(Material.BARRIER, "§cУбрать префикс",
                List.of("§7Сбросить отображение")));

        player.openInventory(inventory);
    }

    public synchronized void clearPrefix(Player player) {
        String path = path(player.getUniqueId());
        config.set(path + ".text", "");
        config.set(path + ".type", PrefixType.NONE.name());
        save();
        updateDisplayName(player);
    }

    public synchronized boolean setCustomPrefix(Player player, String rawPrefix) {
        if (rawPrefix.length() > plugin.getConfig().getInt("prefix.max-length", 16)) {
            return false;
        }

        String path = path(player.getUniqueId());
        config.set(path + ".text", ChatColor.translateAlternateColorCodes('&', rawPrefix));
        config.set(path + ".type", PrefixType.CUSTOM.name());
        save();
        updateDisplayName(player);
        return true;
    }

    public synchronized String buyAndSetPremium(Player player, String rawPrefix) {
        if (rawPrefix.length() > plugin.getConfig().getInt("prefix.max-length", 16)) {
            return "too_long";
        }

        if (!player.hasPermission("hybrid.prefix.premium")) {
            return "no_permission";
        }

        String path = path(player.getUniqueId());
        config.set(path + ".text", ChatColor.translateAlternateColorCodes('&', rawPrefix));
        config.set(path + ".type", PrefixType.PREMIUM.name());
        save();
        updateDisplayName(player);
        return "ok";
    }

    public synchronized void updateDisplayName(Player player) {
        ConfigurationSection section = config.getConfigurationSection(path(player.getUniqueId()));
        if (section == null) {
            player.displayName(Component.text(player.getName()));
            player.playerListName(Component.text(player.getName()));
            return;
        }

        String prefix = section.getString("text", "").trim();
        if (prefix.isEmpty()) {
            player.displayName(Component.text(player.getName()));
            player.playerListName(Component.text(player.getName()));
            return;
        }

        String result = prefix + " §7" + player.getName();
        Component finalName = LEGACY.deserialize(result);
        player.displayName(finalName);
        player.playerListName(finalName);
    }

    public String getMenuTitle() {
        return MENU_TITLE;
    }

    private String path(UUID uuid) {
        return "players." + uuid;
    }

    private ItemStack buildItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(LEGACY.deserialize(name));
        meta.lore(lore.stream().map(LEGACY::deserialize).toList());
        item.setItemMeta(meta);
        return item;
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось сохранить prefixes.yml", e);
        }
    }
}
