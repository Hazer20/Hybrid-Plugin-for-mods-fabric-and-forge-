package com.hazer2_0.disc;

import com.hazer2_0.database.SQLiteManager;
import com.hazer2_0.utils.AsyncExecutor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DiscManager {
    private final JavaPlugin plugin;
    private final SQLiteManager sqlite;
    private final AsyncExecutor executor;
    private final AudioDownloader downloader = new AudioDownloader();
    private final AudioConverter converter = new AudioConverter();
    private final NamespacedKey discIdKey;

    public DiscManager(JavaPlugin plugin, SQLiteManager sqlite, AsyncExecutor executor) {
        this.plugin = plugin;
        this.sqlite = sqlite;
        this.executor = executor;
        this.discIdKey = new NamespacedKey(plugin, "hazer_disc_id");
    }

    public CompletableFuture<ItemStack> createDisc(Player creator, String url, String name) {
        return executor.runAsync(() -> {}).thenApply(v -> {
            try {
                long maxSize = plugin.getConfig().getLong("disc.max-size-mb", 10) * 1024L * 1024L;
                int maxDur = plugin.getConfig().getInt("disc.max-duration-sec", 300);
                String storage = plugin.getConfig().getString("disc.storage-path", "discs/");
                UUID id = UUID.randomUUID();
                Path base = plugin.getDataFolder().toPath().resolve(storage);
                Path source = base.resolve(id + ".source");
                Path ogg = base.resolve(id + ".ogg");

                downloader.download(url, source, maxSize);
                converter.convertToOgg(source, ogg);
                int duration = converter.probeDurationSeconds(ogg);
                if (duration > maxDur) {
                    throw new IllegalArgumentException("Audio exceeds max duration");
                }

                sqlite.saveDisc(id, name, ogg.toString(), creator.getUniqueId().toString(), duration);

                ItemStack item = new ItemStack(Material.MUSIC_DISC_13);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(name);
                meta.setCustomModelData(Math.abs(id.hashCode()));
                meta.getPersistentDataContainer().set(discIdKey, PersistentDataType.STRING, id.toString());
                item.setItemMeta(meta);
                return item;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public Optional<SQLiteManager.DiscRow> getDisc(UUID id) {
        return sqlite.findDisc(id);
    }

    public UUID getDiscId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        String id = item.getItemMeta().getPersistentDataContainer().get(discIdKey, PersistentDataType.STRING);
        if (id == null) return null;
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
