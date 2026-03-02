package com.hazer2_0.radio;

import com.hazer2_0.audio.AudioPlaybackManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class VoiceListener implements Listener {
    private final JavaPlugin plugin;
    private final RadioManager radioManager;
    private final AudioPlaybackManager playbackManager;

    public VoiceListener(JavaPlugin plugin, RadioManager radioManager, AudioPlaybackManager playbackManager) {
        this.plugin = plugin;
        this.radioManager = radioManager;
        this.playbackManager = playbackManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onProxyVoice(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("hazer.radio.use")) {
            return;
        }
        RadioChannel channel = radioManager.getActiveChannel(player);
        if (channel.noteBlocks().isEmpty() || !radioManager.canTransmit(player)) {
            return;
        }

        long delay = plugin.getConfig().getLong("radio.delay-ms", 300L);
        byte[] dummyPacket = new byte[]{1, 2, 3};
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Location loc : channel.noteBlocks()) {
                playbackManager.relayRadioPacket(channel.name(), loc, dummyPacket);
            }
        }, Math.max(1L, delay / 50L));
    }
}
