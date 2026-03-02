package com.hazer2_0.radio;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class RadioPlacementListener implements Listener {

    private static final List<BlockFace> FACES = List.of(BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN);

    private final JavaPlugin plugin;
    private final PlacedRadioRegistry placedRegistry;
    private final NamespacedKey radioChannelKey;

    public RadioPlacementListener(JavaPlugin plugin, PlacedRadioRegistry placedRegistry) {
        this.plugin = plugin;
        this.placedRegistry = placedRegistry;
        this.radioChannelKey = new NamespacedKey(plugin, "hazer_radio_channel");
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        Block block = event.getBlockPlaced();
        if (block.getType() != Material.NOTE_BLOCK) {
            return;
        }
        ItemStack item = event.getItemInHand();
        if (item == null || !item.hasItemMeta()) {
            return;
        }

        String channel = item.getItemMeta().getPersistentDataContainer().get(radioChannelKey, PersistentDataType.STRING);
        if (channel == null || channel.isBlank()) {
            return;
        }

        placedRegistry.setChannel(block.getLocation(), channel);
        placedRegistry.save();

        Player player = event.getPlayer();
        player.sendMessage("§aРация установлена, работает в штатном режиме.");
        player.sendMessage("§eРежим: прослушка.");
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() != Material.NOTE_BLOCK) {
            return;
        }
        if (placedRegistry.getChannel(event.getBlock().getLocation()) != null) {
            placedRegistry.remove(event.getBlock().getLocation());
            placedRegistry.save();
            event.getPlayer().sendMessage("§cРация демонтирована.");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onLeverToggle(BlockRedstoneEvent event) {
        Block lever = event.getBlock();
        if (lever.getType() != Material.LEVER) {
            return;
        }

        Block radioBlock = null;
        for (BlockFace face : FACES) {
            Block near = lever.getRelative(face);
            if (near.getType() == Material.NOTE_BLOCK && placedRegistry.getChannel(near.getLocation()) != null) {
                radioBlock = near;
                break;
            }
        }

        if (radioBlock == null) {
            return;
        }

        final Block targetRadioBlock = radioBlock;

        String channel = placedRegistry.getChannel(targetRadioBlock.getLocation());
        if (channel == null) {
            return;
        }

        boolean answerMode = event.getNewCurrent() > 0;
        int radius = plugin.getConfig().getInt("radio.receive-radius", 16);
        String ownMessage = answerMode
                ? "§aРация перешла в режим ответа. Теперь вы можете говорить."
                : "§eРация перешла в режим прослушки.";

        String remoteMessage = answerMode
                ? "§bКанал [" + channel + "]: одна из раций перешла в режим ответа. Для прослушки выключите рычаг."
                : "§bКанал [" + channel + "]: рация вернулась в режим прослушки.";

        targetRadioBlock.getWorld().getPlayers().forEach(player -> {
            if (player.getLocation().distance(targetRadioBlock.getLocation()) <= radius) {
                player.sendMessage(ownMessage);
            }
        });

        placedRegistry.findByChannel(targetRadioBlock.getWorld(), channel, targetRadioBlock.getLocation(), plugin.getConfig().getInt("radio.max-channel-range", 256))
                .stream()
                .filter(loc -> loc.distance(targetRadioBlock.getLocation()) > 1.0)
                .forEach(loc -> loc.getWorld().getPlayers().forEach(player -> {
                    if (player.getLocation().distance(loc) <= radius) {
                        player.sendMessage(remoteMessage);
                    }
                }));
    }
}
