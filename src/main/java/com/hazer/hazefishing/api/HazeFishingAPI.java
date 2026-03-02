package com.hazer.hazefishing.api;

import com.hazer.hazefishing.HazerFishingPlugin;
import com.hazer.hazefishing.model.NFTFish;
import com.hazer.hazefishing.model.NFTRod;
import com.hazer.hazefishing.model.Rarity;
import com.hazer.hazefishing.model.RodTier;
import org.bukkit.entity.Player;

public final class HazeFishingAPI {
    private final HazerFishingPlugin plugin;

    public HazeFishingAPI(HazerFishingPlugin plugin) {
        this.plugin = plugin;
    }

    public NFTRod mintRod(Player player, RodTier tier) {
        return plugin.getNftRodManager().mint(player, tier);
    }

    public NFTFish createFish(Player player, Rarity rarity) {
        return plugin.getNftFishManager().generate(player, rarity);
    }
}
