package com.hazerengine.resourcepack;

import com.hazerengine.api.ResourcePackAPI;
import com.hazerengine.items.CustomItem;

import java.io.IOException;
import java.nio.file.*;
import java.util.Collection;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ResourcePackBuilder {
    public Path generate(Path output, Collection<CustomItem> items, ResourcePackAPI extensions) throws IOException {
        Path temp = Files.createTempDirectory("hazer-pack");
        Path assets = temp.resolve("assets/hazerengine");
        Files.createDirectories(assets.resolve("textures"));
        Files.createDirectories(assets.resolve("models"));
        Files.createDirectories(assets.resolve("sounds"));
        Files.createDirectories(assets.resolve("fonts"));

        for (CustomItem item : items) {
            Path model = assets.resolve("models/" + item.id() + ".json");
            String json = "{\"parent\":\"item/generated\",\"textures\":{\"layer0\":\"hazerengine:item/" + item.id() + "\"}}";
            Files.writeString(model, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        }

        if (extensions != null) {
            copyAssets(extensions.textures(), assets.resolve("textures"));
            copyAssets(extensions.models(), assets.resolve("models"));
            copyAssets(extensions.sounds(), assets.resolve("sounds"));
        }

        zipDirectory(temp, output);
        return output;
    }

    private void copyAssets(Map<String, java.io.File> source, Path targetDir) throws IOException {
        for (Map.Entry<String, java.io.File> entry : source.entrySet()) {
            if (entry.getValue().exists()) {
                String ext = extension(entry.getValue().getName());
                Files.copy(entry.getValue().toPath(), targetDir.resolve(entry.getKey() + ext), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    private String extension(String fileName) {
        int idx = fileName.lastIndexOf('.');
        return idx > -1 ? fileName.substring(idx) : ".dat";
    }

    private void zipDirectory(Path sourceDir, Path outputZip) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(outputZip))) {
            Files.walk(sourceDir).filter(Files::isRegularFile).forEach(path -> {
                ZipEntry zipEntry = new ZipEntry(sourceDir.relativize(path).toString());
                try {
                    zos.putNextEntry(zipEntry);
                    zos.write(Files.readAllBytes(path));
                    zos.closeEntry();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}
