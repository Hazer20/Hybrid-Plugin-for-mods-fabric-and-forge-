package com.hybrid.rpgcharacter.service;

import com.hybrid.rpgcharacter.config.CharacterConfig;
import com.hybrid.rpgcharacter.data.CharacterData;
import net.minecraft.server.level.ServerPlayer;

/** Handles smooth per-player age progression. */
public class AgeService {
    private final HeightService heightService;

    public AgeService(HeightService heightService) {
        this.heightService = heightService;
    }

    /** Advances one player's age progress and applies yearly transitions when needed. */
    public boolean tickPlayerAge(ServerPlayer player, CharacterData data) {
        data.setAgeProgress(data.getAgeProgress() + 1L);
        if (data.getAgeProgress() >= CharacterConfig.AGE_PROGRESS_THRESHOLD.get()) {
            addAge(player, data);
            return true;
        }
        return false;
    }

    /** Adds one RPG year and recalculates height-dependent placeholders. */
    public void addAge(ServerPlayer player, CharacterData data) {
        data.setAge(data.getAge() + 1);
        data.setAgeProgress(0L);
        data.setHeightCm(heightService.calculateHeight(data.getGender(), data.getAge(), data.getGeneticsFactor(), data.getTraits()));
        data.setHeightScale(heightService.calculateScale(data.getHeightCm()));
        heightService.updatePlayerDimensions(player, data);
    }

    /** Returns the raw progress towards the next configured age threshold. */
    public long getAgeProgress(CharacterData data) {
        return data.getAgeProgress();
    }
}
