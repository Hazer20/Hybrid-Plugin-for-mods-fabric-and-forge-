package com.hazer.hazefishing.manager;

import com.hazer.hazefishing.HazerFishingPlugin;
import com.hazer.hazefishing.model.NFTRod;
import com.hazer.hazefishing.model.RodTier;
import com.hazer.hazefishing.security.IntegrityValidator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public final class NFTRodManager {
    private final HazerFishingPlugin plugin;
    private final IntegrityValidator validator;
    private final Map<UUID, NFTRod> rods = new ConcurrentHashMap<>();
    private final NamespacedKey rodIdKey;
    private long serial = 1;

    public NFTRodManager(HazerFishingPlugin plugin, IntegrityValidator validator) {
        this.plugin = plugin;
        this.validator = validator;
        this.rodIdKey = new NamespacedKey(plugin, "nft_rod_id");
    }

    public NFTRod mint(Player owner, RodTier tier) {
        UUID id = UUID.randomUUID();
        long seed = ThreadLocalRandom.current().nextLong();
        String serialString = "#" + String.format("%06d", serial++);
        double score = 1000 + ThreadLocalRandom.current().nextDouble(9000);
        NFTRod rod = new NFTRod(id, serialString, owner.getUniqueId(), Instant.now(), 1, 0,
                generateName(tier, seed), randomHex(), randomHex(), score,
                "1/" + Math.max(1, (int) (10000 / Math.max(0.1, tier.rarityMultiplier()))), seed,
                tier, 2500, 2500, (long) (15000 * tier.rarityMultiplier()), tier == RodTier.NFT_ROD);
        rods.put(id, rod);
        return rod;
    }

    public Optional<NFTRod> fromItem(ItemStack item) {
        if (item == null || item.getType() != Material.FISHING_ROD || !item.hasItemMeta()) {
            return Optional.empty();
        }
        String id = item.getItemMeta().getPersistentDataContainer().get(rodIdKey, PersistentDataType.STRING);
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(rods.get(UUID.fromString(id)));
    }

    public ItemStack toItem(NFTRod rod) {
        ItemStack stack = new ItemStack(Material.FISHING_ROD);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(Component.text(rod.uniqueName(), rod.tier().color()));
        meta.lore(List.of(
                Component.text("Serial: " + rod.serial(), NamedTextColor.GRAY),
                Component.text("Owner: " + rod.owner(), NamedTextColor.DARK_GRAY),
                Component.text("Rarity score: " + String.format(Locale.US, "%.2f", rod.rarityScore()), NamedTextColor.AQUA),
                Component.text("Integrity: " + validator.computeRodHash(rod).substring(0, 16), NamedTextColor.GREEN)
        ));
        meta.getPersistentDataContainer().set(rodIdKey, PersistentDataType.STRING, rod.rodId().toString());
        stack.setItemMeta(meta);
        return stack;
    }

    public Collection<NFTRod> allRods() {
        return rods.values();
    }

    private String generateName(RodTier tier, long seed) {
        String[] prefixes = {"Astral", "Cryptic", "Eternal", "Solar", "Nebula", "Quantum", "Abyssal"};
        String[] suffixes = {"Whisper", "Reaver", "Pulse", "Tide", "Sigil", "Nova", "Paradox"};
        Random random = new Random(seed);
        return prefixes[random.nextInt(prefixes.length)] + " " + suffixes[random.nextInt(suffixes.length)] + " " + tier.name().replace('_', ' ');
    }

    private String randomHex() {
        int color = ThreadLocalRandom.current().nextInt(0xFFFFFF);
        return String.format("#%06X", color);
    }
}
