package me.adaptiveservercore.automation;

import me.adaptiveservercore.AdaptiveServerCore;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
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

        String configuredFolder = plugin.getConfig().getString("бекапы.папка", "server-backups");
        String folderName = (configuredFolder == null || configuredFolder.isBlank()) ? "server-backups" : configuredFolder;
        int keep = Math.max(1, plugin.getConfig().getInt("бекапы.хранить", 10));

        Path serverRoot = Paths.get(".").toAbsolutePath().normalize();
        Path backupDir = serverRoot.resolve(folderName).normalize();

        try {
            Files.createDirectories(backupDir);
        } catch (IOException e) {
            plugin.getLogger().severe("Не удалось создать папку бекапов: " + e.getMessage());
            return;
        }

        String fileName = "backup_" + reason + "_" + LocalDateTime.now().format(NAME_FORMAT) + ".zip";
        Path zipPath = backupDir.resolve(fileName);

        List<String> targets = List.of("world", "world_nether", "world_the_end", "plugins", "configs", "config");
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(
                zipPath,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
        ))) {
            for (String target : targets) {
                Path source = serverRoot.resolve(target).normalize();
                if (!Files.exists(source)) {
                    continue;
                }

                if (Files.isDirectory(source)) {
                    addDirectoryToZip(serverRoot, source, zip);
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

    private void addDirectoryToZip(Path serverRoot, Path directory, ZipOutputStream zip) {
        try (Stream<Path> stream = Files.walk(directory)) {
            stream
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> addToZip(serverRoot, path, zip));
        } catch (IOException e) {
            plugin.getLogger().warning("Не удалось прочитать директорию для бекапа: " + directory + " -> " + e.getMessage());
        }
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
        try (Stream<Path> stream = Files.list(backupDir)) {
            List<Path> backups = stream
                    .filter(path -> path.getFileName().toString().endsWith(".zip"))
                    .sorted(Comparator.comparingLong(this::lastModifiedMillis).reversed())
                    .toList();

            for (int i = keep; i < backups.size(); i++) {
                Files.deleteIfExists(backups.get(i));
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Не удалось очистить старые бекапы: " + e.getMessage());
        } catch (UncheckedIOException e) {
            plugin.getLogger().warning("Не удалось получить дату изменения файла бекапа: " + e.getMessage());
        }
    }

    private long lastModifiedMillis(Path path) {
        try {
            return Files.getLastModifiedTime(path).toMillis();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
