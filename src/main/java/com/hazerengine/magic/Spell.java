package com.hazerengine.magic;

import com.hazerengine.core.api.Identifiable;
import org.bukkit.entity.Player;

public interface Spell extends Identifiable { void cast(Player player); }
