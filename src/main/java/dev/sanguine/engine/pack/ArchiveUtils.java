package dev.sanguine.engine.pack;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public final class ArchiveUtils {
    private ArchiveUtils() {
    }

    public static void unpackInput(Path inputDir, Path outDir) throws IOException {
        if (!Files.exists(inputDir)) {
            return;
        }
        try (var stream = Files.list(inputDir)) {
            stream.forEach(path -> {
                try {
                    if (Files.isDirectory(path)) {
                        copyTree(path, outDir);
                    } else if (path.getFileName().toString().endsWith(".zip")) {
                        unzip(path, outDir);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (RuntimeException ex) {
            if (ex.getCause() instanceof IOException io) {
                throw io;
            }
            throw ex;
        }
    }

    public static void unzip(Path zip, Path outDir) throws IOException {
        Files.createDirectories(outDir);
        try (InputStream in = Files.newInputStream(zip); ZipInputStream zis = new ZipInputStream(in)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path target = outDir.resolve(entry.getName()).normalize();
                if (!target.startsWith(outDir)) {
                    continue;
                }
                if (entry.isDirectory()) {
                    Files.createDirectories(target);
                } else {
                    Files.createDirectories(target.getParent());
                    try (OutputStream out = Files.newOutputStream(target)) {
                        zis.transferTo(out);
                    }
                }
            }
        }
    }

    public static void zipDirectory(Path sourceDir, Path zipFile) throws IOException {
        Files.createDirectories(zipFile.getParent());
        try (OutputStream out = Files.newOutputStream(zipFile); ZipOutputStream zos = new ZipOutputStream(out)) {
            try (var paths = Files.walk(sourceDir)) {
                paths.filter(Files::isRegularFile).forEach(path -> {
                    try {
                        String name = sourceDir.relativize(path).toString().replace('\\', '/');
                        zos.putNextEntry(new ZipEntry(name));
                        Files.copy(path, zos);
                        zos.closeEntry();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
    }

    public static void copyTree(Path source, Path target) throws IOException {
        Files.createDirectories(target);
        try (var paths = Files.walk(source)) {
            paths.forEach(path -> {
                try {
                    Path rel = source.relativize(path);
                    Path dst = target.resolve(rel);
                    if (Files.isDirectory(path)) {
                        Files.createDirectories(dst);
                    } else {
                        Files.createDirectories(dst.getParent());
                        Files.copy(path, dst, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    public static void cleanDirectory(Path directory) throws IOException {
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
            return;
        }
        try (var paths = Files.walk(directory)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    if (!path.equals(directory)) {
                        Files.deleteIfExists(path);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}
