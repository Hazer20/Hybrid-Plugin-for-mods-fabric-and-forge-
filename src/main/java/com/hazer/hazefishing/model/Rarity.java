package com.hazer.hazefishing.model;

import java.util.Locale;

public enum Rarity {
    COMMON(60.0),
    UNCOMMON(24.0),
    RARE(10.0),
    EPIC(4.0),
    LEGENDARY(1.0),
    MYTHIC(0.5),
    GODLIKE(0.2),
    DIVINE(0.1),
    CELESTIAL(0.05),
    VOID(0.01),
    NFT(0.0001);

    private final double chance;

    Rarity(double chance) {
        this.chance = chance;
    }

    public double getChance() {
        return chance;
    }

    public static Rarity parse(String value) {
        return Rarity.valueOf(value.toUpperCase(Locale.ROOT));
    }
}
