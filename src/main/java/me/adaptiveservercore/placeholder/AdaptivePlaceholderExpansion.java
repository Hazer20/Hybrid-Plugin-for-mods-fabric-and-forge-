package me.adaptiveservercore.placeholder;

import me.adaptiveservercore.AdaptiveServerCore;
import me.adaptiveservercore.height.HeightManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class AdaptivePlaceholderExpansion extends PlaceholderExpansion {

    private final AdaptiveServerCore plugin;
    private final HeightManager heightManager;

    public AdaptivePlaceholderExpansion(AdaptiveServerCore plugin, HeightManager heightManager) {
        this.plugin = plugin;
        this.heightManager = heightManager;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "player";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Hazer_2_0";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        double height = heightManager.getHeight(player);
        double scale = height / HeightManager.DEFAULT_HEIGHT;

        return switch (params.toLowerCase(Locale.ROOT)) {
            case "height" -> Double.toString(height);
            case "height_formatted" -> String.format(Locale.US, "%.2f", height);
            case "hitbox" -> String.format(Locale.US, "%.2f x %.2f", 0.6D * scale, 1.8D * scale);
            case "attack_reach" -> String.format(Locale.US, "%.2f", heightManager.getAttackReach(player));
            default -> null;
        };
    }
}
