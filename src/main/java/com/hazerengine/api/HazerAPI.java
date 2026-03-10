package com.hazerengine.api;

import org.bukkit.Bukkit;

/**
 * Public access facade for external plugins.
 */
public final class HazerAPI {
    private HazerAPI() {}

    public static ItemAPI getItemAPI() {
        return Bukkit.getServicesManager().load(ItemAPI.class);
    }

    public static AbilityAPI getAbilityAPI() {
        return Bukkit.getServicesManager().load(AbilityAPI.class);
    }

    public static RecipeAPI getRecipeAPI() {
        return Bukkit.getServicesManager().load(RecipeAPI.class);
    }

    public static GUIAPI getGUIAPI() {
        return Bukkit.getServicesManager().load(GUIAPI.class);
    }

    public static EconomyAPI getEconomyAPI() {
        return Bukkit.getServicesManager().load(EconomyAPI.class);
    }

    public static NPCAPI getNPCAPI() {
        return Bukkit.getServicesManager().load(NPCAPI.class);
    }

    public static ResourcePackAPI getResourcePackAPI() {
        return Bukkit.getServicesManager().load(ResourcePackAPI.class);
    }
}
