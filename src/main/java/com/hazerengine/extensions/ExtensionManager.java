package com.hazerengine.extensions;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class ExtensionManager {
    public int discoverAndRegister() {
        int count = 0;
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            if (plugin instanceof HazerExtension extension) {
                extension.register();
                count++;
            }
        }
        return count;
    }
}
