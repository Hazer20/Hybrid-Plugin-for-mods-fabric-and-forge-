package dev.sanguine.engine.resource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.sanguine.engine.log.ConversionLogger;
import dev.sanguine.engine.pack.ArchiveUtils;
import dev.sanguine.engine.report.ConversionReport;
import dev.sanguine.engine.translation.VersionTranslationLayer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CompletableFuture;

public class ResourcePackConverter {
    private static final int RESOURCEPACK_1218_FORMAT = 46;

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final VersionTranslationLayer translationLayer = new VersionTranslationLayer();
    private final ConversionLogger logger;
    private final ConversionReport report;

    public ResourcePackConverter(ConversionLogger logger, ConversionReport report) {
        this.logger = logger;
        this.report = report;
    }

    public void convert(Path sourceRoot, Path outRoot) throws IOException {
        ArchiveUtils.copyTree(sourceRoot, outRoot);
        Path itemsDir = outRoot.resolve("assets/minecraft/items");
        Path unsupportedDir = outRoot.resolve("unsupported");
        Files.createDirectories(itemsDir);
        Files.createDirectories(unsupportedDir);

        try (var paths = Files.walk(outRoot)) {
            paths.filter(Files::isRegularFile).forEach(file -> {
                try {
                    String name = file.getFileName().toString();
                    if (name.equals("pack.mcmeta")) {
                        rewritePackMeta(file, RESOURCEPACK_1218_FORMAT);
                    } else if (name.endsWith(".json")) {
                        processJson(file, itemsDir, unsupportedDir);
                    }
                } catch (Exception e) {
                    report.skipped("resourcepack:" + outRoot.relativize(file));
                    logger.warn("Resource pack file skipped: " + file + " -> " + e.getMessage());
                }
            });
        }

        logger.info("Resource pack transpilation completed for " + outRoot);
    }

    private void processJson(Path file, Path itemsDir, Path unsupportedDir) throws IOException {
        JsonObject root = safeParseJson(file);
        if (root == null) {
            Files.move(file, unsupportedDir.resolve(file.getFileName().toString()), StandardCopyOption.REPLACE_EXISTING);
            report.incompatible("invalid-resource-json:" + file);
            return;
        }

        String path = file.toString().replace('\\', '/');
        if (path.contains("assets/minecraft/models/item/")) {
            translationLayer.translateModel(root);
            convertItemModel(root, file, itemsDir);
        }
        if (path.contains("blockstates/")) {
            translationLayer.translateBlockstate(root);
            report.fixed("blockstate translation: " + file);
        }
        if (path.endsWith("sounds.json")) {
            normalizeSounds(root, file);
        }
        if (path.contains("atlases/")) {
            report.fixed("atlas checked: " + file);
        }

        Files.writeString(file, gson.toJson(root), StandardCharsets.UTF_8);
        report.converted("resource-json:" + file);
    }

    private void convertItemModel(JsonObject root, Path legacyModelFile, Path itemsDir) throws IOException {
        if (!root.has("overrides") || !root.get("overrides").isJsonArray()) {
            return;
        }

        JsonArray overrides = root.getAsJsonArray("overrides");
        JsonObject selectNode = new JsonObject();
        selectNode.addProperty("type", "minecraft:select");
        selectNode.addProperty("property", "minecraft:custom_model_data");

        JsonArray cases = new JsonArray();
        for (JsonElement element : overrides) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject ov = element.getAsJsonObject();
            JsonObject predicate = ov.has("predicate") && ov.get("predicate").isJsonObject()
                ? ov.getAsJsonObject("predicate") : null;
            if (predicate == null || !predicate.has("custom_model_data")) {
                continue;
            }

            JsonObject entry = new JsonObject();
            entry.add("when", predicate.get("custom_model_data"));
            entry.addProperty("model", ov.get("model").getAsString());
            cases.add(entry);
        }
        selectNode.add("cases", cases);

        JsonObject modern = new JsonObject();
        modern.add("model", selectNode);

        Path modernTarget = itemsDir.resolve(legacyModelFile.getFileName().toString());
        Files.writeString(modernTarget, gson.toJson(modern), StandardCharsets.UTF_8);

        root.remove("overrides");
        report.fixed("model override migration: " + legacyModelFile);
    }

    private void normalizeSounds(JsonObject root, Path file) {
        for (String key : root.keySet()) {
            if (!root.get(key).isJsonObject()) {
                report.manual("sounds.json entry not object in " + file + " key=" + key);
            }
        }
    }

    private void rewritePackMeta(Path file, int packFormat) throws IOException {
        JsonObject root = safeParseJson(file);
        if (root == null) {
            return;
        }
        translationLayer.translatePackMeta(root, packFormat);
        Files.writeString(file, gson.toJson(root), StandardCharsets.UTF_8);
        report.fixed("resource pack.mcmeta updated: " + file);
    }

    private JsonObject safeParseJson(Path file) {
        try {
            String content = CompletableFuture.supplyAsync(() -> {
                try {
                    return Files.readString(file, StandardCharsets.UTF_8);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).join();
            return JsonParser.parseString(content).getAsJsonObject();
        } catch (Exception ex) {
            logger.warn("Invalid resource JSON: " + file + " -> " + ex.getMessage());
            return null;
        }
    }
}
