package com.desquad.engine.blocks;

import com.desquad.api.blocks.CustomBlock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/** Пример кастомного блока arcane_forge. */
public final class ArcaneForgeBlock implements CustomBlock {
    @Override public String id() { return "arcane_forge"; }

    @Override
    public void place(Player player, Location location) {
        location.getBlock().setType(Material.SMITHING_TABLE);
        player.sendMessage("§dArcane Forge placed.");
    }
}
