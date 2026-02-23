package dev.sanguine.engine;

import dev.sanguine.engine.log.ConversionLogger;
import dev.sanguine.engine.pack.ArchiveUtils;
import dev.sanguine.engine.report.ConversionReport;
import dev.sanguine.engine.resource.ResourcePackConverter;
import dev.sanguine.engine.transpile.DatapackConverter;
import dev.sanguine.engine.validation.PackValidator;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PackConversionEngine {
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final JavaPlugin plugin;
    private final PackWorkspace workspace;
    private final ConversionLogger conversionLogger;
    private final ConversionReport report;

    public PackConversionEngine(JavaPlugin plugin) {
        this.plugin = plugin;
        this.conversionLogger = new ConversionLogger(plugin);
        this.report = new ConversionReport();

        Path root = plugin.getDataFolder().toPath();
        this.workspace = new PackWorkspace(
            root,
            root.resolve("input_datapack"),
            root.resolve("input_resourcepack"),
            root.resolve("generated_datapack"),
            root.resolve("generated_resourcepack"),
            root.resolve("tmp_datapack"),
            root.resolve("tmp_resourcepack"),
            root.resolve("generated_datapack.zip"),
            root.resolve("generated_resourcepack.zip")
        );
    }

    public void prepareDirectories() {
        try {
            Files.createDirectories(workspace.inputDatapack());
            Files.createDirectories(workspace.inputResourcepack());
            Files.createDirectories(workspace.generatedDatapack());
            Files.createDirectories(workspace.generatedResourcepack());
            Files.createDirectories(workspace.tmpDatapack());
            Files.createDirectories(workspace.tmpResourcepack());
        } catch (IOException e) {
            conversionLogger.error("Cannot create engine directories", e);
        }
    }

    public void runConversion() {
        try {
            backupInputs();
            ArchiveUtils.cleanDirectory(workspace.tmpDatapack());
            ArchiveUtils.cleanDirectory(workspace.tmpResourcepack());
            ArchiveUtils.cleanDirectory(workspace.generatedDatapack());
            ArchiveUtils.cleanDirectory(workspace.generatedResourcepack());

            ArchiveUtils.unpackInput(workspace.inputDatapack(), workspace.tmpDatapack());
            ArchiveUtils.unpackInput(workspace.inputResourcepack(), workspace.tmpResourcepack());

            new DatapackConverter(conversionLogger, report).convert(workspace.tmpDatapack(), workspace.generatedDatapack());
            new ResourcePackConverter(conversionLogger, report).convert(workspace.tmpResourcepack(), workspace.generatedResourcepack());

            new PackValidator(conversionLogger).validate(workspace.generatedDatapack(), report);
            new PackValidator(conversionLogger).validate(workspace.generatedResourcepack(), report);

            ArchiveUtils.zipDirectory(workspace.generatedDatapack(), workspace.generatedDatapackZip());
            ArchiveUtils.zipDirectory(workspace.generatedResourcepack(), workspace.generatedResourcepackZip());

            installGeneratedDatapack();
            scheduleResourcePackSend();
            writeReport();
        } catch (Exception exception) {
            conversionLogger.error("SanguineCompatibilityEngine conversion failed", exception);
        }
    }

    private void backupInputs() {
        try {
            Path backupRoot = workspace.pluginRoot().resolve("backup").resolve(TS.format(LocalDateTime.now()));
            Files.createDirectories(backupRoot);
            ArchiveUtils.copyTree(workspace.inputDatapack(), backupRoot.resolve("input_datapack"));
            ArchiveUtils.copyTree(workspace.inputResourcepack(), backupRoot.resolve("input_resourcepack"));
            report.converted("backup-created:" + backupRoot);
        } catch (Exception ex) {
            conversionLogger.warn("Backup creation failed: " + ex.getMessage());
        }
    }

    private void installGeneratedDatapack() throws IOException {
        String worldName = plugin.getConfig().getString("engine.world-name", "world");
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            conversionLogger.warn("World " + worldName + " is not loaded; datapack installation skipped.");
            return;
        }

        Path worldDatapacks = world.getWorldFolder().toPath().resolve("datapacks").resolve("generated_datapack");
        ArchiveUtils.cleanDirectory(worldDatapacks);
        ArchiveUtils.copyTree(workspace.generatedDatapack(), worldDatapacks);

        if (plugin.getConfig().getBoolean("engine.auto-reload-datapacks", true)) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "minecraft:reload");
        }
    }

    private void scheduleResourcePackSend() {
        String url = plugin.getConfig().getString("engine.resource-pack-url", "");
        String sha1 = plugin.getConfig().getString("engine.resource-pack-sha1", "");
        if (sha1 != null && !sha1.isBlank()) {
            report.manual("resource-pack-sha1 configured but Paper API path uses URL-only setResourcePack in this build.");
        }

        if (url == null || url.isBlank()) {
            conversionLogger.warn("resource-pack-url is empty. Host generated_resourcepack.zip and set URL.");
            report.manual("Set engine.resource-pack-url to deliver generated resource pack.");
            return;
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.setResourcePack(url);
            }
        });
    }

    private void writeReport() {
        try {
            report.writeTo(workspace.pluginRoot().resolve("conversion-report.txt"));
            report.writeTo(Path.of("plugins", "HybridConverter", "logs", "conversion-report.txt"));
        } catch (IOException e) {
            conversionLogger.warn("Cannot write conversion report: " + e.getMessage());
        }
    }

    public void shutdown() {
        conversionLogger.info("SanguineCompatibilityEngine disabled.");
    }

    public PackWorkspace workspace() {
        return workspace;
    }
}
