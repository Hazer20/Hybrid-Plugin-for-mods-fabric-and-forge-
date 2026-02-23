package dev.sanguine.bridge;

import java.util.Map;

public class McFunctionTransformer {
    private final Map<String, String> replacements;

    public McFunctionTransformer(Map<String, String> replacements) {
        this.replacements = replacements;
    }

    public String transformLine(String line) {
        if (line.isBlank() || line.startsWith("#")) {
            return line;
        }

        String transformed = line;
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            transformed = transformed.replace(entry.getKey(), entry.getValue());
        }
        return transformed;
    }
}
