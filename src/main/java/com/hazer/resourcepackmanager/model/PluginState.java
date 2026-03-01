package com.hazer.resourcepackmanager.model;

/**
 * Represents the high-level lifecycle and operating state of the plugin.
 * <p>
 * This enum is intentionally explicit so that logs, commands, and listeners can all
 * reason about what the plugin is currently doing without relying on nullable flags.
 * Using a dedicated enum instead of booleans keeps the code readable and extensible.
 */
public enum PluginState {
    /**
     * Plugin object is created but not yet initialized.
     */
    BOOTSTRAPPING,

    /**
     * Startup initialization is in progress (config, folders, scanning).
     */
    INITIALIZING,

    /**
     * Plugin is fully functional and has a selected active pack.
     */
    ACTIVE,

    /**
     * Plugin is functional but currently has no valid pack available.
     */
    ACTIVE_WITHOUT_PACK,

    /**
     * Plugin encountered an internal issue and switched to degraded mode.
     */
    ERROR,

    /**
     * Plugin disable sequence is running.
     */
    SHUTTING_DOWN,

    /**
     * Plugin is disabled.
     */
    DISABLED
}
