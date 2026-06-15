package com.hybrid.rpgcharacter.network;

import com.hybrid.rpgcharacter.data.CharacterData;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public record SyncCharacterDataPacket(CharacterData data) {
    public static void encode(SyncCharacterDataPacket packet, FriendlyByteBuf buffer) { buffer.writeNbt(packet.data.serialize()); }
    public static SyncCharacterDataPacket decode(FriendlyByteBuf buffer) { CompoundTag tag = buffer.readNbt(); return new SyncCharacterDataPacket(CharacterData.deserialize(tag == null ? new CompoundTag() : tag)); }
    public static void handle(SyncCharacterDataPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> ClientCharacterCache.setLocalCharacter(packet.data));
        context.get().setPacketHandled(true);
    }
}
