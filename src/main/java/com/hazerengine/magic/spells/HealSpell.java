package com.hazerengine.magic.spells;

import com.hazerengine.magic.Spell;
import org.bukkit.entity.Player;

public class HealSpell implements Spell { public String id(){return "heal_spell";} public void cast(Player player){ player.setHealth(Math.min(player.getMaxHealth(), player.getHealth()+6));} }
