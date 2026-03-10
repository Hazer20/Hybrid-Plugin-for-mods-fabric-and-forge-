package com.hazerengine.api;

import com.hazerengine.gui.Menu;

import java.util.Optional;

/**
 * Public GUI API.
 */
public interface GUIAPI {
    void register(Menu menu);
    Optional<Menu> get(String id);
}
