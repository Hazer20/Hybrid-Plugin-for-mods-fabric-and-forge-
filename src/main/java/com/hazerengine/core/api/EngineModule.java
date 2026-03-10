package com.hazerengine.core.api;

import com.hazerengine.core.HazerEnginePlugin;

public interface EngineModule {
    String getName();
    void onLoad(HazerEnginePlugin plugin);
    void onEnable(HazerEnginePlugin plugin);
    void onDisable(HazerEnginePlugin plugin);
}
