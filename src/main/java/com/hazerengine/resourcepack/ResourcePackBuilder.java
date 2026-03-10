package com.hazerengine.resourcepack;

import com.hazerengine.items.CustomItem;

import java.io.IOException;
import java.nio.file.*;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ResourcePackBuilder {
    public Path generate(Path output, Collection<CustomItem> items) throws IOException {
        Path temp = Files.createTempDirectory("hazer-pack");
        Files.createDirectories(temp.resolve("assets/hazerengine/textures"));
        Files.createDirectories(temp.resolve("assets/hazerengine/models"));
        Files.createDirectories(temp.resolve("assets/hazerengine/sounds"));
        Files.createDirectories(temp.resolve("assets/hazerengine/fonts"));

        for (CustomItem item : items) {
            Path model = temp.resolve("assets/hazerengine/models/" + item.id() + ".json");
            String json = "{\"parent\":\"item/generated\",\"textures\":{\"layer0\":\"hazerengine:item/" + item.id() + "\"}}";
            Files.writeString(model, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        }

        zipDirectory(temp, output);
        return output;
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
