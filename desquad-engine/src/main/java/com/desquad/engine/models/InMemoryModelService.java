package com.desquad.engine.models;

import com.desquad.api.models.ModelService;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryModelService implements ModelService {
    private final Map<String, Integer> models = new ConcurrentHashMap<>();

    @Override
    public void registerModel(String modelId, int customModelData) {
        models.put(modelId, customModelData);
    }

    @Override
    public int resolveModelData(String modelId) {
        return models.getOrDefault(modelId, 0);
    }
}
