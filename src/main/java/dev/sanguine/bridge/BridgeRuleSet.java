package dev.sanguine.bridge;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Centralized text migration rules for datapack content.
 */
public final class BridgeRuleSet {
    private BridgeRuleSet() {
    }

    public static Map<String, String> defaultCommandReplacements() {
        Map<String, String> replacements = new LinkedHashMap<>();

        // ---- Attributes (1.20 style -> 1.21 namespaced style) ----
        replacements.put("generic.max_health", "minecraft:generic.max_health");
        replacements.put("generic.follow_range", "minecraft:generic.follow_range");
        replacements.put("generic.knockback_resistance", "minecraft:generic.knockback_resistance");
        replacements.put("generic.movement_speed", "minecraft:generic.movement_speed");
        replacements.put("generic.flying_speed", "minecraft:generic.flying_speed");
        replacements.put("generic.attack_damage", "minecraft:generic.attack_damage");
        replacements.put("generic.attack_speed", "minecraft:generic.attack_speed");
        replacements.put("generic.armor", "minecraft:generic.armor");
        replacements.put("generic.armor_toughness", "minecraft:generic.armor_toughness");
        replacements.put("generic.attack_knockback", "minecraft:generic.attack_knockback");
        replacements.put("generic.luck", "minecraft:generic.luck");
        replacements.put("horse.jump_strength", "minecraft:horse.jump_strength");
        replacements.put("zombie.spawn_reinforcements", "minecraft:zombie.spawn_reinforcements");

        // ---- Operations / resource keys that often appear raw in old packs ----
        replacements.put("damage_type", "minecraft:damage_type");
        replacements.put("item_modifier", "minecraft:item_modifier");
        replacements.put("loot_table", "minecraft:loot_table");
        replacements.put("predicate", "minecraft:predicate");
        replacements.put("structure", "minecraft:structure");
        replacements.put("recipe", "minecraft:recipe");

        // ---- Sanguine project placeholders (edit these for real datapack paths) ----
        replacements.put("sanguine:ritual_core", "sanguine:ritual/ritual_core");

        return replacements;
    }
}
