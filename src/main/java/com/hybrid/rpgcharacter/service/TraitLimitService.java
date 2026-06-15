package com.hybrid.rpgcharacter.service;

import com.hybrid.rpgcharacter.data.CharacterTrait;
import java.util.EnumMap;
import java.util.Map;

/** Tracks server-wide rare trait counts and caps. */
public class TraitLimitService {
    private final Map<CharacterTrait, Integer> counts = new EnumMap<>(CharacterTrait.class);
    private final Map<CharacterTrait, Integer> limits = new EnumMap<>(CharacterTrait.class);

    public TraitLimitService() {
        limits.put(CharacterTrait.DWARFISM, 2);
        limits.put(CharacterTrait.TALL, 1);
        limits.put(CharacterTrait.SHORT, 2);
    }

    /** Returns true when another player can receive the requested trait. */
    public boolean canAssign(CharacterTrait trait) {
        return counts.getOrDefault(trait, 0) < limits.getOrDefault(trait, Integer.MAX_VALUE);
    }

    /** Replaces all counters, useful after login/logout recalculation. */
    public void replaceCounts(Map<CharacterTrait, Integer> recalculatedCounts) {
        counts.clear();
        counts.putAll(recalculatedCounts);
    }
}
