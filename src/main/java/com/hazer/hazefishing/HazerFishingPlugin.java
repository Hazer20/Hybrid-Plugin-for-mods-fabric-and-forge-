package com.hazer.hazefishing;

import com.hazer.hazefishing.api.HazeFishingAPI;
import com.hazer.hazefishing.command.AdminCommandManager;
import com.hazer.hazefishing.command.FishCommand;
import com.hazer.hazefishing.command.HFAdminCommand;
import com.hazer.hazefishing.data.DatabaseManager;
import com.hazer.hazefishing.gui.AdminPanel;
import com.hazer.hazefishing.listener.AdminPanelListener;
import com.hazer.hazefishing.listener.FishingListener;
import com.hazer.hazefishing.manager.*;
import com.hazer.hazefishing.security.IntegrityValidator;
import com.hazer.hazefishing.security.SecurityManager;
import com.hazer.hazefishing.util.GradientEngine;
import com.hazer.hazefishing.util.ParticleEngine;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class HazerFishingPlugin extends JavaPlugin {

    private DatabaseManager databaseManager;
    private NFTRodManager nftRodManager;
    private NFTFishManager nftFishManager;
    private RGBAnimationManager rgbAnimationManager;
    private EconomyManager economyManager;
    private AuctionManager auctionManager;
    private SecurityManager securityManager;
    private IntegrityValidator integrityValidator;
    private GradientEngine gradientEngine;
    private ParticleEngine particleEngine;
    private AnimationScheduler animationScheduler;
    private AdminCommandManager adminCommandManager;
    private AdminAuditLogger adminAuditLogger;
    private DiscordWebhookManager discordWebhookManager;
    private EventManager eventManager;
    private AdminPanel adminPanel;
    private HazeFishingAPI api;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResource("messages.yml", false);

        this.integrityValidator = new IntegrityValidator();
        this.gradientEngine = new GradientEngine();
        this.particleEngine = new ParticleEngine();
        this.databaseManager = new DatabaseManager(this);
        this.securityManager = new SecurityManager(this);
        this.nftRodManager = new NFTRodManager(this, integrityValidator);
        this.nftFishManager = new NFTFishManager(this);
        this.rgbAnimationManager = new RGBAnimationManager(this, gradientEngine, particleEngine);
        this.economyManager = new EconomyManager(this);
        this.auctionManager = new AuctionManager();
        this.animationScheduler = new AnimationScheduler(this);
        this.adminCommandManager = new AdminCommandManager(this);
        this.adminAuditLogger = new AdminAuditLogger(this);
        this.discordWebhookManager = new DiscordWebhookManager(this);
        this.eventManager = new EventManager(this);
        this.adminPanel = new AdminPanel();
        this.api = new HazeFishingAPI(this);

        databaseManager.initialize();
        economyManager.initialize();
        animationScheduler.startPerformanceSampler();

        getServer().getPluginManager().registerEvents(new FishingListener(this), this);
        getServer().getPluginManager().registerEvents(new AdminPanelListener(), this);

        registerCommands();
        getLogger().info("HazerFishing 4.0 NFT EDITION enabled.");
    }

    private void registerCommands() {
        FishCommand fishCommand = new FishCommand(this);
        PluginCommand fish = getCommand("fish");
        if (fish != null) {
            fish.setExecutor(fishCommand);
            fish.setTabCompleter(fishCommand);
        }

        HFAdminCommand adminCommand = new HFAdminCommand(this);
        for (String commandName : new String[]{"hazefish", "hfadmin", "hf"}) {
            PluginCommand cmd = getCommand(commandName);
            if (cmd != null) {
                cmd.setExecutor(adminCommand);
                cmd.setTabCompleter(adminCommand);
            }
        }
    }

    @Override
    public void onDisable() {
        databaseManager.shutdown();
    }

    public NFTRodManager getNftRodManager() { return nftRodManager; }
    public NFTFishManager getNftFishManager() { return nftFishManager; }
    public RGBAnimationManager getRgbAnimationManager() { return rgbAnimationManager; }
    public EconomyManager getEconomyManager() { return economyManager; }
    public AuctionManager getAuctionManager() { return auctionManager; }
    public SecurityManager getSecurityManager() { return securityManager; }
    public AnimationScheduler getAnimationScheduler() { return animationScheduler; }
    public AdminCommandManager getAdminCommandManager() { return adminCommandManager; }
    public AdminAuditLogger getAdminAuditLogger() { return adminAuditLogger; }
    public DiscordWebhookManager getDiscordWebhookManager() { return discordWebhookManager; }
    public EventManager getEventManager() { return eventManager; }
    public AdminPanel getAdminPanel() { return adminPanel; }
    public HazeFishingAPI getApi() { return api; }
}
