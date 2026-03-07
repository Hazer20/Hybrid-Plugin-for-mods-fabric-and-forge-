package dev.hazer.universe.worlds;

import dev.hazer.universe.FracturedUniverse;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;

public class WorldManager {
    private final FracturedUniverse plugin;

    public WorldManager(FracturedUniverse plugin) {
        this.plugin = plugin;
    }

    public World getOrCreateBrokenOverworld() {
        return getOrCreateWorld(plugin.getConfig().getString("worlds.broken_overworld", "fractured_broken_overworld"), World.Environment.NORMAL);
    }

    public World getOrCreateCyberDimension() {
        return getOrCreateWorld(plugin.getConfig().getString("worlds.cyber_dimension", "fractured_cyber_dimension"), World.Environment.THE_END);
    }

    public World getOrCreateVoidSea() {
        return getOrCreateWorld(plugin.getConfig().getString("worlds.void_sea", "fractured_void_sea"), World.Environment.NORMAL);
    }

    private World getOrCreateWorld(String name, World.Environment environment) {
        World existing = Bukkit.getWorld(name);
        if (existing != null) {
            return existing;
        }

        WorldCreator creator = new WorldCreator(name)
                .environment(environment)
                .type(WorldType.NORMAL)
                .generateStructures(false);

        if (name.contains("void")) {
            creator.generatorSettings("minecraft:overworld");
        }

        return creator.createWorld();
    }
}
