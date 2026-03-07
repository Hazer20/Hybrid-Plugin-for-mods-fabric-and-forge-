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

    public void preloadEventWorlds() {
        // Создаем миры один раз на старте, чтобы не блокировать сервер в PlayerMoveEvent
        getOrCreateBrokenOverworld();
        getOrCreateCyberDimension();
        getOrCreateVoidSea();
    }

    public World getOrCreateBrokenOverworld() {
        return getOrCreateWorld(plugin.getConfig().getString("миры.сломанный_оверворлд", "fractured_broken_overworld"), World.Environment.NORMAL);
    }

    public World getOrCreateCyberDimension() {
        return getOrCreateWorld(plugin.getConfig().getString("миры.кибер_измерение", "fractured_cyber_dimension"), World.Environment.THE_END);
    }

    public World getOrCreateVoidSea() {
        return getOrCreateWorld(plugin.getConfig().getString("миры.море_пустоты", "fractured_void_sea"), World.Environment.NORMAL);
    }

    public World getLoadedBrokenOverworldOrFallback() {
        return getLoadedWorldOrFallback(plugin.getConfig().getString("миры.сломанный_оверворлд", "fractured_broken_overworld"));
    }

    public World getLoadedCyberDimensionOrFallback() {
        return getLoadedWorldOrFallback(plugin.getConfig().getString("миры.кибер_измерение", "fractured_cyber_dimension"));
    }

    private World getLoadedWorldOrFallback(String name) {
        World world = Bukkit.getWorld(name);
        return world != null ? world : Bukkit.getWorlds().getFirst();
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
