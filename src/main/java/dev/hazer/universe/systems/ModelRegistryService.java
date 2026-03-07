package dev.hazer.universe.systems;

import dev.hazer.universe.FracturedUniverse;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ModelRegistryService {
    private final FracturedUniverse plugin;
    private final List<String> detectedModels = new ArrayList<>();

    public ModelRegistryService(FracturedUniverse plugin) {
        this.plugin = plugin;
    }

    public void scanDatapackModels() {
        detectedModels.clear();
        File folder = new File(plugin.getDataFolder(), "datapack/data/fractured_universe/models");
        if (!folder.exists()) {
            folder.mkdirs();
            return;
        }
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".json") || name.endsWith(".mcmeta"));
        if (files == null) {
            return;
        }
        for (File file : files) {
            detectedModels.add(file.getName());
        }
        plugin.getLogger().info("Обнаружено кастомных моделей: " + detectedModels.size());
    }

    public List<String> getDetectedModels() {
        return List.copyOf(detectedModels);
    }
}
