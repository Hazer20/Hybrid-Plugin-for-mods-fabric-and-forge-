package com.hybrid.rpgcharacter.persistence;

import com.hybrid.rpgcharacter.data.CharacterData;
import java.util.Collection;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;

/** Facade for loading and saving character profiles in world saved data. */
public class CharacterRepository {
    /** Gets an existing profile or creates an uninitialized shell for first-login generation. */
    public CharacterData getOrCreate(MinecraftServer server, UUID playerId) {
        return storage(server).getOrCreate(playerId);
    }

    /** Persists a character profile and marks the backing saved data dirty. */
    public void save(MinecraftServer server, CharacterData data) {
        storage(server).put(data);
    }

    /** Returns every known profile for server-wide trait recounting. */
    public Collection<CharacterData> findAll(MinecraftServer server) {
        return storage(server).findAll();
    }

    private CharacterSavedData storage(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(CharacterSavedData::load, CharacterSavedData::new, CharacterSavedData.DATA_NAME);
    }
}
