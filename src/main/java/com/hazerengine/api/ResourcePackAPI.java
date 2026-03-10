package com.hazerengine.api;

import java.io.File;
import java.util.Map;

/**
 * Public resource-pack extension API.
 */
public interface ResourcePackAPI {
    void addTexture(String key, File file);
    void addModel(String key, File file);
    void addSound(String key, File file);
    Map<String, File> textures();
    Map<String, File> models();
    Map<String, File> sounds();
}
