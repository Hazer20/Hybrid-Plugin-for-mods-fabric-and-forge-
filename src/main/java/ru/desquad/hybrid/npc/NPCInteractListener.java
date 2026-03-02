package ru.desquad.hybrid.npc;

import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import ru.desquad.hybrid.DESHybridPlugin;

public class NPCInteractListener implements Listener {

    private final DESHybridPlugin plugin;
    private final BuilderNPCManager manager;

    public NPCInteractListener(DESHybridPlugin plugin, BuilderNPCManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler
    public void onInteract(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof Villager villager)) return;
        if (manager.getNpc() == null) return;
        if (!villager.getUniqueId().equals(manager.getNpc().getUniqueId())) return;
        event.setCancelled(true);
        manager.requestOrder(event.getPlayer());
    }
}
