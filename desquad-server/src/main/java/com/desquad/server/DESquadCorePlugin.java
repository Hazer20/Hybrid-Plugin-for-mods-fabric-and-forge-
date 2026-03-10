package com.desquad.server;

import com.desquad.api.DESquadAPI;
import com.desquad.api.ServiceContainer;
import com.desquad.engine.blocks.ArcaneForgeBlock;
import com.desquad.engine.blocks.InMemoryBlockRegistry;
import com.desquad.engine.dialogue.SimpleDialogueService;
import com.desquad.engine.economy.InMemoryEconomyService;
import com.desquad.engine.gui.SimpleGuiService;
import com.desquad.engine.items.DragonSwordItem;
import com.desquad.engine.items.InMemoryItemRegistry;
import com.desquad.engine.mobs.InMemoryMobRegistry;
import com.desquad.engine.mobs.StoneGuardianMob;
import com.desquad.engine.models.InMemoryModelService;
import com.desquad.engine.module.ModuleLoader;
import com.desquad.engine.npc.InMemoryNPCManager;
import com.desquad.engine.performance.AsyncExecutionEngine;
import com.desquad.engine.tick.DesquadTickLoop;
import com.desquad.server.commands.BalanceCommand;
import com.desquad.server.commands.DesquadCommand;
import com.desquad.server.commands.EconomyCommand;
import com.desquad.server.commands.NpcCommand;
import com.desquad.server.commands.PayCommand;
import com.desquad.server.runtime.ServerTickRuntime;
import java.util.Objects;
import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;

/** Bootstrap-слой DESquadCore. */
public final class DESquadCorePlugin extends JavaPlugin {
    private ModuleLoader moduleLoader;
    private AsyncExecutionEngine performanceEngine;
    private DesquadTickLoop tickLoop;
    private ServerTickRuntime tickRuntime;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        var items = new InMemoryItemRegistry();
        items.register(new DragonSwordItem());
        var npcs = new InMemoryNPCManager();
        var economy = new InMemoryEconomyService();
        var gui = new SimpleGuiService();
        var dialogue = new SimpleDialogueService();
        var models = new InMemoryModelService();
        var blocks = new InMemoryBlockRegistry();
        blocks.register(new ArcaneForgeBlock());
        var mobs = new InMemoryMobRegistry();
        mobs.register(new StoneGuardianMob());

        int workers = Math.max(2, getConfig().getInt("performance.worker-threads", Runtime.getRuntime().availableProcessors()));
        performanceEngine = new AsyncExecutionEngine(workers);

        moduleLoader = new ModuleLoader(getLogger());
        moduleLoader.discoverAndEnableAll();

        DESquadAPI.bootstrap(new ServiceContainer(items, npcs, economy, gui, dialogue, models, blocks, mobs, moduleLoader, performanceEngine));

        tickLoop = new DesquadTickLoop(getLogger());
        tickRuntime = new ServerTickRuntime(this, tickLoop);
        tickRuntime.registerDefaults();
        tickRuntime.start();

        Objects.requireNonNull(getCommand("desquad")).setExecutor(new DesquadCommand(tickLoop));
        Objects.requireNonNull(getCommand("npc")).setExecutor(new NpcCommand());
        Objects.requireNonNull(getCommand("economy")).setExecutor(new EconomyCommand());
        Objects.requireNonNull(getCommand("balance")).setExecutor(new BalanceCommand());
        Objects.requireNonNull(getCommand("pay")).setExecutor(new PayCommand());

        Logger.getLogger("DESquadCore").info("DESquadCore enabled for MC 1.21.8");
    }

    @Override
    public void onDisable() {
        if (tickRuntime != null) {
            tickRuntime.stop();
        }
        if (moduleLoader != null) {
            moduleLoader.disableAll();
        }
        if (performanceEngine != null) {
            performanceEngine.shutdown();
        }
    }
}
