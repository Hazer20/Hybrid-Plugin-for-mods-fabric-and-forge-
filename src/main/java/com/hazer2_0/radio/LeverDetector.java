package com.hazer2_0.radio;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Powerable;

import java.util.List;

public class LeverDetector {
    private static final List<BlockFace> FACES = List.of(BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN);

    public boolean isLeverPoweredNearby(Location noteBlockLocation) {
        Block base = noteBlockLocation.getBlock();
        for (BlockFace face : FACES) {
            Block relative = base.getRelative(face);
            if (relative.getType() == Material.LEVER && relative.getBlockData() instanceof Powerable powerable && powerable.isPowered()) {
                return true;
            }
        }
        return false;
    }
}
