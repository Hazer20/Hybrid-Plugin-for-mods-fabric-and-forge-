package dev.sanguine.bridge;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Replacement map for frequent 1.20.4 -> 1.21.8 datapack text migrations.
 *
 * <p>Important: there is no guaranteed exhaustive auto-migration for every custom datapack command,
 * but this set covers common vanilla identifiers that changed style or break due to namespacing.
 */
public final class BridgeRuleSet {
    private BridgeRuleSet() {
    }

    public static Map<String, String> defaultCommandReplacements() {
        Map<String, String> replacements = new LinkedHashMap<>();

        // ---- Attributes: force fully-qualified style ----
        replacements.put("generic.max_health", "minecraft:generic.max_health");
        replacements.put("generic.follow_range", "minecraft:generic.follow_range");
        replacements.put("generic.knockback_resistance", "minecraft:generic.knockback_resistance");
        replacements.put("generic.movement_speed", "minecraft:generic.movement_speed");
        replacements.put("generic.attack_damage", "minecraft:generic.attack_damage");
        replacements.put("generic.attack_speed", "minecraft:generic.attack_speed");
        replacements.put("generic.armor", "minecraft:generic.armor");
        replacements.put("generic.armor_toughness", "minecraft:generic.armor_toughness");
        replacements.put("generic.attack_knockback", "minecraft:generic.attack_knockback");
        replacements.put("generic.luck", "minecraft:generic.luck");
        replacements.put("zombie.spawn_reinforcements", "minecraft:zombie.spawn_reinforcements");
        replacements.put("horse.jump_strength", "minecraft:horse.jump_strength");

        // ---- Damage types / tags frequently referenced from functions ----
        replacements.put("damage_type", "minecraft:damage_type");

        // ---- Sanguine-specific placeholders; edit under your datapack IDs ----
        replacements.put("sanguine:ritual_core", "sanguine:ritual/ritual_core");

        return replacements;
    }
}
