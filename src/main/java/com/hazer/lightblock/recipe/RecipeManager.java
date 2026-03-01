package com.hazer.lightblock.recipe;

import com.hazer.lightblock.LightBlockPlugin;
import com.hazer.lightblock.item.LightBlockItemManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class RecipeManager {

    private final List<RecipeRequirement> vanillaCurve;
    private final LightBlockItemManager itemManager;

    public RecipeManager(LightBlockPlugin plugin, LightBlockItemManager itemManager) {
        this.itemManager = itemManager;
        this.vanillaCurve = List.of(
                new RecipeRequirement(Material.SHROOMLIGHT, 2),
                new RecipeRequirement(Material.GLOW_INK_SAC, 2),
                new RecipeRequirement(Material.PRISMARINE_CRYSTALS, 4),
                new RecipeRequirement(Material.END_ROD, 2),
                new RecipeRequirement(Material.NETHER_STAR, 1),
                new RecipeRequirement(Material.BLAZE_POWDER, 6),
                new RecipeRequirement(Material.ECHO_SHARD, 1),
                new RecipeRequirement(Material.GLOW_BERRIES, 10),
                new RecipeRequirement(Material.QUARTZ, 12),
                new RecipeRequirement(Material.LANTERN, 2),
                new RecipeRequirement(Material.OCHRE_FROGLIGHT, 2),
                new RecipeRequirement(Material.VERDANT_FROGLIGHT, 2),
                new RecipeRequirement(Material.PEARLESCENT_FROGLIGHT, 2),
                new RecipeRequirement(Material.DIAMOND, 4)
        );
    }

    public List<RecipeRequirement> getRequirements(int level) {
        int safeLevel = Math.max(1, Math.min(15, level));
        List<RecipeRequirement> requirements = new ArrayList<>();

        if (safeLevel == 2) {
            return requirements;
        }

        if (safeLevel == 1) {
            requirements.add(new RecipeRequirement(Material.GLOWSTONE, 1));
            requirements.add(new RecipeRequirement(Material.SEA_LANTERN, 4));
            requirements.add(new RecipeRequirement(Material.BLAZE_ROD, 2));
            requirements.add(new RecipeRequirement(Material.AMETHYST_SHARD, 2));
            return requirements;
        }

        int previousLevel = safeLevel == 3 ? 1 : safeLevel - 1;
        requirements.add(new RecipeRequirement(itemManager.createLightBlockItem(previousLevel, 1), 2));
        requirements.add(vanillaCurve.get(safeLevel - 2));
        requirements.add(new RecipeRequirement(Material.EXPERIENCE_BOTTLE, safeLevel));
        return requirements;
    }

    public int getCraftableAmount(Player player, int level) {
        List<RecipeRequirement> requirements = getRequirements(level);
        if (requirements.isEmpty()) {
            return 0;
        }
        int max = Integer.MAX_VALUE;

        for (RecipeRequirement requirement : requirements) {
            int available = countItem(player, requirement.prototype());
            int craftableByThis = available / requirement.amount();
            max = Math.min(max, craftableByThis);
        }

        return max == Integer.MAX_VALUE ? 0 : Math.max(0, max);
    }

    public boolean consumeForCraft(Player player, int level, int amount) {
        if (amount <= 0) {
            return false;
        }

        List<RecipeRequirement> requirements = getRequirements(level);
        if (requirements.isEmpty()) {
            return false;
        }
        for (RecipeRequirement requirement : requirements) {
            int needed = requirement.amount() * amount;
            if (countItem(player, requirement.prototype()) < needed) {
                return false;
            }
        }

        for (RecipeRequirement requirement : requirements) {
            int remaining = requirement.amount() * amount;
            ItemStack[] contents = player.getInventory().getContents();
            for (int i = 0; i < contents.length && remaining > 0; i++) {
                ItemStack item = contents[i];
                if (!matches(item, requirement.prototype())) {
                    continue;
                }

                int removed = Math.min(item.getAmount(), remaining);
                item.setAmount(item.getAmount() - removed);
                remaining -= removed;

                if (item.getAmount() <= 0) {
                    contents[i] = null;
                }
            }
            player.getInventory().setContents(contents);
        }

        player.updateInventory();
        return true;
    }

    private int countItem(Player player, ItemStack prototype) {
        int amount = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (matches(item, prototype)) {
                amount += item.getAmount();
            }
        }
        return amount;
    }

    private boolean matches(ItemStack item, ItemStack prototype) {
        if (item == null || prototype == null || item.getType() != prototype.getType()) {
            return false;
        }

        if (itemManager.isLightBlockItem(prototype)) {
            return itemManager.isLightBlockItem(item)
                    && itemManager.getLightLevel(item) == itemManager.getLightLevel(prototype);
        }

        return true;
    }

    public record RecipeRequirement(ItemStack prototype, int amount) {
        public RecipeRequirement(Material material, int amount) {
            this(new ItemStack(material), amount);
        }
    }
}
