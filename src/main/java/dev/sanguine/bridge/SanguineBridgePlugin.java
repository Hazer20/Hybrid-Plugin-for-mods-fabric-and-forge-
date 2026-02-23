package dev.sanguine.bridge;

import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;

public class SanguineBridgePlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        saveDefaultConfig();

        String sourceName = getConfig().getString("source-datapack", "Sanguine");
        String bridgedName = getConfig().getString("output-datapack", "Sanguine_121_bridge");

        Path datapacksRoot = getServer().getWorldContainer().toPath().resolve(getConfig().getString("world-name", "world")).resolve("datapacks");
        Path sourceDatapack = datapacksRoot.resolve(sourceName);
        Path outputDatapack = datapacksRoot.resolve(bridgedName);

        DatapackBridgeService bridgeService = new DatapackBridgeService(this);

        try {
            BridgeResult result = bridgeService.bridge(sourceDatapack, outputDatapack);
            bridgeService.logResult(result);
            getLogger().info("Reload datapacks with /minecraft:reload after startup.");
        } catch (Exception exception) {
            getLogger().severe("Failed to create Sanguine bridge datapack: " + exception.getMessage());
            getServer().getPluginManager().disablePlugin(this);
        }
    }
}
