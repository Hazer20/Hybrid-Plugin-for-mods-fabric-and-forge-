package com.hazerexample;

import com.hazerengine.abilities.impl.ShadowStepAbility;
import com.hazerengine.api.HazerAPI;
import com.hazerengine.api.ItemAPI;
import com.hazerengine.items.CustomItem;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

public class HazerExamplePlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        ItemAPI itemAPI = HazerAPI.getItemAPI();
        if (itemAPI == null) {
            getLogger().warning("HazerEngine ItemAPI is unavailable");
            return;
        }

        CustomItem item = itemAPI.createItem("shadow_blade")
                .name("§5Shadow Blade")
                .material(Material.NETHERITE_SWORD)
                .modelData(2001)
                .ability(new ShadowStepAbility())
                .build();

        itemAPI.register(item);
        getLogger().info("Registered shadow_blade using HazerEngine API");
    }
}
