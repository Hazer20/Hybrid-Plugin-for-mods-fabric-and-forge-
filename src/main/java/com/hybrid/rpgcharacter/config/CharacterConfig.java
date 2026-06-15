package com.hybrid.rpgcharacter.config;

import net.minecraftforge.common.ForgeConfigSpec;

/** Server-side tunables for age and character progression. */
public final class CharacterConfig {
    public static final ForgeConfigSpec SERVER_SPEC;
    public static final ForgeConfigSpec.LongValue AGE_PROGRESS_THRESHOLD;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("age");
        AGE_PROGRESS_THRESHOLD = builder.comment("Player ticks required to advance the RPG age by one year.")
                .defineInRange("ageProgressThreshold", 24000L * 30L, 1L, Long.MAX_VALUE);
        builder.pop();
        SERVER_SPEC = builder.build();
    }

    private CharacterConfig() {
    }
}
