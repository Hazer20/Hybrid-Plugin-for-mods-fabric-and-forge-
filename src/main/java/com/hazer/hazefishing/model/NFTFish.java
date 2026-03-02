package com.hazer.hazefishing.model;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record NFTFish(
        long registryId,
        UUID fishId,
        UUID owner,
        double weight,
        double length,
        String geneticCode,
        double rarityFactor,
        long worldRank,
        Rarity rarity,
        String element,
        Map<String, String> traits,
        Instant createdAt,
        long seed
) {
}
