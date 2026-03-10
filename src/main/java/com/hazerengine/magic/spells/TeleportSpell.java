package com.hazerengine.magic.spells;

import com.hazerengine.magic.Spell;
import org.bukkit.entity.Player;

public class TeleportSpell implements Spell { public String id(){return "teleport_spell";} public void cast(Player player){ player.teleport(player.getLocation().add(0,0,6));} }
