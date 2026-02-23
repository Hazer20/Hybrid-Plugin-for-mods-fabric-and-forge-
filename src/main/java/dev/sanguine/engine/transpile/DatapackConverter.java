package dev.sanguine.engine.transpile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.sanguine.engine.pack.ArchiveUtils;
import dev.sanguine.engine.parser.McFunctionParser;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class DatapackConverter {
    private static final int DATAPACK_1218_FORMAT = 61;

    private final JavaPlugin plugin;
    private final McFunctionParser parser = new McFunctionParser();
    private final CommandTranspiler transpiler = new CommandTranspiler();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public DatapackConverter(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void convert(Path sourceRoot, Path outRoot) throws IOException {
        ArchiveUtils.copyTree(sourceRoot, outRoot);

        try (var paths = Files.walk(outRoot)) {
            paths.filter(Files::isRegularFile).forEach(file -> {
                try {
                    String name = file.getFileName().toString();
                    if (name.endsWith(".mcfunction")) {
                        transpileMcFunction(file);
                    } else if (name.equals("pack.mcmeta")) {
                        rewritePackMeta(file, DATAPACK_1218_FORMAT);
                    } else if (isDatapackJson(file)) {
                        normalizeDatapackJson(file);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        plugin.getLogger().info("Datapack transpilation completed for " + outRoot);
    }

    private void transpileMcFunction(Path file) throws IOException {
        var lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        var out = lines.stream()
            .map(line -> transpiler.transpile(parser.parseLine(line)))
            .toList();
        Files.write(file, out, StandardCharsets.UTF_8);
    }

    private void rewritePackMeta(Path file, int packFormat) throws IOException {
        JsonObject root = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject();
        JsonObject pack = root.has("pack") ? root.getAsJsonObject("pack") : new JsonObject();
        pack.addProperty("pack_format", packFormat);
        root.add("pack", pack);
        Files.writeString(file, gson.toJson(root), StandardCharsets.UTF_8);
    }

    private boolean isDatapackJson(Path file) {
        String path = file.toString().replace('\\', '/');
        return path.contains("/predicates/") || path.contains("/loot_tables/") || path.contains("/advancements/") || path.contains("/damage_type/");
    }

    private void normalizeDatapackJson(Path file) throws IOException {
        JsonObject root = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject();
        if (root.has("type") && !root.get("type").getAsString().contains(":")) {
            root.addProperty("type", "minecraft:" + root.get("type").getAsString());
        }
        Files.writeString(file, gson.toJson(root), StandardCharsets.UTF_8);
    }
}
