package dev.sanguine.engine;

import dev.sanguine.engine.pack.ArchiveUtils;
import dev.sanguine.engine.resource.ResourcePackConverter;
import dev.sanguine.engine.transpile.DatapackConverter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PackConversionEngine {
    private final JavaPlugin plugin;
    private final PackWorkspace workspace;

    public PackConversionEngine(JavaPlugin plugin) {
        this.plugin = plugin;
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
            throw new IllegalStateException("Cannot create engine directories", e);
        }
    }

    public void runConversion() {
        try {
            ArchiveUtils.cleanDirectory(workspace.tmpDatapack());
            ArchiveUtils.cleanDirectory(workspace.tmpResourcepack());
            ArchiveUtils.cleanDirectory(workspace.generatedDatapack());
            ArchiveUtils.cleanDirectory(workspace.generatedResourcepack());

            ArchiveUtils.unpackInput(workspace.inputDatapack(), workspace.tmpDatapack());
            ArchiveUtils.unpackInput(workspace.inputResourcepack(), workspace.tmpResourcepack());

            DatapackConverter datapackConverter = new DatapackConverter(plugin);
            datapackConverter.convert(workspace.tmpDatapack(), workspace.generatedDatapack());

            ResourcePackConverter resourcePackConverter = new ResourcePackConverter(plugin);
            resourcePackConverter.convert(workspace.tmpResourcepack(), workspace.generatedResourcepack());

            ArchiveUtils.zipDirectory(workspace.generatedDatapack(), workspace.generatedDatapackZip());
            ArchiveUtils.zipDirectory(workspace.generatedResourcepack(), workspace.generatedResourcepackZip());

            installGeneratedDatapack();
            scheduleResourcePackHint();
        } catch (Exception exception) {
            plugin.getLogger().severe("SanguineCompatibilityEngine conversion failed: " + exception.getMessage());
        }
    }

    private void installGeneratedDatapack() throws IOException {
        String worldName = plugin.getConfig().getString("engine.world-name", "world");
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            plugin.getLogger().warning("World " + worldName + " is not loaded; datapack installation skipped.");
            return;
        }

        Path worldDatapacks = world.getWorldFolder().toPath().resolve("datapacks").resolve("generated_datapack");
        ArchiveUtils.cleanDirectory(worldDatapacks);
        ArchiveUtils.copyTree(workspace.generatedDatapack(), worldDatapacks);

        if (plugin.getConfig().getBoolean("engine.auto-reload-datapacks", true)) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "minecraft:reload");
        }
    }

    private void scheduleResourcePackHint() {
        String url = plugin.getConfig().getString("engine.resource-pack-url", "");
        if (url == null || url.isBlank()) {
            plugin.getLogger().warning("resource-pack-url is empty. Put generated_resourcepack.zip on CDN and set URL in config.");
        }
    }

    public void shutdown() {
        plugin.getLogger().info("SanguineCompatibilityEngine disabled.");
    }

    public PackWorkspace workspace() {
        return workspace;
    }
}
