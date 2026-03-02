package com.hazer.hazefishing.manager;

import com.google.gson.JsonObject;
import com.hazer.hazefishing.HazerFishingPlugin;
import com.hazer.hazefishing.model.NFTFish;
import org.bukkit.entity.Player;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public final class DiscordWebhookManager {
    private final HazerFishingPlugin plugin;

    public DiscordWebhookManager(HazerFishingPlugin plugin) {
        this.plugin = plugin;
    }

    public void sendNftCatch(Player player, NFTFish fish) {
        String url = plugin.getConfig().getString("discord.webhook", "");
        if (url.isBlank()) return;
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                HttpURLConnection connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                JsonObject json = new JsonObject();
                json.addProperty("content", "⚡ " + player.getName() + " minted NFT Fish #" + fish.registryId() + " (" + fish.weight() + "g)");
                try (OutputStream output = connection.getOutputStream()) {
                    output.write(json.toString().getBytes(StandardCharsets.UTF_8));
                }
                connection.getInputStream().close();
            } catch (Exception exception) {
                plugin.getLogger().warning("Webhook failed: " + exception.getMessage());
            }
        });
    }
}
