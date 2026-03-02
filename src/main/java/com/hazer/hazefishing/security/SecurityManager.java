package com.hazer.hazefishing.security;

import com.hazer.hazefishing.HazerFishingPlugin;
import com.hazer.hazefishing.model.NFTRod;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SecurityManager {
    private final HazerFishingPlugin plugin;
    private final Set<UUID> blacklist = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Long> abuseAttempts = new ConcurrentHashMap<>();

    public SecurityManager(HazerFishingPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean verifyRodUse(Player player, NFTRod rod) {
        if (blacklist.contains(player.getUniqueId())) {
            return false;
        }
        if (!player.getUniqueId().equals(rod.owner()) && rod.soulbound()) {
            registerAttempt(player.getUniqueId());
            return false;
        }
        return true;
    }

    public boolean isSuspicious(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        String nbt = item.getItemMeta().toString();
        return nbt.contains("PublicBukkitValues") && nbt.length() > 5000;
    }

    public void registerAttempt(UUID player) {
        abuseAttempts.merge(player, 1L, Long::sum);
        if (abuseAttempts.getOrDefault(player, 0L) > 5) {
            blacklist.add(player);
            plugin.getLogger().warning("Player blacklisted due to exploit attempts: " + player);
        }
    }

    public Map<UUID, Long> attempts() {
        return abuseAttempts;
    }
}
