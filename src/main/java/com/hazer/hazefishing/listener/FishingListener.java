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
            player.sendMessage("§cПроверка безопасности NFT-удочки не пройдена.");
            return;
        }

        Rarity rolled = rollRarity();
        if (rolled.ordinal() >= Rarity.DIVINE.ordinal()) {
            plugin.getRgbAnimationManager().triggerFishAnimation(player, "УЛОВ " + rolled.name());
        }

        if (rolled == Rarity.NFT || ThreadLocalRandom.current().nextDouble(100) <= 0.0001) {
            NFTFish fish = plugin.getNftFishManager().generate(player, Rarity.NFT);
            player.getInventory().addItem(plugin.getNftFishManager().toItem(fish));
            player.sendMessage("§dВы поймали NFT-рыбу #" + fish.registryId());
            plugin.getEventManager().broadcastNftCatch(player, fish);
        }
    }

    private Rarity rollRarity() {
        double roll = ThreadLocalRandom.current().nextDouble(100);
        double cursor = 0;
        for (Rarity rarity : Rarity.values()) {
            cursor += plugin.getAdminCommandManager().getChance(rarity);
            if (roll <= cursor) {
                return rarity;
            }
        }
        return Rarity.COMMON;
    }
}
