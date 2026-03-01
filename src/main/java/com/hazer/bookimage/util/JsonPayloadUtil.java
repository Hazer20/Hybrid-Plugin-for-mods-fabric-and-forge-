package com.hazer.bookimage.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Utility to create lightweight JSON + Base64 payload with source metadata.
 */
public final class JsonPayloadUtil {

    private static final Gson GSON = new Gson();

    private JsonPayloadUtil() {
    }

    public static String encodeSourceUrl(String url) {
        JsonObject object = new JsonObject();
        object.addProperty("type", "bookimage");
        object.addProperty("url", url);
        String json = GSON.toJson(object);
        return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }
}
