package dev.sanguine.bridge;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class SanguineBridgePlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        saveDefaultConfig();

        String sourceName = getConfig().getString("source-datapack", "Sanguine");
        String bridgedName = getConfig().getString("output-datapack", "Sanguine_121_bridge");

        Path datapacksRoot = getServer().getWorldContainer().toPath()
            .resolve(getConfig().getString("world-name", "world"))
            .resolve("datapacks");
        Path sourceDatapack = datapacksRoot.resolve(sourceName);
        Path outputDatapack = datapacksRoot.resolve(bridgedName);

        DatapackBridgeService bridgeService = new DatapackBridgeService(this, readExtraReplacements());

        try {
            BridgeResult result = bridgeService.bridge(sourceDatapack, outputDatapack);
            bridgeService.logResult(result);
            getLogger().info("Reload datapacks with /minecraft:reload after startup.");
        } catch (Exception exception) {
            getLogger().severe("Failed to create Sanguine bridge datapack: " + exception.getMessage());
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    private Map<String, String> readExtraReplacements() {
        Map<String, String> replacements = new LinkedHashMap<>();
        ConfigurationSection section = getConfig().getConfigurationSection("extra-replacements");
        if (section == null) {
            return replacements;
        }

        for (String key : section.getKeys(false)) {
            replacements.put(key, section.getString(key, key));
        }
        return replacements;
    }
}
