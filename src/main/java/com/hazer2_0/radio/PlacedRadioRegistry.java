package com.hazer2_0.radio;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PlacedRadioRegistry {

    private final JavaPlugin plugin;
    private final File file;
    private final Map<String, String> channelsByPos = new HashMap<>();

    public PlacedRadioRegistry(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "radios.yml");
    }

    public void load() {
        channelsByPos.clear();
        if (!file.exists()) {
            return;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection radios = yaml.getConfigurationSection("radios");
        if (radios == null) {
            return;
        }
        for (String key : radios.getKeys(false)) {
            String channel = radios.getString(key + ".channel");
            if (channel != null && !channel.isBlank()) {
                channelsByPos.put(key, channel);
            }
        }
    }

    public void save() {
        YamlConfiguration yaml = new YamlConfiguration();
        for (Map.Entry<String, String> entry : channelsByPos.entrySet()) {
            yaml.set("radios." + entry.getKey() + ".channel", entry.getValue());
        }
        try {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Не удалось сохранить radios.yml: " + e.getMessage());
        }
    }

    public void setChannel(Location location, String channel) {
        channelsByPos.put(toKey(location), channel);
    }

    public void remove(Location location) {
        channelsByPos.remove(toKey(location));
    }

    public String getChannel(Location location) {
        return channelsByPos.get(toKey(location));
    }

    public List<Location> findByChannel(World world, String channel, Location from, int maxDistance) {
        List<Location> locations = new ArrayList<>();
        int maxDistSq = maxDistance * maxDistance;
        for (Map.Entry<String, String> entry : channelsByPos.entrySet()) {
            if (!entry.getValue().equalsIgnoreCase(channel)) {
                continue;
            }
            Location location = fromKey(entry.getKey());
            if (location == null || location.getWorld() == null || !Objects.equals(location.getWorld().getUID(), world.getUID())) {
                continue;
            }
            if (location.distanceSquared(from) <= maxDistSq) {
                locations.add(location.clone().add(0.5, 0.5, 0.5));
            }
        }
        return locations;
    }

    private String toKey(Location location) {
        return location.getWorld().getName() + ";" + location.getBlockX() + ";" + location.getBlockY() + ";" + location.getBlockZ();
    }

    private Location fromKey(String key) {
        String[] parts = key.split(";");
        if (parts.length != 4) {
            return null;
        }
        World world = Bukkit.getWorld(parts[0]);
        if (world == null) {
            return null;
        }
        try {
            return new Location(world, Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
