package com.hazer2_0.disc;

import com.hazer2_0.audio.AudioPlaybackManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Jukebox;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.UUID;

public class JukeboxListener implements Listener {
    private final JavaPlugin plugin;
    private final DiscManager discManager;
    private final AudioPlaybackManager playbackManager;

    public JukeboxListener(JavaPlugin plugin, DiscManager discManager, AudioPlaybackManager playbackManager) {
        this.plugin = plugin;
        this.discManager = discManager;
        this.playbackManager = playbackManager;
    }

    @EventHandler
    public void onAutomatedMove(InventoryMoveItemEvent event) {
        if (event.getDestination().getHolder() instanceof Jukebox jukebox) {
            handleInsert(jukebox.getBlock(), event.getItem());
        }
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        Arrays.stream(event.getChunk().getTileEntities(false))
                .filter(state -> state.getType() == Material.JUKEBOX)
                .forEach(state -> playbackManager.stop(UUID.nameUUIDFromBytes(state.getLocation().toString().getBytes())));
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.JUKEBOX) {
            playbackManager.stop(UUID.nameUUIDFromBytes(event.getBlock().getLocation().toString().getBytes()));
        }
    }

    private void handleInsert(Block block, ItemStack item) {
        UUID discId = discManager.getDiscId(item);
        if (discId == null) return;

        discManager.getDisc(discId).ifPresent(row -> {
            UUID source = UUID.nameUUIDFromBytes(block.getLocation().toString().getBytes());
            playbackManager.playOggAt(block.getLocation().add(0.5, 0.5, 0.5), Path.of(row.filePath()), source);
            plugin.getLogger().info("Playing custom disc " + row.name() + " at " + block.getLocation());
        });
    }
}
