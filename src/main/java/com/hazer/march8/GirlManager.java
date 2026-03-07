package com.hazer.march8;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Stores and manipulates the configured list of players receiving celebration rewards.
 */
public final class GirlManager {

    private final March8Plugin plugin;
    private final ConfigManager configManager;
    private final Set<String> girls;

    public GirlManager(@NotNull March8Plugin plugin, @NotNull ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.girls = new LinkedHashSet<>();
        reloadFromConfig();
    }

    public void reloadFromConfig() {
        girls.clear();
        for (String entry : configManager.getGirls()) {
            if (entry == null || entry.isBlank()) {
                continue;
            }
            girls.add(normalize(entry));
        }
    }

    public boolean addGirl(@NotNull String name) {
        String normalized = normalize(name);
        boolean added = girls.add(normalized);
        if (added) {
            persist();
            plugin.getLogger().info("Added girl profile: " + normalized);
        }
        return added;
    }

    public boolean removeGirl(@NotNull String name) {
        String normalized = normalize(name);
        boolean removed = girls.remove(normalized);
        if (removed) {
            persist();
            plugin.getLogger().info("Removed girl profile: " + normalized);
        }
        return removed;
    }

    public boolean isGirl(@NotNull String name) {
        return girls.contains(normalize(name));
    }

    public List<String> getGirls() {
        List<String> list = new ArrayList<>(girls);
        Collections.sort(list);
        return list;
    }

    public void clear() {
        girls.clear();
        persist();
    }

    public int size() {
        return girls.size();
    }

    public boolean isEmpty() {
        return girls.isEmpty();
    }

    private void persist() {
        configManager.setGirls(getGirls());
    }

    private String normalize(@NotNull String s) {
        return s.trim().toLowerCase(Locale.ROOT);
    }
}
