package com.hybrid.rpgcharacter.service;

import com.hybrid.rpgcharacter.data.CharacterData;
import com.hybrid.rpgcharacter.data.CharacterTrait;
import com.hybrid.rpgcharacter.data.Gender;
import net.minecraft.server.level.ServerPlayer;

/** Central place for height, scale, eye-height and dimension extension points. */
public class HeightService {
    private static final float BASE_PLAYER_HEIGHT_CM = 180.0F;

    /** Calculates final height in centimeters from age, gender, genetics and rare traits. */
    public int calculateHeight(Gender gender, int age, float geneticsFactor, Iterable<CharacterTrait> traits) {
        float base = baseHeight(gender, age);
        float traitModifier = 0.0F;
        for (CharacterTrait trait : traits) {
            if (trait == CharacterTrait.DWARFISM) traitModifier -= 35.0F;
            if (trait == CharacterTrait.TALL) traitModifier += 12.0F;
            if (trait == CharacterTrait.SHORT) traitModifier -= 10.0F;
        }
        return Math.round(Math.max(95.0F, base + geneticsFactor + traitModifier));
    }

    /** Converts centimeters into a safe player-render scale placeholder. */
    public float calculateScale(int heightCm) {
        return Math.max(0.65F, Math.min(1.25F, heightCm / BASE_PLAYER_HEIGHT_CM));
    }

    /** Extension point for future hitbox/model dimension updates. */
    public void updatePlayerDimensions(ServerPlayer player, CharacterData data) {
        player.refreshDimensions();
    }

    /** Calculates the eye height matching the scaled body. */
    public float calculateEyeHeight(CharacterData data) {
        return 1.62F * data.getHeightScale();
    }

    private float baseHeight(Gender gender, int age) {
        if (gender == Gender.FEMALE) {
            if (age <= 16) return 162.0F;
            if (age == 17) return 163.0F;
            return 163.5F;
        }
        if (age <= 16) return 173.0F;
        if (age == 17) return 175.0F;
        if (age == 18) return 176.0F;
        if (age == 19) return 176.5F;
        return 177.0F;
    }
}
