package dev.hazer.universe.systems;

import dev.hazer.universe.FracturedUniverse;
import org.bukkit.*;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ChunkCollapseManager {
    private final FracturedUniverse plugin;

    public ChunkCollapseManager(FracturedUniverse plugin) {
        this.plugin = plugin;
    }

    public void triggerCollapse() {
        World world = Bukkit.getWorlds().getFirst();
        int chunks = plugin.getConfig().getInt("вселенная.коллапс_чанков.случайных_чанков", 3);
        int restoreSec = plugin.getConfig().getInt("вселенная.коллапс_чанков.восстановление_сек", 180);

        Bukkit.broadcastMessage(ChatColor.DARK_RED + "[Коллапс] Пространственный коллапс начался.");

        for (int i = 0; i < chunks; i++) {
            int cx = ThreadLocalRandom.current().nextInt(-120, 121);
            int cz = ThreadLocalRandom.current().nextInt(-120, 121);
            Chunk chunk = world.getChunkAt(cx, cz);
            List<BlockSnapshot> snapshots = carveChunk(chunk);

            Bukkit.getScheduler().runTaskLater(plugin, () -> restoreChunk(snapshots), restoreSec * 20L);
        }
    }

    private List<BlockSnapshot> carveChunk(Chunk chunk) {
        List<BlockSnapshot> snapshots = new ArrayList<>();
        World world = chunk.getWorld();
        int minY = world.getMinHeight();
        int maxY = Math.min(world.getMaxHeight(), minY + 32);

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = minY; y < maxY; y++) {
                    Block block = chunk.getBlock(x, y, z);
                    if (block.getType().isAir()) {
                        continue;
                    }
                    snapshots.add(new BlockSnapshot(block.getLocation(), block.getBlockData()));
                    block.setType(Material.AIR, false);
                }
            }
        }

        Location center = chunk.getBlock(8, world.getHighestBlockYAt(chunk.getBlock(8, 0, 8).getLocation()), 8).getLocation();
        world.spawnParticle(Particle.SQUID_INK, center, 120, 8, 8, 8, 0.02);
        return snapshots;
    }

    private void restoreChunk(List<BlockSnapshot> snapshots) {
        for (BlockSnapshot snap : snapshots) {
            Block block = snap.location().getBlock();
            block.setBlockData(snap.data(), false);
        }
        if (!snapshots.isEmpty()) {
            snapshots.getFirst().location().getWorld().playSound(snapshots.getFirst().location(), Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1f, 1.2f);
        }
    }

    private record BlockSnapshot(Location location, org.bukkit.block.data.BlockData data) {
    }
}
