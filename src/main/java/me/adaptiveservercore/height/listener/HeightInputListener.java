package me.adaptiveservercore.height.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.adaptiveservercore.AdaptiveServerCore;
import me.adaptiveservercore.height.HeightManager;
import me.adaptiveservercore.height.gui.HeightGui;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Locale;

public class HeightInputListener implements Listener {

    private final HeightManager heightManager;
    private final HeightGui heightGui;

    public HeightInputListener(AdaptiveServerCore plugin, HeightManager heightManager, HeightGui heightGui) {
        this.heightManager = heightManager;
        this.heightGui = heightGui;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (!HeightGui.TITLE.equals(event.getView().getTitle())) {
            return;
        }

        event.setCancelled(true);
        if (event.getCurrentItem() == null) {
            return;
        }

        boolean used = heightGui.handleClick(player, event.getCurrentItem());
        if (used) {
            player.closeInventory();
        }
    }

    @EventHandler
    public void onChatInput(AsyncChatEvent event) {
        Player player = event.getPlayer();
        if (!heightGui.isAwaitingChatInput(player)) {
            return;
        }

        event.setCancelled(true);
        String message = PlainTextComponentSerializer.plainText().serialize(event.message()).trim();

        if (message.equalsIgnoreCase("отмена")) {
            heightGui.clearAwaiting(player);
            player.sendMessage("§eВвод роста отменён.");
            return;
        }

        double value;
        try {
            value = Double.parseDouble(message.replace(',', '.'));
        } catch (NumberFormatException e) {
            player.sendMessage("§cВведите корректное число или напишите §fотмена§c.");
            return;
        }

        double min = heightManager.getMinHeight();
        double max = heightManager.getMaxHeight();
        if (value < min || value > max) {
            player.sendMessage("§cДопустимый диапазон: от §f" + String.format(Locale.US, "%.2f", min)
                    + " §cдо §f" + String.format(Locale.US, "%.2f", max));
            return;
        }

        heightGui.clearAwaiting(player);
        if (heightManager.setHeight(player, value)) {
            player.sendMessage("§aРост успешно установлен: §f" + String.format(Locale.US, "%.2f", value));
        }
    }
}
