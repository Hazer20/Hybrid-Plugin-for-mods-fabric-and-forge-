package dev.hazer.universe;

import dev.hazer.universe.bosses.FinalBossManager;
import dev.hazer.universe.commands.UniverseCommand;
import dev.hazer.universe.effects.PlayerInstabilityManager;
import dev.hazer.universe.events.WorldEventListener;
import dev.hazer.universe.lore.LoreManager;
import dev.hazer.universe.portals.FissureManager;
import dev.hazer.universe.portals.PortalRitualManager;
import dev.hazer.universe.systems.*;
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
    private UniverseStabilityManager stabilityManager;
    private ArgEventManager argEventManager;
    private PortalRitualManager portalRitualManager;
    private ChunkCollapseManager chunkCollapseManager;
    private FinalBossManager finalBossManager;
    private ModelRegistryService modelRegistryService;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.phaseManager = new EventPhaseManager(this);
        this.worldManager = new WorldManager(this);
        this.loreManager = new LoreManager(this);
        this.stabilityManager = new UniverseStabilityManager(this);
        this.fissureManager = new FissureManager(this, phaseManager, worldManager);
        this.instabilityManager = new PlayerInstabilityManager(this, phaseManager);
        this.argSignalService = new ArgSignalService(this, phaseManager);
        this.argEventManager = new ArgEventManager(this);
        this.portalRitualManager = new PortalRitualManager(this);
        this.chunkCollapseManager = new ChunkCollapseManager(this);
        this.finalBossManager = new FinalBossManager(this);
        this.modelRegistryService = new ModelRegistryService(this);

        if (getConfig().getBoolean("миры.предзагрузка_на_старте", false)) {
            worldManager.preloadEventWorlds();
        }

        this.eventScheduler = new EventScheduler(this, phaseManager, fissureManager, instabilityManager, argSignalService, stabilityManager, argEventManager, chunkCollapseManager);

        getServer().getPluginManager().registerEvents(
                new WorldEventListener(this, phaseManager, fissureManager, instabilityManager, portalRitualManager, stabilityManager),
                this
        );

        UniverseCommand command = new UniverseCommand(this, phaseManager, eventScheduler, fissureManager, instabilityManager,
                worldManager, loreManager, stabilityManager, argEventManager, portalRitualManager, finalBossManager, modelRegistryService);
        getCommand("universe").setExecutor(command);
        getCommand("universe").setTabCompleter(command);

        loreManager.load();
        modelRegistryService.scanDatapackModels();
        eventScheduler.bootstrap();
        getLogger().info("FracturedUniverse запущен.");
    }

    @Override
    public void onDisable() {
        eventScheduler.shutdown();
        loreManager.save();
        fissureManager.cleanup();
        instabilityManager.cleanup();
        finalBossManager.finishSeason();
        getLogger().info("FracturedUniverse остановлен.");
    }
}
