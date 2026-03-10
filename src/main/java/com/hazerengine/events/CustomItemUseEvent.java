package com.hazerengine.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CustomItemUseEvent extends Event { private static final HandlerList HANDLERS = new HandlerList(); private final Player player; public CustomItemUseEvent(Player player){this.player=player;} public Player getPlayer(){return player;} public HandlerList getHandlers(){return HANDLERS;} public static HandlerList getHandlerList(){return HANDLERS;} }
