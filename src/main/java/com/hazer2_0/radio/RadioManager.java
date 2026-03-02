package com.hazer2_0.radio;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RadioManager {
    private final JavaPlugin plugin;
    private final NoteBlockRegistry registry = new NoteBlockRegistry();
    private final LeverDetector leverDetector = new LeverDetector();
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    public RadioManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public RadioChannel getActiveChannel(Player player) {
        int transmitRadius = plugin.getConfig().getInt("radio.transmit-radius", 3);
        int range = plugin.getConfig().getInt("radio.max-channel-range", 256);
        World world = player.getWorld();
        Location loc = player.getLocation();

        for (int x = -transmitRadius; x <= transmitRadius; x++) {
            for (int y = -transmitRadius; y <= transmitRadius; y++) {
                for (int z = -transmitRadius; z <= transmitRadius; z++) {
                    Location target = loc.clone().add(x, y, z);
                    if (!(target.getBlock().getBlockData() instanceof NoteBlock)) continue;
                    String name = ChannelNameResolver.resolve(target.getBlock().getState());
                    if (name == null || name.isBlank()) continue;
                    if (!leverDetector.isLeverPoweredNearby(target)) continue;
                    List<Location> network = registry.findByName(world, name, range, target);
                    if (network.size() > 1) {
                        return new RadioChannel(name, network);
                    }
                }
            }
        }
        return new RadioChannel("", Collections.emptyList());
    }

    public boolean canTransmit(Player player) {
        long now = System.currentTimeMillis();
        long last = cooldowns.getOrDefault(player.getUniqueId(), 0L);
        long cooldownMs = plugin.getConfig().getLong("radio.cooldown-ms", 600L);
        if (now - last < cooldownMs) {
            return false;
        }
        cooldowns.put(player.getUniqueId(), now);
        return true;
    }
}
