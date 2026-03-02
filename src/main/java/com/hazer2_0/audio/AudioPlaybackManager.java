package com.hazer2_0.audio;

import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AudioPlaybackManager {
    private final JavaPlugin plugin;
    private final Map<UUID, String> active = new ConcurrentHashMap<>();

    public AudioPlaybackManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void playOggAt(Location location, Path path, UUID sourceId) {
        active.put(sourceId, path.toString());
        plugin.getLogger().info("Queued OGG playback at " + location + " from " + path);
    }

    public void relayRadioPacket(String channel, Location location, byte[] opusData) {
        plugin.getLogger().fine("Relaying radio packet channel=" + channel + " at " + location + " bytes=" + opusData.length);
    }

    public void stop(UUID sourceId) {
        active.remove(sourceId);
    }

    public void stopAll() {
        active.clear();
    }
}
