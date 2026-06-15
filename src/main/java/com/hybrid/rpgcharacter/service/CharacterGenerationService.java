package com.hybrid.rpgcharacter.service;

import com.hybrid.rpgcharacter.data.BodyType;
import com.hybrid.rpgcharacter.data.CharacterData;
import com.hybrid.rpgcharacter.data.CharacterTrait;
import com.hybrid.rpgcharacter.data.Gender;
import java.util.Random;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;

/** Creates first-login character profiles exactly once. */
public class CharacterGenerationService {
    private final HeightService heightService;
    private final TraitLimitService traitLimitService;

    public CharacterGenerationService(HeightService heightService, TraitLimitService traitLimitService) {
        this.heightService = heightService;
        this.traitLimitService = traitLimitService;
    }

    /** Initializes a profile if it was never generated before. */
    public CharacterData initializeIfNeeded(ServerPlayer player, CharacterData existing) {
        if (existing.isInitialized()) {
            return existing;
        }
        UUID id = player.getUUID();
        Random random = new Random(id.getMostSignificantBits() ^ System.nanoTime());
        existing.setPlayerId(id);
        existing.setCharacterName(player.getGameProfile().getName());
        existing.setGeneticsSeed(random.nextLong());
        Random genetics = new Random(existing.getGeneticsSeed());
        existing.setGender(genetics.nextBoolean() ? Gender.MALE : Gender.FEMALE);
        existing.setGeneticsFactor(generateGeneticsFactor(genetics));
        existing.setBodyType(BodyType.values()[genetics.nextInt(BodyType.values().length)]);
        maybeAssignRareTrait(existing, genetics);
        existing.setAge(16);
        existing.setHeightCm(heightService.calculateHeight(existing.getGender(), existing.getAge(), existing.getGeneticsFactor(), existing.getTraits()));
        existing.setHeightScale(heightService.calculateScale(existing.getHeightCm()));
        existing.setWeightKg(calculateInitialWeight(existing));
        existing.setInitialized(true);
        return existing;
    }

    private float generateGeneticsFactor(Random random) {
        int bucket = random.nextInt(100);
        if (bucket < 5) return -16.0F + random.nextFloat() * 6.0F;
        if (bucket < 20) return -9.0F + random.nextFloat() * 5.0F;
        if (bucket < 80) return -3.0F + random.nextFloat() * 6.0F;
        if (bucket < 95) return 4.0F + random.nextFloat() * 5.0F;
        return 10.0F + random.nextFloat() * 8.0F;
    }

    private void maybeAssignRareTrait(CharacterData data, Random random) {
        CharacterTrait[] candidates = {CharacterTrait.DWARFISM, CharacterTrait.TALL, CharacterTrait.SHORT};
        CharacterTrait candidate = candidates[random.nextInt(candidates.length)];
        if (random.nextFloat() < 0.08F && traitLimitService.canAssign(candidate)) {
            data.getTraits().add(candidate);
        } else {
            data.getTraits().add(CharacterTrait.NONE);
        }
    }

    private float calculateInitialWeight(CharacterData data) {
        float base = data.getHeightCm() - 105.0F;
        return switch (data.getBodyType()) {
            case THIN -> base * 0.88F;
            case ATHLETIC -> base * 1.08F;
            case HEAVY -> base * 1.22F;
            case NORMAL -> base;
        };
    }
}
