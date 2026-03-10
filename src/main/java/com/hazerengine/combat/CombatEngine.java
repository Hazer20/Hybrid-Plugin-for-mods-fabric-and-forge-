package com.hazerengine.combat;

import com.hazerengine.core.HazerEnginePlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class CombatEngine implements Listener {
    private final HazerEnginePlugin plugin;

    public CombatEngine(HazerEnginePlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        event.setDamage(event.getDamage() * 1.1);
    }
}
