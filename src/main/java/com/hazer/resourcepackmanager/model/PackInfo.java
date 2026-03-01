package com.hazer.resourcepackmanager.model;

import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable data holder describing one discovered resource pack zip file.
 * <p>
 * We use a regular class (instead of record) for broader readability and to attach
 * small formatting helpers with detailed Javadoc.
 */
public final class PackInfo {
    private static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withLocale(Locale.ROOT)
            .withZone(ZoneId.systemDefault());

    private final Path path;
    private final String fileName;
    private final long sizeBytes;
    private final Instant lastModified;
    private final Optional<String> sha1Hex;
    private final Optional<String> packUrl;

    /**
     * Creates a pack descriptor.
     *
     * @param path full filesystem path
     * @param fileName file name of the zip
     * @param sizeBytes file size in bytes
     * @param lastModified last modified timestamp
     * @param sha1Hex optional SHA-1 hash used by Minecraft clients
     * @param packUrl optional URL that clients download from
     */
    public PackInfo(final Path path,
                    final String fileName,
                    final long sizeBytes,
                    final Instant lastModified,
                    final Optional<String> sha1Hex,
                    final Optional<String> packUrl) {
        this.path = Objects.requireNonNull(path, "path");
        this.fileName = Objects.requireNonNull(fileName, "fileName");
        this.sizeBytes = sizeBytes;
        this.lastModified = Objects.requireNonNull(lastModified, "lastModified");
        this.sha1Hex = Objects.requireNonNull(sha1Hex, "sha1Hex");
        this.packUrl = Objects.requireNonNull(packUrl, "packUrl");
    }

    /**
     * @return absolute filesystem path.
     */
    public Path path() {
        return path;
    }

    /**
     * @return file name.
     */
    public String fileName() {
        return fileName;
    }

    /**
     * @return size in bytes.
     */
    public long sizeBytes() {
        return sizeBytes;
    }

    /**
     * @return last modified instant.
     */
    public Instant lastModified() {
        return lastModified;
    }

    /**
     * @return optional SHA-1 checksum string.
     */
    public Optional<String> sha1Hex() {
        return sha1Hex;
    }

    /**
     * @return optional download URL.
     */
    public Optional<String> packUrl() {
        return packUrl;
    }

    /**
     * @return human-readable date/time for info command and logs.
     */
    public String formattedLastModified() {
        return DISPLAY_TIME.format(lastModified);
    }

    /**
     * @return human-readable binary size string.
     */
    public String formattedSize() {
        final String[] units = {"B", "KB", "MB", "GB", "TB"};
        double value = sizeBytes;
        int unitIndex = 0;
        while (value >= 1024 && unitIndex < units.length - 1) {
            value /= 1024;
            unitIndex++;
        }
        return String.format(Locale.ROOT, "%.2f %s", value, units[unitIndex]);
    }

    /**
     * Builds multiline description for command output.
     *
     * @return text description of all key fields.
     */
    public String asDescription() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Имя: ").append(fileName).append('\n');
        builder.append("Размер: ").append(formattedSize()).append(" (").append(sizeBytes).append(" bytes)").append('\n');
        builder.append("Изменён: ").append(formattedLastModified()).append('\n');
        builder.append("SHA1: ").append(sha1Hex.orElse("не вычислен")).append('\n');
        builder.append("URL: ").append(packUrl.orElse("не сформирован"));
        return builder.toString();
    }
}
