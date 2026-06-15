package com.hybrid.rpgcharacter.network;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public record UpdateAgePacket(int age, long ageProgress) {
    public static void encode(UpdateAgePacket packet, FriendlyByteBuf buffer) { buffer.writeInt(packet.age); buffer.writeLong(packet.ageProgress); }
    public static UpdateAgePacket decode(FriendlyByteBuf buffer) { return new UpdateAgePacket(buffer.readInt(), buffer.readLong()); }
    public static void handle(UpdateAgePacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (ClientCharacterCache.getLocalCharacter() != null) {
                ClientCharacterCache.getLocalCharacter().setAge(packet.age);
                ClientCharacterCache.getLocalCharacter().setAgeProgress(packet.ageProgress);
            }
        });
        context.get().setPacketHandled(true);
    }
}
