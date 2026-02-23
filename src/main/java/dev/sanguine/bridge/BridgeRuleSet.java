package dev.sanguine.bridge;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Curated command replacements that frequently break 1.20.4 datapacks on 1.21.8.
 * Add new mappings as Sanguine evolves.
 */
public final class BridgeRuleSet {
    private BridgeRuleSet() {
    }

    public static Map<String, String> defaultCommandReplacements() {
        Map<String, String> replacements = new LinkedHashMap<>();

        // 1.21 unifies many attribute identifiers in vanilla content.
        replacements.put("generic.max_health", "minecraft:generic.max_health");
        replacements.put("generic.attack_damage", "minecraft:generic.attack_damage");
        replacements.put("generic.armor", "minecraft:generic.armor");

        // Sanguine datapack-specific namespaces can be remapped here when identifiers move.
        replacements.put("sanguine:ritual_core", "sanguine:ritual/ritual_core");

        return replacements;
    }
}
