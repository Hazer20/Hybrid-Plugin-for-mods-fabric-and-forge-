package com.hazer.hazefishing.model;

import java.time.Instant;
import java.util.UUID;

public record NFTRod(
        UUID rodId,
        String serial,
        UUID owner,
        Instant createdAt,
        int level,
        int prestige,
        String uniqueName,
        String gradientA,
        String gradientB,
        double rarityScore,
        String rarityRatio,
        long immutableSeed,
        RodTier tier,
        int durability,
        int maxDurability,
        long repairCost,
        boolean soulbound
) {
}
