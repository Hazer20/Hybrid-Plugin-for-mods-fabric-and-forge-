package dev.sanguine.bridge;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DatapackBridgeService {
    private static final int MC_121_PACK_FORMAT = 61;
    private static final Set<String> TEXT_EXTENSIONS = Set.of(
        ".mcfunction", ".json", ".mcmeta", ".txt", ".nbt.txt"
    );

    private static final Pattern LEGACY_ATTRIBUTE_PATTERN = Pattern.compile("(?<!minecraft:)\\bgeneric\\.[a-z_]+\\b");

    private final JavaPlugin plugin;
    private final McFunctionTransformer transformer;

    public DatapackBridgeService(JavaPlugin plugin, Map<String, String> extraReplacements) {
        this.plugin = plugin;

        Map<String, String> allReplacements = new LinkedHashMap<>(BridgeRuleSet.defaultCommandReplacements());
        allReplacements.putAll(extraReplacements);

        this.transformer = new McFunctionTransformer(allReplacements);
    }

    public DatapackBridgeReport bridge(Path sourceDatapack, Path outputDatapack) throws IOException {
        if (!Files.exists(sourceDatapack)) {
            throw new IOException("Source datapack does not exist: " + sourceDatapack);
        }

        List<Path> transformedFiles = new ArrayList<>();
        Set<String> warnings = new LinkedHashSet<>();
        Files.createDirectories(outputDatapack);

        int[] scannedFiles = {0};
        try (var stream = Files.walk(sourceDatapack)) {
            stream.forEach(path -> {
                try {
                    Path relative = sourceDatapack.relativize(path);
                    Path target = outputDatapack.resolve(relative);

                    if (Files.isDirectory(path)) {
                        Files.createDirectories(target);
                        return;
                    }

                    scannedFiles[0]++;
                    String fileName = path.getFileName().toString();

                    if ("pack.mcmeta".equals(fileName) || isTextFile(fileName)) {
                        String original = Files.readString(path, StandardCharsets.UTF_8);
                        String transformed = transformer.transformText(original);

                        if ("pack.mcmeta".equals(fileName)) {
                            transformed = transformed.replaceAll("\"pack_format\"\\s*:\\s*\\d+", "\"pack_format\": " + MC_121_PACK_FORMAT);
                        }

                        if (!original.equals(transformed)) {
                            transformedFiles.add(target);
                        }

                        collectWarnings(relative, transformed, warnings);
                        Files.writeString(target, transformed, StandardCharsets.UTF_8);
                    } else {
                        Files.copy(path, target, StandardCopyOption.REPLACE_EXISTING);
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

        return new DatapackBridgeReport(sourceDatapack, outputDatapack, transformedFiles.size(), scannedFiles[0], List.copyOf(warnings));
    }

    private void collectWarnings(Path relative, String content, Set<String> warnings) {
        Matcher attributeMatcher = LEGACY_ATTRIBUTE_PATTERN.matcher(content);
        if (attributeMatcher.find()) {
            warnings.add(relative + " still contains legacy attribute token: " + attributeMatcher.group());
        }
    }

    private boolean isTextFile(String fileName) {
        String lower = fileName.toLowerCase();
        for (String extension : TEXT_EXTENSIONS) {
            if (lower.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    public void logResult(DatapackBridgeReport result) {
        plugin.getLogger().info("Sanguine bridge built: " + result.sourceDatapack() + " -> " + result.outputDatapack()
            + " (" + result.transformedFiles() + " files transformed, " + result.scannedFiles() + " files scanned)");

        for (String warning : result.warnings()) {
            plugin.getLogger().warning("Bridge review: " + warning);
        }
    }
}
