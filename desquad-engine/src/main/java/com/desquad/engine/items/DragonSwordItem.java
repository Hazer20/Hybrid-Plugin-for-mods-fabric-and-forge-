package com.desquad.engine.items;

import com.desquad.api.items.CustomItem;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class DragonSwordItem implements CustomItem {
    @Override public String id() { return "dragon_sword"; }

    @Override
    public ItemStack createStack() {
        ItemStack stack = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName("§5Dragon Sword");
        meta.setLore(List.of("§7Forged by DESquadCore", "§dEmits a draconic shockwave on hit"));
        meta.setCustomModelData(40001);
        stack.setItemMeta(meta);
        return stack;
    }

    @Override
    public void onUse(Player player) {
        player.getWorld().spawnParticle(Particle.DRAGON_BREATH, player.getLocation(), 50, 0.3, 1.0, 0.3, 0.01);
    }
}
