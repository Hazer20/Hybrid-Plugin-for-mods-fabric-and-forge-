package com.hybrid.rpgcharacter.network;

import com.hybrid.rpgcharacter.client.CharacterCreationScreen;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

public record OpenCharacterCreationPacket() {
    public static void encode(OpenCharacterCreationPacket packet, FriendlyByteBuf buffer) { }
    public static OpenCharacterCreationPacket decode(FriendlyByteBuf buffer) { return new OpenCharacterCreationPacket(); }
    public static void handle(OpenCharacterCreationPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(OpenCharacterCreationPacket::openClientScreen);
        context.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void openClientScreen() {
        Minecraft.getInstance().setScreen(new CharacterCreationScreen());
    }
}
