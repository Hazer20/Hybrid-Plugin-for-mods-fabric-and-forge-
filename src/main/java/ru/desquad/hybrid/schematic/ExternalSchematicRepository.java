package ru.desquad.hybrid.schematic;

import ru.desquad.hybrid.DESHybridPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.*;
import java.util.*;

public class ExternalSchematicRepository {

    private final DESHybridPlugin plugin;
    private final Path dir;

    public ExternalSchematicRepository(DESHybridPlugin plugin) {
        this.plugin = plugin;
        this.dir = plugin.getDataFolder().toPath().resolve("schematics");
    }

    public void init() {
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать папку schematics", e);
        }
    }

    public Path getDir() {
        return dir;
    }

    public Set<String> listKeys() {
        Set<String> keys = new LinkedHashSet<>();
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir)) {
            for (Path p : ds) {
                String name = p.getFileName().toString();
                if (name.endsWith(".schem") || name.endsWith(".schematic") || name.endsWith(".litematic")) {
                    keys.add("ext:" + stripExt(name));
                }
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Ошибка чтения папки schematics: " + e.getMessage());
        }
        return keys;
    }

    public boolean isExternalKey(String key) {
        return key != null && key.startsWith("ext:");
    }

    public Optional<Path> resolveByKey(String key) {
        if (!isExternalKey(key)) return Optional.empty();
        String base = key.substring(4);
        Path a0 = dir.resolve(base + ".schem");
        Path a = dir.resolve(base + ".schematic");
        Path b = dir.resolve(base + ".litematic");
        if (Files.exists(a0)) return Optional.of(a0);
        if (Files.exists(a)) return Optional.of(a);
        if (Files.exists(b)) return Optional.of(b);
        return Optional.empty();
    }

    public void downloadFromUrl(String name, String url) throws Exception {
        String low = url.toLowerCase(Locale.ROOT);
        if (!(low.endsWith(".schem") || low.endsWith(".schematic") || low.endsWith(".litematic"))) {
            throw new IllegalArgumentException("URL должен заканчиваться на .schem/.schematic/.litematic");
        }

        String ext = low.endsWith(".litematic") ? ".litematic" : (low.endsWith(".schem") ? ".schem" : ".schematic");
        String safe = name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_\\-]", "_");
        Path target = dir.resolve(safe + ext);

        HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build();
        HttpRequest req = HttpRequest.newBuilder(URI.create(url)).GET().build();
        HttpResponse<InputStream> resp = client.send(req, HttpResponse.BodyHandlers.ofInputStream());
        if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
            throw new IOException("HTTP " + resp.statusCode());
        }

        try (InputStream in = resp.body()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private String stripExt(String n) {
        int i = n.lastIndexOf('.');
        return i > 0 ? n.substring(0, i) : n;
    }
}
