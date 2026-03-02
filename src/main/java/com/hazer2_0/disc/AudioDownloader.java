package com.hazer2_0.disc;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;

public class AudioDownloader {
    private final HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();

    public Path download(String url, Path target, long maxBytes) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(URI.create(url)).GET().build();
        HttpResponse<InputStream> response = client.send(req, HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("HTTP " + response.statusCode());
        }
        Files.createDirectories(target.getParent());
        try (InputStream is = response.body(); var os = Files.newOutputStream(target)) {
            byte[] buf = new byte[8192];
            long total = 0;
            int r;
            while ((r = is.read(buf)) != -1) {
                total += r;
                if (total > maxBytes) throw new IOException("File exceeds max size");
                os.write(buf, 0, r);
            }
        }
        return target;
    }
}
