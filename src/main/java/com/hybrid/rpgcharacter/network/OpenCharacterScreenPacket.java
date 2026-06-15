package com.hybrid.rpgcharacter.network;

import com.hybrid.rpgcharacter.client.CharacterScreen;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

public record OpenCharacterScreenPacket() {
    public static void encode(OpenCharacterScreenPacket packet, FriendlyByteBuf buffer) { }
    public static OpenCharacterScreenPacket decode(FriendlyByteBuf buffer) { return new OpenCharacterScreenPacket(); }
    public static void handle(OpenCharacterScreenPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(OpenCharacterScreenPacket::openClientScreen);
        context.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void openClientScreen() {
        Minecraft.getInstance().setScreen(new CharacterScreen(ClientCharacterCache.getLocalCharacter()));
    }
}
