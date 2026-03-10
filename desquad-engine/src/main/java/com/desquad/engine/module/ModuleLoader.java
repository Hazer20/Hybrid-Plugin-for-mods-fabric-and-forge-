package com.desquad.engine.module;

import com.desquad.api.modules.ModuleManager;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Загрузчик и рантайм-менеджер модулей.
 * Поддерживает включение/отключение модулей без перезапуска.
 */
public final class ModuleLoader implements ModuleManager {
    private final Logger logger;
    private final Map<String, Module> discovered = new LinkedHashMap<>();
    private final Set<String> enabled = new LinkedHashSet<>();

    public ModuleLoader(Logger logger) {
        this.logger = logger;
    }

    public void discoverAndEnableAll() {
        reload();
    }

    @Override
    public Set<String> knownModules() {
        return Set.copyOf(discovered.keySet());
    }

    @Override
    public Set<String> enabledModules() {
        return Set.copyOf(enabled);
    }

    @Override
    public boolean enable(String moduleId) {
        Module module = discovered.get(moduleId);
        if (module == null || enabled.contains(moduleId)) {
            return false;
        }
        module.enable();
        enabled.add(moduleId);
        logger.info("Module enabled: " + moduleId);
        return true;
    }

    @Override
    public boolean disable(String moduleId) {
        Module module = discovered.get(moduleId);
        if (module == null || !enabled.contains(moduleId)) {
            return false;
        }
        if (!module.supportsHotReload()) {
            return false;
        }
        module.disable();
        enabled.remove(moduleId);
        logger.info("Module disabled: " + moduleId);
        return true;
    }

    @Override
    public void reload() {
        disableAll();
        discovered.clear();
        ServiceLoader.load(Module.class).forEach(module -> discovered.put(module.id(), module));
        discovered.keySet().forEach(this::enable);
    }

    public void disableAll() {
        Collection<String> snapshot = Set.copyOf(enabled);
        snapshot.forEach(this::disable);
    }
}
