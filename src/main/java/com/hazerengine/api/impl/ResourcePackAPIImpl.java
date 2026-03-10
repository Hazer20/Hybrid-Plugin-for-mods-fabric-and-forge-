package com.hazerengine.api.impl;

import com.hazerengine.api.ResourcePackAPI;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ResourcePackAPIImpl implements ResourcePackAPI {
    private final Map<String, File> textures = new ConcurrentHashMap<>();
    private final Map<String, File> models = new ConcurrentHashMap<>();
    private final Map<String, File> sounds = new ConcurrentHashMap<>();

    @Override
    public void addTexture(String key, File file) { textures.put(key, file); }

    @Override
    public void addModel(String key, File file) { models.put(key, file); }

    @Override
    public void addSound(String key, File file) { sounds.put(key, file); }

    @Override
    public Map<String, File> textures() { return Collections.unmodifiableMap(textures); }

    @Override
    public Map<String, File> models() { return Collections.unmodifiableMap(models); }

    @Override
    public Map<String, File> sounds() { return Collections.unmodifiableMap(sounds); }
}
