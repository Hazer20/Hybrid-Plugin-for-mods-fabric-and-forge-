package com.hazerengine.abilities;

import com.hazerengine.core.api.Identifiable;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public interface Ability extends Identifiable {
    default void onAttack(Player player, EntityDamageByEntityEvent event) {}
    default void onUse(Player player) {}
    default void onTick(Player player) {}
    default void onKill(Player player) {}
    default void onEquip(Player player) {}
    default void onUnequip(Player player) {}
    @Override default String id() { return getClass().getSimpleName().toLowerCase(); }
}
