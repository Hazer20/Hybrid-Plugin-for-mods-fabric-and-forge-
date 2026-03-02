package com.hazer.hazefishing.model;

import net.kyori.adventure.text.format.TextColor;

import java.util.Locale;

public enum RodTier {
    RARE_ROD(TextColor.color(66, 165, 245), 1.20),
    EPIC_ROD(TextColor.color(171, 71, 188), 1.35),
    LEGENDARY_ROD(TextColor.color(255, 202, 40), 1.50),
    MYTHIC_ROD(TextColor.color(255, 112, 67), 1.65),
    GODLIKE_ROD(TextColor.color(229, 57, 53), 1.90),
    DIVINE_ROD(TextColor.color(124, 179, 66), 2.10),
    CELESTIAL_ROD(TextColor.color(41, 182, 246), 2.35),
    VOID_ROD(TextColor.color(94, 53, 177), 2.70),
    NFT_ROD(TextColor.color(255, 255, 255), 3.00);

    private final TextColor color;
    private final double rarityMultiplier;

    RodTier(TextColor color, double rarityMultiplier) {
        this.color = color;
        this.rarityMultiplier = rarityMultiplier;
    }

    public TextColor color() {
        return color;
    }

    public double rarityMultiplier() {
        return rarityMultiplier;
    }

    public static RodTier parse(String name) {
        return RodTier.valueOf(name.toUpperCase(Locale.ROOT));
    }
}
