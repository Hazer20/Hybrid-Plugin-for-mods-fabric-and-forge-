package com.hazerengine.hooks;

/**
 * Implement this interface in third-party plugins to receive Hazer lifecycle callbacks.
 */
public interface HazerPluginHook {
    void onHazerLoad();
    void onHazerEnable();
    void onHazerDisable();
}
