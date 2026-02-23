package dev.sanguine.engine.transpile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.sanguine.engine.log.ConversionLogger;
import dev.sanguine.engine.pack.ArchiveUtils;
import dev.sanguine.engine.parser.McFunctionParser;
import dev.sanguine.engine.report.ConversionReport;
import dev.sanguine.engine.translation.LocalAiTranslationModule;
import dev.sanguine.engine.translation.VersionTranslationLayer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class DatapackConverter {
    private static final int DATAPACK_1218_FORMAT = 61;

    private final McFunctionParser parser = new McFunctionParser();
    private final CommandTranspiler transpiler = new CommandTranspiler();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final VersionTranslationLayer translationLayer = new VersionTranslationLayer();
    private final LocalAiTranslationModule localAi = new LocalAiTranslationModule();
    private final ConversionLogger logger;
    private final ConversionReport report;
    private final ExecutorService ioExecutor;

    public DatapackConverter(ConversionLogger logger, ConversionReport report, ExecutorService ioExecutor) {
        this.logger = logger;
        this.report = report;
        this.ioExecutor = ioExecutor;
    }

    public void convert(Path sourceRoot, Path outRoot, Path unsupportedDir) throws IOException {
        ArchiveUtils.copyTree(sourceRoot, outRoot);
        Files.createDirectories(unsupportedDir);

        try (var paths = Files.walk(outRoot)) {
            paths.filter(Files::isRegularFile).forEach(file -> {
                try {
                    String name = file.getFileName().toString();
                    if (name.endsWith(".mcfunction")) {
                        transpileMcFunction(file);
                    } else if (name.equals("pack.mcmeta")) {
                        rewritePackMeta(file, DATAPACK_1218_FORMAT);
                    } else if (name.endsWith(".json")) {
                        processDatapackJson(file, unsupportedDir);
                    }
                } catch (Exception e) {
                    report.skipped("datapack:" + outRoot.relativize(file));
                    logger.warn("Datapack file skipped: " + file + " -> " + e.getMessage());
                }
            });
        }
    }

    private void transpileMcFunction(Path file) throws IOException {
        String text = asyncRead(file);
        String aiText = localAi.translateRawText(text);
        var lines = aiText.lines().toList();
        var out = lines.stream().map(this::safeTranspileLine).toList();
        Files.write(file, out, StandardCharsets.UTF_8);
        report.converted("mcfunction:" + file);
    }

    private String safeTranspileLine(String line) {
        try {
            String recovered = attemptNbtRecovery(line);
            String translated = translationLayer.translateCommand(recovered);
            String output = transpiler.transpile(parser.parseLine(translated));
            if (!line.equals(output)) report.fixed("command:" + line + " -> " + output);
            return output;
        } catch (Exception ex) {
            report.skipped("command-line:" + line);
            return line;
        }
    }

    private String attemptNbtRecovery(String line) {
        long open = line.chars().filter(c -> c == '{').count();
        long close = line.chars().filter(c -> c == '}').count();
        if (open <= close) return line;
        StringBuilder recovered = new StringBuilder(line);
        for (long i = 0; i < (open - close); i++) recovered.append('}');
        report.fixed("nbt-recovery:" + line);
        return recovered.toString();
    }

    private void rewritePackMeta(Path file, int packFormat) throws IOException {
        JsonObject root = safeParseJson(file);
        if (root == null) return;
        translationLayer.translatePackMeta(root, packFormat);
        Files.writeString(file, gson.toJson(root), StandardCharsets.UTF_8);
    }

    private void processDatapackJson(Path file, Path unsupportedDir) throws IOException {
        JsonObject root = safeParseJson(file);
        if (root == null) {
            Files.move(file, unsupportedDir.resolve(file.getFileName().toString()), StandardCopyOption.REPLACE_EXISTING);
            report.incompatible("invalid-json:" + file);
            return;
        }

        localAi.translateJson(root);
        String path = file.toString().replace('\\', '/');
        if (path.contains("/predicates/")) translationLayer.translatePredicate(root);
        if (path.contains("/loot_tables/")) translationLayer.translateLootTable(root);
        if (path.contains("/advancements/")) translationLayer.translateAdvancement(root);
        if (path.contains("/recipes/")) translationLayer.translateRecipe(root);

        if (root.has("type") && root.get("type").isJsonPrimitive()) {
            String type = root.get("type").getAsString();
            if (!type.contains(":")) root.addProperty("type", "minecraft:" + type);
        }

        Files.writeString(file, gson.toJson(root), StandardCharsets.UTF_8);
        report.converted("json:" + file);
    }

    private JsonObject safeParseJson(Path file) {
        try {
            return JsonParser.parseString(asyncRead(file)).getAsJsonObject();
        } catch (Exception ex) {
            logger.warn("Invalid datapack JSON: " + file + " -> " + ex.getMessage());
            return null;
        }
    }

    private String asyncRead(Path file) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return Files.readString(file, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, ioExecutor).join();
    }
}
