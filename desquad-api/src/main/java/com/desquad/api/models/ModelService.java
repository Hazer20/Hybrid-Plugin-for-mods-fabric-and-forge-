package com.desquad.api.models;

public interface ModelService {
    void registerModel(String modelId, int customModelData);
    int resolveModelData(String modelId);
}
