package dev.hazer.universe;

import dev.hazer.universe.commands.UniverseCommand;
import dev.hazer.universe.effects.PlayerInstabilityManager;
import dev.hazer.universe.events.WorldEventListener;
import dev.hazer.universe.lore.LoreManager;
import dev.hazer.universe.portals.FissureManager;
import dev.hazer.universe.systems.ArgSignalService;
import dev.hazer.universe.systems.EventPhaseManager;
import dev.hazer.universe.systems.EventScheduler;
import dev.hazer.universe.worlds.WorldManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class FracturedUniverse extends JavaPlugin {
    private EventPhaseManager phaseManager;
    private EventScheduler eventScheduler;
    private FissureManager fissureManager;
    private PlayerInstabilityManager instabilityManager;
    private WorldManager worldManager;
    private LoreManager loreManager;
    private ArgSignalService argSignalService;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.phaseManager = new EventPhaseManager(this);
        this.worldManager = new WorldManager(this);
        this.loreManager = new LoreManager(this);
        this.fissureManager = new FissureManager(this, phaseManager, worldManager);
        this.instabilityManager = new PlayerInstabilityManager(this, phaseManager);
        this.argSignalService = new ArgSignalService(this, phaseManager);
        this.eventScheduler = new EventScheduler(this, phaseManager, fissureManager, instabilityManager, argSignalService);

        getServer().getPluginManager().registerEvents(
                new WorldEventListener(this, phaseManager, fissureManager, instabilityManager),
                this
        );

        UniverseCommand command = new UniverseCommand(this, phaseManager, eventScheduler, fissureManager, instabilityManager, worldManager, loreManager);
        getCommand("universe").setExecutor(command);
        getCommand("universe").setTabCompleter(command);

        loreManager.load();
        eventScheduler.bootstrap();
        getLogger().info("FracturedUniverse has awakened.");
    }

    @Override
    public void onDisable() {
        eventScheduler.shutdown();
        loreManager.save();
        fissureManager.cleanup();
        instabilityManager.cleanup();
        getLogger().info("FracturedUniverse has gone silent.");
    }
}
