package com.hazerengine.core;

import com.hazerengine.armor.ArmorSet;
import com.hazerengine.bootstrap.ExampleContentLoader;
import com.hazerengine.combat.CombatEngine;
import com.hazerengine.core.config.ConfigService;
import com.hazerengine.core.module.ModuleManager;
import com.hazerengine.core.performance.AsyncTaskEngine;
import com.hazerengine.core.performance.CacheManager;
import com.hazerengine.core.performance.MemoryManager;
import com.hazerengine.core.registry.RegistryHub;
import com.hazerengine.modules.*;
import com.hazerengine.resourcepack.ResourcePackBuilder;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class HazerEnginePlugin extends JavaPlugin {
    private final RegistryHub registryHub = new RegistryHub();
    private final ModuleManager moduleManager = new ModuleManager();
    private final CacheManager cacheManager = new CacheManager();
    private final MemoryManager memoryManager = new MemoryManager();
    private final List<ArmorSet> armorSets = new ArrayList<>();
    private ConfigService configService;
    private AsyncTaskEngine asyncTaskEngine;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        configService = new ConfigService(this);
        configService.loadDefaults();
        asyncTaskEngine = new AsyncTaskEngine(this, getConfig().getInt("engine.async-threads", 4));
        registerModules();
        moduleManager.loadAll(this);
        ExampleContentLoader.load(this);
        moduleManager.enableAll(this);
        Bukkit.getPluginManager().registerEvents(new CombatEngine(this), this);

        asyncTaskEngine.runAsync(() -> {
            try {
                Path packOut = getDataFolder().toPath().resolve("resourcepack.zip");
                new ResourcePackBuilder().generate(packOut, registryHub.items.all());
                getLogger().info("Resourcepack generated: " + packOut);
            } catch (Exception ex) {
                getLogger().warning("Resourcepack generation failed: " + ex.getMessage());
            }
        });

        getLogger().info("HazerEngine loaded with modules=" + moduleManager.modules().size() +
                ", items=" + registryHub.items.size() +
                ", abilities=" + registryHub.abilities.size());
    }

    @Override
    public void onDisable() {
        moduleManager.disableAll(this);
        if (asyncTaskEngine != null) {
            asyncTaskEngine.shutdown();
        }
        memoryManager.hintGc();
    }

    private void registerModules() {
        moduleManager.register(new HazerItemsModule());
        moduleManager.register(new HazerFoodModule());
        moduleManager.register(new HazerCombatModule());
        moduleManager.register(new HazerAbilitiesModule());
        moduleManager.register(new HazerCraftingModule());
        moduleManager.register(new HazerGUIModule());
        moduleManager.register(new HazerResourcePackModule());
        moduleManager.register(new HazerNPCModule());
        moduleManager.register(new HazerQuestsModule());
        moduleManager.register(new HazerEconomyModule());
        moduleManager.register(new HazerMagicModule());
        moduleManager.register(new HazerDungeonsModule());
        moduleManager.register(new HazerMobsModule());
    }

    public RegistryHub getRegistryHub() { return registryHub; }
    public ModuleManager getModuleManager() { return moduleManager; }
    public CacheManager getCacheManager() { return cacheManager; }
    public MemoryManager getMemoryManager() { return memoryManager; }
    public List<ArmorSet> getArmorSets() { return armorSets; }
    public ConfigService getConfigService() { return configService; }
}
