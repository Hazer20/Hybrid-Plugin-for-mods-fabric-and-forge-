package com.hazerengine.core;

import com.hazerengine.api.*;
import com.hazerengine.api.impl.*;
import com.hazerengine.armor.ArmorSet;
import com.hazerengine.bootstrap.ExampleContentLoader;
import com.hazerengine.combat.CombatEngine;
import com.hazerengine.core.config.ConfigService;
import com.hazerengine.core.module.ModuleAutoLoader;
import com.hazerengine.core.module.ModuleManager;
import com.hazerengine.core.performance.AsyncTaskEngine;
import com.hazerengine.core.performance.CacheManager;
import com.hazerengine.core.performance.MemoryManager;
import com.hazerengine.core.registry.RegistryHub;
import com.hazerengine.economy.EconomyAPI;
import com.hazerengine.extensions.ExtensionManager;
import com.hazerengine.hooks.HookManager;
import com.hazerengine.resourcepack.ResourcePackBuilder;
import com.hazerengine.update.PluginCompatibilityChecker;
import com.hazerengine.update.UpdateManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class HazerEnginePlugin extends JavaPlugin {
    private final RegistryHub registryHub = new RegistryHub();
    private final ModuleManager moduleManager = new ModuleManager();
    private final CacheManager cacheManager = new CacheManager();
    private final MemoryManager memoryManager = new MemoryManager();
    private final HookManager hookManager = new HookManager();
    private final ExtensionManager extensionManager = new ExtensionManager();
    private final List<ArmorSet> armorSets = new ArrayList<>();

    private ConfigService configService;
    private AsyncTaskEngine asyncTaskEngine;
    private UpdateManager updateManager;

    private ItemAPI itemAPI;
    private AbilityAPI abilityAPI;
    private RecipeAPI recipeAPI;
    private GUIAPI guiAPI;
    private com.hazerengine.api.EconomyAPI economyAPI;
    private NPCAPI npcAPI;
    private ResourcePackAPI resourcePackAPI;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        configService = new ConfigService(this);
        configService.loadDefaults();
        asyncTaskEngine = new AsyncTaskEngine(this, getConfig().getInt("engine.async-threads", 4));

        updateManager = new UpdateManager(this);
        updateManager.runStartupChecks();

        autoLoadModules();
        moduleManager.loadAll(this);

        setupPublicServices();
        hookManager.discoverHooks();
        hookManager.callLoad();

        ExampleContentLoader.load(this);
        extensionManager.discoverAndRegister();

        moduleManager.enableAll(this);
        hookManager.callEnable();

        Bukkit.getPluginManager().registerEvents(new CombatEngine(this), this);
        new PluginCompatibilityChecker(this).checkAll();

        asyncTaskEngine.runAsync(() -> {
            try {
                Path packOut = getDataFolder().toPath().resolve("resourcepack.zip");
                new ResourcePackBuilder().generate(packOut, registryHub.items.all(), resourcePackAPI);
                getLogger().info("Resourcepack generated: " + packOut);
            } catch (Exception ex) {
                getLogger().warning("Resourcepack generation failed: " + ex.getMessage());
            }
        });

        getLogger().info("HazerEngine API ready. hooks=" + hookManager.size() + ", modules=" + moduleManager.modules().size());
    }

    @Override
    public void onDisable() {
        hookManager.callDisable();
        moduleManager.disableAll(this);
        Bukkit.getServicesManager().unregisterAll(this);
        if (asyncTaskEngine != null) asyncTaskEngine.shutdown();
        memoryManager.hintGc();
    }

    private void autoLoadModules() {
        ModuleAutoLoader loader = new ModuleAutoLoader();
        loader.discover(this, "com.hazerengine.modules").forEach(moduleManager::register);
    }

    private void setupPublicServices() {
        itemAPI = new ItemAPIImpl(registryHub.items);
        abilityAPI = new AbilityAPIImpl(registryHub.abilities);
        recipeAPI = new RecipeAPIImpl(registryHub.recipes);
        guiAPI = new GUIAPIImpl();
        economyAPI = new EconomyAPI();
        npcAPI = new NPCAPIImpl(registryHub.npcs);
        resourcePackAPI = new ResourcePackAPIImpl();

        getServer().getServicesManager().register(ItemAPI.class, itemAPI, this, ServicePriority.Normal);
        getServer().getServicesManager().register(AbilityAPI.class, abilityAPI, this, ServicePriority.Normal);
        getServer().getServicesManager().register(RecipeAPI.class, recipeAPI, this, ServicePriority.Normal);
        getServer().getServicesManager().register(GUIAPI.class, guiAPI, this, ServicePriority.Normal);
        getServer().getServicesManager().register(com.hazerengine.api.EconomyAPI.class, economyAPI, this, ServicePriority.Normal);
        getServer().getServicesManager().register(NPCAPI.class, npcAPI, this, ServicePriority.Normal);
        getServer().getServicesManager().register(ResourcePackAPI.class, resourcePackAPI, this, ServicePriority.Normal);
    }

    public RegistryHub getRegistryHub() { return registryHub; }
    public ModuleManager getModuleManager() { return moduleManager; }
    public CacheManager getCacheManager() { return cacheManager; }
    public MemoryManager getMemoryManager() { return memoryManager; }
    public List<ArmorSet> getArmorSets() { return armorSets; }
    public ConfigService getConfigService() { return configService; }
}
