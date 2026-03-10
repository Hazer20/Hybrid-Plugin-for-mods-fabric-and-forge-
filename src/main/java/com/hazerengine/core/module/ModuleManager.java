package com.hazerengine.core.module;

import com.hazerengine.core.HazerEnginePlugin;
import com.hazerengine.core.api.EngineModule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ModuleManager {
    private final List<EngineModule> modules = new ArrayList<>();

    public void register(EngineModule module) {
        modules.add(module);
    }

    public void loadAll(HazerEnginePlugin plugin) {
        modules.forEach(m -> m.onLoad(plugin));
    }

    public void enableAll(HazerEnginePlugin plugin) {
        modules.forEach(m -> m.onEnable(plugin));
    }

    public void disableAll(HazerEnginePlugin plugin) {
        modules.forEach(m -> m.onDisable(plugin));
    }

    public List<EngineModule> modules() {
        return Collections.unmodifiableList(modules);
    }
}
