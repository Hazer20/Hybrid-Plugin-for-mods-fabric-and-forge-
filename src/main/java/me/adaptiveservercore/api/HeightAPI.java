package me.adaptiveservercore.api;

import org.bukkit.entity.Player;

public final class HeightAPI {

    private static HeightService service;

    private HeightAPI() {
    }

    public static void initialize(HeightService heightService) {
        service = heightService;
    }

    public static boolean setHeight(Player player, double height) {
        return service != null && service.setHeight(player, height);
    }

    public static double getHeight(Player player) {
        return service == null ? 1.8D : service.getHeight(player);
    }

    public static boolean resetHeight(Player player) {
        return service != null && service.resetHeight(player);
    }

    public static void registerHeightType(String name, double value) {
        if (service != null) {
            service.registerHeightType(name, value);
        }
    }

    public static boolean isHeightAnimating(Player player) {
        return service != null && service.isHeightAnimating(player);
    }
}
