package com.hazer.hazefishing.listener;

import com.hazer.hazefishing.HazerFishingPlugin;
import com.hazer.hazefishing.model.NFTFish;
import com.hazer.hazefishing.model.NFTRod;
import com.hazer.hazefishing.model.Rarity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public final class FishingListener implements Listener {
    private final HazerFishingPlugin plugin;

    public FishingListener(HazerFishingPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;
        Player player = event.getPlayer();
        ItemStack rodItem = player.getInventory().getItemInMainHand();

        Optional<NFTRod> rod = plugin.getNftRodManager().fromItem(rodItem);
        if (rod.isPresent() && !plugin.getSecurityManager().verifyRodUse(player, rod.get())) {
            event.setCancelled(true);
            player.sendMessage("§cNFT Rod security verification failed.");
            return;
        }

        Rarity rolled = rollRarity();
        if (rolled.ordinal() >= Rarity.DIVINE.ordinal()) {
            plugin.getRgbAnimationManager().triggerFishAnimation(player, rolled.name() + " FISH");
        }

        if (rolled == Rarity.NFT || ThreadLocalRandom.current().nextDouble(100) <= 0.0001) {
            NFTFish fish = plugin.getNftFishManager().generate(player, Rarity.NFT);
            plugin.getEventManager().broadcastNftCatch(player, fish);
        }
    }

    private Rarity rollRarity() {
        double roll = ThreadLocalRandom.current().nextDouble(100);
        double cursor = 0;
        for (Rarity rarity : Rarity.values()) {
            cursor += rarity.getChance();
            if (roll <= cursor) {
                return rarity;
            }
        }
        return Rarity.COMMON;
    }
}
