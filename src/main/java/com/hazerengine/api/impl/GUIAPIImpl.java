package com.hazerengine.api.impl;

import com.hazerengine.api.GUIAPI;
import com.hazerengine.gui.Menu;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class GUIAPIImpl implements GUIAPI {
    private final Map<String, Menu> menuCache = new ConcurrentHashMap<>();

    @Override
    public void register(Menu menu) { menuCache.put(menu.id(), menu); }

    @Override
    public Optional<Menu> get(String id) { return Optional.ofNullable(menuCache.get(id)); }
}
