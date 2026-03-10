package com.hazerengine.core.module;

import com.hazerengine.core.api.EngineModule;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ModuleAutoLoader {
    public List<EngineModule> discover(JavaPlugin plugin, String packageName) {
        List<EngineModule> modules = new ArrayList<>();
        String path = packageName.replace('.', '/');
        try {
            URI location = plugin.getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
            File source = new File(location);
            if (source.isFile()) {
                try (JarFile jar = new JarFile(source)) {
                    Enumeration<JarEntry> entries = jar.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry e = entries.nextElement();
                        String name = e.getName();
                        if (!name.startsWith(path) || !name.endsWith(".class")) continue;
                        addIfModule(modules, name);
                    }
                }
            } else if (source.isDirectory()) {
                File root = new File(source, path);
                if (root.exists()) {
                    walkDirectory(modules, source, root);
                }
            }
        } catch (Exception ex) {
            plugin.getLogger().warning("Module auto-discovery failed: " + ex.getMessage());
        }
        return modules;
    }

    private void walkDirectory(List<EngineModule> modules, File base, File current) {
        File[] files = current.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isDirectory()) {
                walkDirectory(modules, base, f);
            } else if (f.isFile() && f.getName().endsWith(".class")) {
                String rel = base.toPath().relativize(f.toPath()).toString().replace(File.separatorChar, '/');
                addIfModule(modules, rel);
            }
        }
    }

    private void addIfModule(List<EngineModule> modules, String classPath) {
        String clazz = classPath.replace('/', '.').replace(".class", "");
        if (clazz.contains("$")) return;
        try {
            Class<?> loaded = Class.forName(clazz);
            if (EngineModule.class.isAssignableFrom(loaded) && !loaded.isInterface()) {
                modules.add((EngineModule) loaded.getDeclaredConstructor().newInstance());
            }
        } catch (Exception ignored) {
        }
    }
}
