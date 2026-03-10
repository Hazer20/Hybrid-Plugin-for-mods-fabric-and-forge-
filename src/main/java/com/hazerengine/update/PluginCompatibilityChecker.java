package com.hazerengine.update;

import com.hazerengine.core.HazerEnginePlugin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.jar.JarFile;

public class PluginCompatibilityChecker {
    private final HazerEnginePlugin plugin;

    public PluginCompatibilityChecker(HazerEnginePlugin plugin) { this.plugin = plugin; }

    public void checkAll() {
        String engineVersion = plugin.getDescription().getVersion();
        for (Plugin p : Bukkit.getPluginManager().getPlugins()) {
            if (p == plugin) continue;
            String required = readRequiredVersion(p);
            if (required == null || required.isBlank()) continue;
            if (!engineVersion.startsWith(required.split("\\.")[0])) {
                plugin.getLogger().warning("[Compatibility] Plugin " + p.getName() + " requires Hazer version " + required + ", current=" + engineVersion);
            }
        }
    }

    private String readRequiredVersion(Plugin pluginInstance) {
        try {
            if (!(pluginInstance instanceof JavaPlugin jp)) return null;
            Method m = JavaPlugin.class.getDeclaredMethod("getFile");
            m.setAccessible(true);
            File file = (File) m.invoke(jp);
            try (JarFile jar = new JarFile(file)) {
                var entry = jar.getJarEntry("plugin.yml");
                if (entry == null) return null;
                try (InputStream in = jar.getInputStream(entry)) {
                    YamlConfiguration yml = YamlConfiguration.loadConfiguration(new java.io.InputStreamReader(in));
                    return yml.getString("required-hazer-version");
                }
            }
        } catch (Exception ignored) {
            return null;
        }
    }
}
