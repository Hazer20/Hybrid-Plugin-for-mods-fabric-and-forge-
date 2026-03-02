package com.hazer2_0.radio;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.List;

public class NoteBlockRegistry {

    private final PlacedRadioRegistry placedRadioRegistry;

    public NoteBlockRegistry(PlacedRadioRegistry placedRadioRegistry) {
        this.placedRadioRegistry = placedRadioRegistry;
    }

    public List<Location> findByName(World world, String channelName, int maxDistance, Location from) {
        return placedRadioRegistry.findByChannel(world, channelName, from, maxDistance);
    }
}
