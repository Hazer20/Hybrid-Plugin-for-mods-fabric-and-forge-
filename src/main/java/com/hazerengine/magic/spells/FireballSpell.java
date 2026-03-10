package com.hazerengine.magic.spells;

import com.hazerengine.magic.Spell;
import org.bukkit.entity.Player;

public class FireballSpell implements Spell { public String id(){return "fireball_spell";} public void cast(Player player){ player.launchProjectile(org.bukkit.entity.Fireball.class);} }
