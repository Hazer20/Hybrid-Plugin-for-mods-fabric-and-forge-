package com.hazer2_0.radio;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.NoteBlock;

import java.util.ArrayList;
import java.util.List;

public class NoteBlockRegistry {

    public List<Location> findByName(World world, String channelName, int maxDistance, Location from) {
        List<Location> result = new ArrayList<>();
        int radius = Math.max(16, maxDistance);
        int baseX = from.getBlockX();
        int baseY = from.getBlockY();
        int baseZ = from.getBlockZ();

        for (int x = baseX - radius; x <= baseX + radius; x++) {
            for (int y = Math.max(world.getMinHeight(), baseY - radius); y <= Math.min(world.getMaxHeight(), baseY + radius); y++) {
                for (int z = baseZ - radius; z <= baseZ + radius; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (!(block.getBlockData() instanceof NoteBlock)) {
                        continue;
                    }
                    String customName = ChannelNameResolver.resolve(block.getState());
                    if (customName != null && customName.equalsIgnoreCase(channelName)) {
                        result.add(block.getLocation().add(0.5, 0.5, 0.5));
                    }
                }
            }
        }
        return result;
    }
}
