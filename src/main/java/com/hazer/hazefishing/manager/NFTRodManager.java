package com.hazer.hazefishing.manager;

import com.hazer.hazefishing.HazerFishingPlugin;
import com.hazer.hazefishing.model.NFTRod;
import com.hazer.hazefishing.model.RodTier;
import com.hazer.hazefishing.security.IntegrityValidator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
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
    private final NamespacedKey customPowerKey;
    private final NamespacedKey customMasteryKey;
    private long serial = 1;

    public NFTRodManager(HazerFishingPlugin plugin, IntegrityValidator validator) {
        this.plugin = plugin;
        this.validator = validator;
        this.rodIdKey = new NamespacedKey(plugin, "nft_rod_id");
        this.customPowerKey = new NamespacedKey(plugin, "rod_custom_power");
        this.customMasteryKey = new NamespacedKey(plugin, "rod_custom_mastery");
    }

    public NFTRod mint(Player owner, RodTier tier) {
        UUID id = UUID.randomUUID();
        long seed = ThreadLocalRandom.current().nextLong();
        String serialString = "#" + String.format("%06d", serial++);
        double score = 1000 + ThreadLocalRandom.current().nextDouble(9000) * tier.rarityMultiplier();
        NFTRod rod = new NFTRod(id, serialString, owner.getUniqueId(), Instant.now(), 1, 0,
                generateName(tier, seed), randomHex(), randomHex(), score,
                "1/" + Math.max(1, (int) (15000 / Math.max(0.1, tier.rarityMultiplier()))), seed,
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

        int customPower = 10 + rod.tier().ordinal() * 7;
        int customMastery = 1 + rod.tier().ordinal();

        meta.lore(List.of(
                Component.text("Серия: " + rod.serial(), NamedTextColor.GRAY),
                Component.text("Владелец: " + rod.owner(), NamedTextColor.DARK_GRAY),
                Component.text("Очки редкости: " + String.format(Locale.US, "%.2f", rod.rarityScore()), NamedTextColor.AQUA),
                Component.text("Кастом: Сила заброса +" + customPower, NamedTextColor.LIGHT_PURPLE),
                Component.text("Кастом: Мастерство рыбака +" + customMastery, NamedTextColor.GOLD),
                Component.text("Защита: " + validator.computeRodHash(rod).substring(0, 16), NamedTextColor.GREEN)
        ));

        applyEnchantments(meta, rod.tier());
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.getPersistentDataContainer().set(rodIdKey, PersistentDataType.STRING, rod.rodId().toString());
        meta.getPersistentDataContainer().set(customPowerKey, PersistentDataType.INTEGER, customPower);
        meta.getPersistentDataContainer().set(customMasteryKey, PersistentDataType.INTEGER, customMastery);
        stack.setItemMeta(meta);
        return stack;
    }

    private void applyEnchantments(ItemMeta meta, RodTier tier) {
        int luck = Math.min(8, 1 + tier.ordinal());
        int lure = Math.min(8, 1 + tier.ordinal() / 2);
        meta.addEnchant(Enchantment.LUCK_OF_THE_SEA, luck, true);
        meta.addEnchant(Enchantment.LURE, lure, true);

        if (tier.ordinal() >= RodTier.LEGENDARY_ROD.ordinal()) {
            meta.addEnchant(Enchantment.UNBREAKING, Math.min(10, 3 + tier.ordinal()), true);
        }
        if (tier.ordinal() >= RodTier.DIVINE_ROD.ordinal()) {
            meta.addEnchant(Enchantment.MENDING, 1, true);
        }
    }

    public Collection<NFTRod> allRods() {
        return rods.values();
    }

    public String requirementsDescription(RodTier tier) {
        return switch (tier) {
            case RARE_ROD -> "Требования: 20 уровней + 64 трески";
            case EPIC_ROD -> "Требования: 35 уровней + 64 лосося + 8 призмарина";
            case LEGENDARY_ROD -> "Требования: 50 уровней + 3 незеритовых слитка + 1 сердце моря";
            case MYTHIC_ROD -> "Требования: 65 уровней + 6 незеритовых слитков + 2 сердца моря";
            case GODLIKE_ROD -> "Требования: 80 уровней + 8 незеритовых слитков + 1 звезда Незера";
            case DIVINE_ROD -> "Требования: 100 уровней + 12 незеритовых слитков + 2 звезды Незера";
            case CELESTIAL_ROD -> "Требования: 120 уровней + 16 незеритовых слитков + 3 звезды Незера";
            case VOID_ROD -> "Требования: 150 уровней + 24 незеритовых слитков + 5 звёзд Незера";
            case NFT_ROD -> "Требования: 200 уровней + 32 незеритовых слитка + 8 звёзд Незера + 1 яйцо дракона";
        };
    }

    private String generateName(RodTier tier, long seed) {
        String[] prefixes = {"Астральная", "Криптовая", "Вечная", "Солнечная", "Теневая", "Квантовая", "Бездонная"};
        String[] suffixes = {"Шепот", "Жнец", "Импульс", "Прилив", "Печать", "Нова", "Парадокс"};
        Random random = new Random(seed);
        return prefixes[random.nextInt(prefixes.length)] + " " + suffixes[random.nextInt(suffixes.length)] + " " + tier.name().replace('_', ' ');
    }

    private String randomHex() {
        int color = ThreadLocalRandom.current().nextInt(0xFFFFFF);
        return String.format("#%06X", color);
    }
}
