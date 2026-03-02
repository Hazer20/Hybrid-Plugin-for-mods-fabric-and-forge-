package ru.desquad.hybrid.npc;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.desquad.hybrid.gui.GUIFactory;

public class NPCTokenUseListener implements Listener {

    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    private final BuilderNPCManager manager;

    public NPCTokenUseListener(BuilderNPCManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onUse(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        ItemStack item = event.getItem();
        if (item == null) return;
        if (item.getType() != GUIFactory.createNpcTokenItem().getType()) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || meta.displayName() == null) return;
        String name = PLAIN.serialize(meta.displayName());
        if (!name.contains("Призыв строителя DE Squad")) return;

        event.setCancelled(true);
        manager.spawnNpcAt(event.getClickedBlock().getLocation().add(0.5, 1, 0.5));
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            event.getPlayer().getInventory().setItemInMainHand(null);
        }
        event.getPlayer().sendMessage("§aNPC-строитель призван на выбранной позиции.");
    }
}
