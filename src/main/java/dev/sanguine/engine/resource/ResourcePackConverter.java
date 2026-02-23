package dev.sanguine.engine.resource;

import com.google.gson.*;
import dev.sanguine.engine.pack.ArchiveUtils;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ResourcePackConverter {
    private static final int RESOURCEPACK_1218_FORMAT = 46;

    private final JavaPlugin plugin;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public ResourcePackConverter(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void convert(Path sourceRoot, Path outRoot) throws IOException {
        ArchiveUtils.copyTree(sourceRoot, outRoot);
        Path itemsDir = outRoot.resolve("assets/minecraft/items");
        Files.createDirectories(itemsDir);

        try (var paths = Files.walk(outRoot)) {
            paths.filter(Files::isRegularFile).forEach(file -> {
                try {
                    String name = file.getFileName().toString();
                    if (name.equals("pack.mcmeta")) {
                        rewritePackMeta(file, RESOURCEPACK_1218_FORMAT);
                    }
                    if (isLegacyItemModel(file)) {
                        convertItemModel(file, itemsDir);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        plugin.getLogger().info("Resource pack transpilation completed for " + outRoot);
    }

    private boolean isLegacyItemModel(Path file) {
        String path = file.toString().replace('\\', '/');
        return path.contains("assets/minecraft/models/item/") && path.endsWith(".json");
    }

    private void convertItemModel(Path legacyModelFile, Path itemsDir) throws IOException {
        JsonObject root = JsonParser.parseString(Files.readString(legacyModelFile, StandardCharsets.UTF_8)).getAsJsonObject();
        if (!root.has("overrides")) {
            return;
        }

        JsonArray overrides = root.getAsJsonArray("overrides");
        JsonObject selectNode = new JsonObject();
        selectNode.addProperty("type", "minecraft:select");
        selectNode.addProperty("property", "minecraft:custom_model_data");

        JsonArray cases = new JsonArray();
        for (JsonElement element : overrides) {
            JsonObject ov = element.getAsJsonObject();
            JsonObject predicate = ov.getAsJsonObject("predicate");
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

        String itemName = legacyModelFile.getFileName().toString();
        Path modernTarget = itemsDir.resolve(itemName);
        Files.writeString(modernTarget, gson.toJson(modern), StandardCharsets.UTF_8);

        root.remove("overrides");
        Files.writeString(legacyModelFile, gson.toJson(root), StandardCharsets.UTF_8);
    }

    private void rewritePackMeta(Path file, int packFormat) throws IOException {
        JsonObject root = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject();
        JsonObject pack = root.has("pack") ? root.getAsJsonObject("pack") : new JsonObject();
        pack.addProperty("pack_format", packFormat);
        root.add("pack", pack);
        Files.writeString(file, gson.toJson(root), StandardCharsets.UTF_8);
    }
}
