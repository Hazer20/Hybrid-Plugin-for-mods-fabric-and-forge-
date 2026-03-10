package com.hazerengine.food;

import com.hazerengine.core.api.Identifiable;
import org.bukkit.Sound;
import org.bukkit.potion.PotionEffect;

import java.util.List;

public record CustomFoodProfile(String id, int hunger, float saturation, List<PotionEffect> effects, Sound eatSound) implements Identifiable { }
