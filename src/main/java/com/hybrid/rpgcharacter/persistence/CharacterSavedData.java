package com.hybrid.rpgcharacter.persistence;

import com.hybrid.rpgcharacter.data.CharacterData;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;

/** Forge SavedData container used instead of external databases. */
public class CharacterSavedData extends SavedData {
    public static final String DATA_NAME = "rpgcharacter_profiles";
    private final Map<UUID, CharacterData> profiles = new HashMap<>();

    public CharacterSavedData() {
    }

    public static void registerFactory() {
        // Intentionally empty: computeIfAbsent supplies the factory on demand.
    }

    /** Deserializes world character storage from NBT. */
    public static CharacterSavedData load(CompoundTag root) {
        CharacterSavedData savedData = new CharacterSavedData();
        ListTag profileTags = root.getList("Profiles", Tag.TAG_COMPOUND);
        profileTags.forEach(tag -> {
            CharacterData profile = CharacterData.deserialize((CompoundTag) tag);
            savedData.profiles.put(profile.getPlayerId(), profile);
        });
        return savedData;
    }

    @Override
    public CompoundTag save(CompoundTag root) {
        ListTag profileTags = new ListTag();
        profiles.values().forEach(profile -> profileTags.add(profile.serialize()));
        root.put("Profiles", profileTags);
        return root;
    }

    public CharacterData getOrCreate(UUID playerId) {
        return profiles.computeIfAbsent(playerId, CharacterData::new);
    }

    public void put(CharacterData data) {
        profiles.put(data.getPlayerId(), data);
        setDirty();
    }

    public Collection<CharacterData> findAll() {
        return profiles.values();
    }
}
