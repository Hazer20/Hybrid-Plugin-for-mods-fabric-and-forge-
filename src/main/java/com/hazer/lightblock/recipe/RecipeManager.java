package com.hazer.lightblock.recipe;

import com.hazer.lightblock.LightBlockPlugin;
import com.hazer.lightblock.item.LightBlockItemManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class RecipeManager {

    public RecipeManager(LightBlockPlugin plugin, LightBlockItemManager itemManager) {
        // Единственный рецепт: уровень I.
    }

    public List<RecipeRequirement> getRequirements(int level) {
        List<RecipeRequirement> requirements = new ArrayList<>();
        requirements.add(new RecipeRequirement(Material.GLOWSTONE, 1));
        requirements.add(new RecipeRequirement(Material.SEA_LANTERN, 4));
        requirements.add(new RecipeRequirement(Material.BLAZE_ROD, 2));
        requirements.add(new RecipeRequirement(Material.AMETHYST_SHARD, 2));
        return requirements;
    }

    public int getCraftableAmount(Player player, int level) {
        List<RecipeRequirement> requirements = getRequirements(1);
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

        List<RecipeRequirement> requirements = getRequirements(1);
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
        return item != null && prototype != null && item.getType() == prototype.getType();
    }

    public record RecipeRequirement(ItemStack prototype, int amount) {
        public RecipeRequirement(Material material, int amount) {
            this(new ItemStack(material), amount);
        }
    }
}
