package dev.sanguine.engine.translation;

import com.google.gson.JsonObject;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Local AI-like rule engine (offline, deterministic) for legacy key migration.
 */
public class LocalAiTranslationModule {
    private final Map<String, String> keyReplacements = new LinkedHashMap<>();

    public LocalAiTranslationModule() {
        keyReplacements.put("location_minecraft:predicate", "location_predicate");
        keyReplacements.put("input_minecraft:predicate", "input_predicate");
        keyReplacements.put("surface_minecraft:structures", "structures");
        keyReplacements.put("minecraft:structures", "structures");
    }

    public String translateRawText(String text) {
        String out = text;
        for (Map.Entry<String, String> entry : keyReplacements.entrySet()) {
            out = out.replace(entry.getKey(), entry.getValue());
        }
        return out;
    }

    public void translateJson(JsonObject root) {
        for (Map.Entry<String, String> entry : keyReplacements.entrySet()) {
            if (root.has(entry.getKey())) {
                root.add(entry.getValue(), root.remove(entry.getKey()));
            }
        }
    }
}
