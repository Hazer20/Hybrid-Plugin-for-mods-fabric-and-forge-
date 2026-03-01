package me.adaptiveservercore.automation;

import me.adaptiveservercore.AdaptiveServerCore;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BackupManager {

    private static final DateTimeFormatter NAME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    private final AdaptiveServerCore plugin;

    public BackupManager(AdaptiveServerCore plugin) {
        this.plugin = plugin;
    }

    public void createBackupSync(String reason) {
        if (!plugin.getConfig().getBoolean("бекапы.включены", true)) {
            return;
        }

        String folderName = plugin.getConfig().getString("бекапы.папка", "server-backups");
        int keep = Math.max(1, plugin.getConfig().getInt("бекапы.хранить", 10));

        Path serverRoot = Paths.get(".").toAbsolutePath().normalize();
        Path backupDir = serverRoot.resolve(folderName);
        try {
            Files.createDirectories(backupDir);
        } catch (IOException e) {
            plugin.getLogger().severe("Не удалось создать папку бекапов: " + e.getMessage());
            return;
        }

        String fileName = "backup_" + reason + "_" + LocalDateTime.now().format(NAME_FORMAT) + ".zip";
        Path zipPath = backupDir.resolve(fileName);

        List<String> targets = List.of("world", "world_nether", "world_the_end", "plugins", "configs", "config");
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(zipPath))) {
            for (String target : targets) {
                Path source = serverRoot.resolve(target).normalize();
                if (!Files.exists(source)) {
                    continue;
                }
                if (Files.isDirectory(source)) {
                    Files.walk(source)
                            .filter(path -> !Files.isDirectory(path))
                            .forEach(path -> addToZip(serverRoot, path, zip));
                } else {
                    addToZip(serverRoot, source, zip);
                }
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Ошибка создания бекапа: " + e.getMessage());
            return;
        }

        cleanupOldBackups(backupDir, keep);
        Bukkit.getLogger().info("[AdaptiveServerCore] Резервная копия создана: " + zipPath.getFileName());
    }

    private void addToZip(Path serverRoot, Path file, ZipOutputStream zip) {
        String zipName = serverRoot.relativize(file).toString().replace('\\', '/');
        try (InputStream in = Files.newInputStream(file)) {
            zip.putNextEntry(new ZipEntry(zipName));
            in.transferTo(zip);
            zip.closeEntry();
        } catch (IOException e) {
            plugin.getLogger().warning("Не удалось добавить файл в бекап: " + file + " -> " + e.getMessage());
        }
    }

    private void cleanupOldBackups(Path backupDir, int keep) {
        try {
            List<Path> backups = Files.list(backupDir)
                    .filter(path -> path.getFileName().toString().endsWith(".zip"))
                    .sorted(Comparator.comparingLong(path -> path.toFile().lastModified()).reversed())
                    .toList();

            for (int i = keep; i < backups.size(); i++) {
                Files.deleteIfExists(backups.get(i));
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Не удалось очистить старые бекапы: " + e.getMessage());
        }
    }
}
