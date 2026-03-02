package com.hazer.bookimage.util;

import java.net.URI;
import java.net.URISyntaxException;

public final class UrlUtils {

    private UrlUtils() {
    }

    public static boolean isValidImageUrl(String raw) {
        if (raw == null || raw.isBlank()) {
            return false;
        }

        try {
            URI uri = new URI(raw.trim());
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                return false;
            }
            if (uri.getHost() == null || uri.getHost().isBlank()) {
                return false;
            }
            String path = uri.getPath();
            if (path == null) {
                return false;
            }
            String lowerPath = path.toLowerCase();
            return lowerPath.endsWith(".png") || lowerPath.endsWith(".jpg") || lowerPath.endsWith(".jpeg");
        } catch (URISyntaxException ex) {
            return false;
        }
    }
}
