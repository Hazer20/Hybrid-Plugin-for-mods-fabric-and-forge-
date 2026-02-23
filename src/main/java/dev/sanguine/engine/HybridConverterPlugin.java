package dev.sanguine.engine;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class HybridConverterPlugin extends JavaPlugin {
    private PackConversionEngine engine;

    @Override
    public void onEnable() {
        try {
            saveDefaultConfig();
            engine = new PackConversionEngine(this);
            engine.prepareDirectories();
            if (getConfig().getBoolean("converter.auto-convert-on-startup", true)) {
                engine.convertAllAsync(getServer().getConsoleSender());
            }
            getLogger().info("HybridConverter enabled.");
        } catch (Exception ex) {
            getLogger().severe("HybridConverter startup error: " + ex.getMessage());
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!"convertpack".equalsIgnoreCase(command.getName())) {
            return false;
        }
        sender.sendMessage("§7[HybridConverter] Async conversion started...");
        engine.convertAllAsync(sender);
        return true;
    }

    @Override
    public void onDisable() {
        if (engine != null) {
            engine.shutdown();
        }
    }
}
