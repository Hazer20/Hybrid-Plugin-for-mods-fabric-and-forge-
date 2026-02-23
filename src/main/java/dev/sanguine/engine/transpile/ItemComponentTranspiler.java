package dev.sanguine.engine.transpile;

import java.util.regex.Pattern;

public class ItemComponentTranspiler {
    private static final Pattern LEGACY_TAG_PATTERN = Pattern.compile("\\{.*tag:\\{", Pattern.CASE_INSENSITIVE);

    public String convertLegacyNbtInGiveLikeCommand(String command) {
        if (!command.startsWith("give ") && !command.startsWith("item ")) {
            return command;
        }

        String transformed = command
            .replace("tag:{", "components:{")
            .replace("display:{Name:", "minecraft:custom_name=")
            .replace("Lore:[", "minecraft:lore=[");

        if (LEGACY_TAG_PATTERN.matcher(command).find() && !transformed.contains("minecraft:custom_data")) {
            transformed = transformed + " # migrated_to_components";
        }
        return transformed;
    }
}
