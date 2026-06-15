package com.hybrid.rpgcharacter.network;

import com.hybrid.rpgcharacter.data.CharacterData;

/** Client-only cache kept deliberately small for GUI display. */
public final class ClientCharacterCache {
    private static CharacterData localCharacter;

    private ClientCharacterCache() {
    }

    public static CharacterData getLocalCharacter() { return localCharacter; }
    public static void setLocalCharacter(CharacterData data) { localCharacter = data; }
}
