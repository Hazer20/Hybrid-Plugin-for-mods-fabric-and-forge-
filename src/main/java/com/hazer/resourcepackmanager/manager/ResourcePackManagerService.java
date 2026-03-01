package com.hazer.resourcepackmanager.manager;

import com.hazer.resourcepackmanager.model.PackAvailability;
import com.hazer.resourcepackmanager.model.PackInfo;
import com.hazer.resourcepackmanager.util.PluginLogger;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Central service responsible for all filesystem and metadata operations related
 * to resource pack discovery and selection.
 * <p>
 * Responsibilities include:
 * <ul>
 *     <li>Ensuring {@code resourcepacks} directory exists.</li>
 *     <li>Listing and sorting all valid .zip files.</li>
 *     <li>Choosing the newest file by last-modified timestamp.</li>
 *     <li>Generating user-download URL from plugin configuration.</li>
 *     <li>Computing SHA-1 hash used by Minecraft for cache validation.</li>
 *     <li>Providing thread-safe reads/writes of active pack snapshot.</li>
 * </ul>
 */
public final class ResourcePackManagerService {
    private final JavaPlugin plugin;
    private final PluginLogger logger;
    private final Path serverRoot;
    private final Path resourcepacksDirectory;
    private final ReentrantReadWriteLock lock;

    private volatile PackAvailability availability;
    private volatile Optional<PackInfo> activePack;

    /**
     * Creates manager service.
     *
     * @param plugin plugin instance
     * @param logger logging helper
     */
    public ResourcePackManagerService(final JavaPlugin plugin, final PluginLogger logger) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.logger = Objects.requireNonNull(logger, "logger");
        this.serverRoot = plugin.getServer().getWorldContainer().toPath().toAbsolutePath();
        this.resourcepacksDirectory = serverRoot.resolve("resourcepacks");
        this.lock = new ReentrantReadWriteLock();
        this.availability = PackAvailability.NO_PACKS_FOUND;
        this.activePack = Optional.empty();
    }

    /**
     * Ensures resourcepack folder exists. If missing, creates it.
     *
     * @return true if folder exists or was created
     */
    public boolean ensureResourcepacksDirectory() {
        lock.writeLock().lock();
        try {
            if (Files.exists(resourcepacksDirectory)) {
                if (!Files.isDirectory(resourcepacksDirectory)) {
                    logger.error("Путь resourcepacks существует, но это не папка: " + resourcepacksDirectory);
                    availability = PackAvailability.DIRECTORY_READ_ERROR;
                    return false;
                }
                logger.debug("Папка resourcepacks уже существует: " + resourcepacksDirectory);
                return true;
            }

            Files.createDirectories(resourcepacksDirectory);
            logger.info("Создана папка resourcepacks: " + resourcepacksDirectory);
            return true;
        } catch (final IOException ioException) {
            logger.error("Не удалось создать/проверить папку resourcepacks", ioException);
            availability = PackAvailability.DIRECTORY_READ_ERROR;
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Reloads available packs from filesystem and selects newest one.
     *
     * @param computeHash whether to compute SHA-1 hash for selected pack
     * @return availability result after reload
     */
    public PackAvailability reload(final boolean computeHash) {
        lock.writeLock().lock();
        try {
            logger.info("Перезагрузка списка ресурс-паков...");
            if (!ensureResourcepacksDirectory()) {
                return availability;
            }

            final List<Path> zipFiles = listZipFilesInternal();
            if (zipFiles.isEmpty()) {
                this.activePack = Optional.empty();
                this.availability = PackAvailability.NO_PACKS_FOUND;
                logger.warn("В папке resourcepacks не найдено ни одного .zip файла.");
                return availability;
            }

            final Path latest = zipFiles.stream()
                    .max(Comparator.comparing(this::lastModifiedSafe))
                    .orElseThrow();

            final PackInfo selected = createPackInfo(latest, computeHash);
            this.activePack = Optional.of(selected);
            this.availability = selected.packUrl().isPresent()
                    ? PackAvailability.AVAILABLE
                    : PackAvailability.URL_CONFIGURATION_ERROR;

            logger.info("Выбран активный ресурс-пак: " + selected.fileName());
            logger.debug("Информация о паке: " + selected.asDescription().replace('\n', ' '));
            return availability;
        } catch (final RuntimeException runtimeException) {
            logger.error("Ошибка во время перезагрузки ресурс-паков", runtimeException);
            this.activePack = Optional.empty();
            this.availability = PackAvailability.DIRECTORY_READ_ERROR;
            return availability;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Returns currently active pack snapshot.
     *
     * @return active pack if available
     */
    public Optional<PackInfo> getActivePack() {
        lock.readLock().lock();
        try {
            return activePack;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Returns current availability status.
     *
     * @return availability enum
     */
    public PackAvailability getAvailability() {
        return availability;
    }

    /**
     * Lists discovered zip files as {@link PackInfo} sorted by newest first.
     *
     * @return immutable snapshot list
     */
    public List<PackInfo> listAllPacks() {
        lock.readLock().lock();
        try {
            if (!Files.isDirectory(resourcepacksDirectory)) {
                return List.of();
            }
            final List<Path> files = listZipFilesInternal();
            return files.stream()
                    .sorted(Comparator.comparing(this::lastModifiedSafe).reversed())
                    .map(path -> createPackInfo(path, false))
                    .collect(Collectors.toUnmodifiableList());
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * @return absolute path to managed folder.
     */
    public Path getResourcepacksDirectory() {
        return resourcepacksDirectory;
    }

    private List<Path> listZipFilesInternal() {
        if (!Files.isDirectory(resourcepacksDirectory)) {
            return List.of();
        }

        try (Stream<Path> stream = Files.list(resourcepacksDirectory)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().toLowerCase().endsWith(".zip"))
                    .collect(Collectors.toCollection(ArrayList::new));
        } catch (final IOException ioException) {
            logger.error("Ошибка чтения папки resourcepacks", ioException);
            return List.of();
        }
    }

    private PackInfo createPackInfo(final Path path, final boolean computeHash) {
        final String fileName = path.getFileName().toString();
        final long size = sizeSafe(path);
        final Instant modified = lastModifiedSafe(path);
        final Optional<String> url = buildPackUrl(fileName);
        final Optional<String> sha1 = computeHash ? computeSha1(path) : Optional.empty();

        return new PackInfo(path, fileName, size, modified, sha1, url);
    }

    private Optional<String> buildPackUrl(final String fileName) {
        final FileConfiguration config = plugin.getConfig();
        final String rawBaseUrl = config.getString("resource-pack-base-url", "").trim();

        if (rawBaseUrl.isEmpty()) {
            logger.warn("resource-pack-base-url не настроен в config.yml");
            return Optional.empty();
        }

        try {
            final String base = rawBaseUrl.endsWith("/") ? rawBaseUrl : rawBaseUrl + "/";
            final String encodedName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
            final String built = base + encodedName;
            URI.create(built);
            return Optional.of(built);
        } catch (final IllegalArgumentException illegalArgumentException) {
            logger.error("Некорректный URL resource-pack-base-url: " + rawBaseUrl, illegalArgumentException);
            return Optional.empty();
        }
    }

    private Optional<String> computeSha1(final Path zipPath) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-1");
            try (InputStream input = Files.newInputStream(zipPath)) {
                final byte[] buffer = new byte[8192];
                int read;
                while ((read = input.read(buffer)) >= 0) {
                    digest.update(buffer, 0, read);
                }
            }
            return Optional.of(HexFormat.of().formatHex(digest.digest()));
        } catch (final NoSuchAlgorithmException impossible) {
            logger.error("JVM не поддерживает SHA-1", impossible);
            return Optional.empty();
        } catch (final IOException ioException) {
            logger.error("Не удалось вычислить SHA-1 для файла: " + zipPath, ioException);
            return Optional.empty();
        }
    }

    private long sizeSafe(final Path path) {
        try {
            return Files.size(path);
        } catch (final IOException e) {
            logger.warn("Не удалось прочитать размер файла: " + path);
            return 0L;
        }
    }

    private Instant lastModifiedSafe(final Path path) {
        try {
            return Files.getLastModifiedTime(path).toInstant();
        } catch (final IOException e) {
            logger.warn("Не удалось получить дату изменения файла: " + path);
            return Instant.EPOCH;
        }
    }
}
