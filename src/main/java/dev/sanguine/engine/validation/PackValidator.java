package dev.sanguine.engine.validation;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.sanguine.engine.log.ConversionLogger;
import dev.sanguine.engine.report.ConversionReport;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class PackValidator {
    private final ConversionLogger logger;

    public PackValidator(ConversionLogger logger) {
        this.logger = logger;
    }

    public void validate(Path root, ConversionReport report) throws IOException {
        try (var paths = Files.walk(root)) {
            paths.filter(Files::isRegularFile).forEach(file -> {
                try {
                    validateFile(root, file, report);
                } catch (Exception ex) {
                    report.incompatible("Validation error: " + root.relativize(file) + " -> " + ex.getMessage());
                    logger.warn("Validation error in " + file + ": " + ex.getMessage());
                }
            });
        }
    }

    private void validateFile(Path root, Path file, ConversionReport report) throws IOException {
        String path = root.relativize(file).toString().replace('\\', '/');

        if (path.endsWith(".json") || path.endsWith(".mcmeta")) {
            JsonObject json;
            try {
                json = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject();
            } catch (Exception ex) {
                report.incompatible("Invalid JSON: " + path);
                return;
            }

            if (path.contains("models/") && json.has("parent") && json.get("parent").isJsonPrimitive()) {
                String parent = json.get("parent").getAsString();
                if (!parent.contains(":")) {
                    report.manual("Model parent without namespace: " + path + " -> " + parent);
                }
            }

            if (path.endsWith("sounds.json") && !json.keySet().isEmpty()) {
                for (String key : json.keySet()) {
                    if (!json.get(key).isJsonObject()) {
                        report.incompatible("sounds.json entry is not object: " + key);
                    }
                }
            }
        }

        if (path.contains("textures/") && !path.endsWith(".png") && !path.endsWith(".mcmeta")) {
            report.manual("Unexpected texture extension: " + path);
        }

        if (path.contains("shaders/") && !path.endsWith(".json") && !path.endsWith(".vsh") && !path.endsWith(".fsh")) {
            report.manual("Potential shader incompatibility: " + path);
        }
    }
}
