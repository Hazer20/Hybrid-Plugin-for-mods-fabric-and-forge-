package com.hazerengine.hooks;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class HookManager {
    private final List<HazerPluginHook> hooks = new ArrayList<>();

    public void discoverHooks() {
        hooks.clear();
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            if (plugin instanceof HazerPluginHook hook) {
                hooks.add(hook);
            }
        }
    }

    public void callLoad() { hooks.forEach(HazerPluginHook::onHazerLoad); }
    public void callEnable() { hooks.forEach(HazerPluginHook::onHazerEnable); }
    public void callDisable() { hooks.forEach(HazerPluginHook::onHazerDisable); }
    public int size() { return hooks.size(); }
}
