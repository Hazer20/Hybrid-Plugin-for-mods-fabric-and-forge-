package dev.sanguine.engine;

import dev.sanguine.engine.log.ConversionLogger;
import dev.sanguine.engine.pack.ArchiveUtils;
import dev.sanguine.engine.report.ConversionReport;
import dev.sanguine.engine.resource.ResourcePackConverter;
import dev.sanguine.engine.transpile.DatapackConverter;
import dev.sanguine.engine.validation.PackValidator;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PackConversionEngine {
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final JavaPlugin plugin;
    private final PackWorkspace workspace;
    private final ExecutorService ioExecutor;
    private final ConversionLogger logger;

    public PackConversionEngine(JavaPlugin plugin) {
        this.plugin = plugin;
        Path root = plugin.getDataFolder().toPath();
        this.workspace = new PackWorkspace(
            root,
            root.resolve("input/datapacks"),
            root.resolve("input/resourcepacks"),
            root.resolve("generated/datapacks"),
            root.resolve("generated/resourcepacks"),
            root.resolve("temp"),
            root.resolve("backup"),
            root.resolve("backup/unsupported"),
            root.resolve("logs"),
            root.resolve("conversion-report.txt")
        );
        this.ioExecutor = Executors.newFixedThreadPool(Math.max(2, Runtime.getRuntime().availableProcessors() / 2));
        this.logger = new ConversionLogger(plugin, workspace.logs());
    }

    public void prepareDirectories() {
        try {
            Files.createDirectories(workspace.inputDatapacks());
            Files.createDirectories(workspace.inputResourcepacks());
            Files.createDirectories(workspace.generatedDatapacks());
            Files.createDirectories(workspace.generatedResourcepacks());
            Files.createDirectories(workspace.temp());
            Files.createDirectories(workspace.backup());
            Files.createDirectories(workspace.unsupported());
            Files.createDirectories(workspace.logs());
        } catch (IOException e) {
            logger.error("Cannot create workspace directories", e);
        }
    }

    public void convertAllAsync(CommandSender sender) {
        CompletableFuture.runAsync(() -> convertAll(sender), ioExecutor)
            .exceptionally(ex -> {
                logger.error("Async conversion crashed", ex);
                if (sender != null) sender.sendMessage("§c[HybridConverter] Conversion crashed: " + ex.getMessage());
                return null;
            });
    }

    public void convertAll(CommandSender sender) {
        ConversionReport report = new ConversionReport();
        try {
            if (sender != null) sender.sendMessage("§e[HybridConverter] Starting conversion...");
            backupInputs(report);

            ArchiveUtils.cleanDirectory(workspace.generatedDatapacks());
            ArchiveUtils.cleanDirectory(workspace.generatedResourcepacks());

            convertDatapacks(report);
            convertResourcepacks(report);

            PackValidator validator = new PackValidator(logger);
            validator.validate(workspace.generatedDatapacks(), report);
            validator.validate(workspace.generatedResourcepacks(), report);

            report.writeTo(workspace.reportFile());
            if (sender != null) sender.sendMessage("§a[HybridConverter] Conversion finished. Report: " + workspace.reportFile());
        } catch (Exception ex) {
            logger.error("Conversion failed", ex);
            if (sender != null) sender.sendMessage("§c[HybridConverter] Conversion failed: " + ex.getMessage());
        }
    }

    private void backupInputs(ConversionReport report) {
        try {
            Path stamp = workspace.backup().resolve(TS.format(LocalDateTime.now()));
            Files.createDirectories(stamp);
            ArchiveUtils.copyTree(workspace.inputDatapacks(), stamp.resolve("input_datapacks"));
            ArchiveUtils.copyTree(workspace.inputResourcepacks(), stamp.resolve("input_resourcepacks"));
            report.converted("backup: " + stamp);
        } catch (Exception ex) {
            logger.warn("Backup failed: " + ex.getMessage());
            report.manual("Backup failed: " + ex.getMessage());
        }
    }

    private void convertDatapacks(ConversionReport report) throws IOException {
        DatapackConverter converter = new DatapackConverter(logger, report, ioExecutor);
        try (var packs = Files.list(workspace.inputDatapacks())) {
            packs.filter(Files::isDirectory).forEach(packDir -> {
                try {
                    Path target = workspace.generatedDatapacks().resolve(packDir.getFileName().toString());
                    converter.convert(packDir, target, workspace.unsupported());
                    report.converted("datapack folder: " + packDir.getFileName());
                } catch (Exception ex) {
                    logger.warn("Datapack conversion failed: " + packDir + " -> " + ex.getMessage());
                    report.incompatible("datapack failed: " + packDir);
                }
            });
        }
    }

    private void convertResourcepacks(ConversionReport report) throws IOException {
        ResourcePackConverter converter = new ResourcePackConverter(logger, report, ioExecutor);
        Path tempUnpackRoot = workspace.temp().resolve("resourcepacks");
        ArchiveUtils.cleanDirectory(tempUnpackRoot);

        try (var zips = Files.list(workspace.inputResourcepacks())) {
            zips.filter(path -> Files.isRegularFile(path) && path.getFileName().toString().endsWith(".zip"))
                .forEach(zip -> {
                    try {
                        Path unpacked = tempUnpackRoot.resolve(stripZipExt(zip.getFileName().toString()));
                        Files.createDirectories(unpacked);
                        ArchiveUtils.unzip(zip, unpacked);

                        Path generatedFolder = workspace.generatedResourcepacks().resolve(stripZipExt(zip.getFileName().toString()));
                        Path generatedZip = workspace.generatedResourcepacks().resolve(zip.getFileName().toString());
                        converter.convert(unpacked, generatedFolder, workspace.unsupported(), generatedZip);
                        report.converted("resourcepack zip: " + zip.getFileName());
                    } catch (Exception ex) {
                        logger.warn("Resourcepack conversion failed: " + zip + " -> " + ex.getMessage());
                        report.incompatible("resourcepack failed: " + zip.getFileName());
                    }
                });
        }
    }

    private String stripZipExt(String name) {
        return name.endsWith(".zip") ? name.substring(0, name.length() - 4) : name;
    }

    public void shutdown() {
        ioExecutor.shutdownNow();
        logger.info("HybridConverter disabled");
    }
}
