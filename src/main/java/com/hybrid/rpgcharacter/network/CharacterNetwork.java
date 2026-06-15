package com.hybrid.rpgcharacter.network;

import com.hybrid.rpgcharacter.RpgCharacterMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/** Owns the SimpleChannel and packet registration order. */
public final class CharacterNetwork {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(RpgCharacterMod.MOD_ID, "main"), () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    private CharacterNetwork() {
    }

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, SyncCharacterDataPacket.class, SyncCharacterDataPacket::encode, SyncCharacterDataPacket::decode, SyncCharacterDataPacket::handle);
        CHANNEL.registerMessage(id++, OpenCharacterScreenPacket.class, OpenCharacterScreenPacket::encode, OpenCharacterScreenPacket::decode, OpenCharacterScreenPacket::handle);
        CHANNEL.registerMessage(id++, UpdateAgePacket.class, UpdateAgePacket::encode, UpdateAgePacket::decode, UpdateAgePacket::handle);
        CHANNEL.registerMessage(id++, OpenCharacterCreationPacket.class, OpenCharacterCreationPacket::encode, OpenCharacterCreationPacket::decode, OpenCharacterCreationPacket::handle);
        CHANNEL.registerMessage(id, SubmitCharacterCreationPacket.class, SubmitCharacterCreationPacket::encode, SubmitCharacterCreationPacket::decode, SubmitCharacterCreationPacket::handle);
    }
}
