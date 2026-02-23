package dev.sanguine.bridge;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DatapackBridgeService {
    private static final int MC_121_PACK_FORMAT = 61;

    private final JavaPlugin plugin;
    private final McFunctionTransformer transformer;

    public DatapackBridgeService(JavaPlugin plugin, Map<String, String> extraReplacements) {
        this.plugin = plugin;

        Map<String, String> allReplacements = new LinkedHashMap<>(BridgeRuleSet.defaultCommandReplacements());
        allReplacements.putAll(extraReplacements);

        this.transformer = new McFunctionTransformer(allReplacements);
    }

    public BridgeResult bridge(Path sourceDatapack, Path outputDatapack) throws IOException {
        if (!Files.exists(sourceDatapack)) {
            throw new IOException("Source datapack does not exist: " + sourceDatapack);
        }

        List<Path> transformedFiles = new ArrayList<>();
        Files.createDirectories(outputDatapack);

        try (var stream = Files.walk(sourceDatapack)) {
            stream.forEach(path -> {
                try {
                    Path relative = sourceDatapack.relativize(path);
                    Path target = outputDatapack.resolve(relative);

                    if (Files.isDirectory(path)) {
                        Files.createDirectories(target);
                        return;
                    }

                    String fileName = path.getFileName().toString();
                    if (fileName.endsWith(".mcfunction")) {
                        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
                        List<String> transformed = lines.stream().map(transformer::transformLine).toList();
                        Files.write(target, transformed, StandardCharsets.UTF_8);
                        transformedFiles.add(target);
                    } else if ("pack.mcmeta".equals(fileName)) {
                        String contents = Files.readString(path, StandardCharsets.UTF_8);
                        contents = contents.replaceAll("\"pack_format\"\\s*:\\s*\\d+", "\"pack_format\": " + MC_121_PACK_FORMAT);
                        Files.writeString(target, contents, StandardCharsets.UTF_8);
                    } else {
                        Files.copy(path, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException exception) {
                    throw new RuntimeException(exception);
                }
            });
        } catch (RuntimeException wrapped) {
            if (wrapped.getCause() instanceof IOException io) {
                throw io;
            }
            throw wrapped;
        }

        return new BridgeResult(sourceDatapack, outputDatapack, transformedFiles.size());
    }

    public void logResult(BridgeResult result) {
        plugin.getLogger().info("Sanguine bridge built: " + result.sourceDatapack() + " -> " + result.outputDatapack()
            + " (" + result.transformedFiles() + " .mcfunction files transformed)");
    }
}
