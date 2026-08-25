package com.example.cinematicscenes.network;

import com.example.cinematicscenes.CinematicScenes;
import com.example.cinematicscenes.scene.SceneManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import java.util.function.Supplier;

public final class SceneNetwork {
    private static final String VERSION = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation(CinematicScenes.MOD_ID, "main"), () -> VERSION, VERSION::equals, VERSION::equals);
    private static int id;
    public static void register() { CHANNEL.registerMessage(id++, ControlPacket.class, ControlPacket::encode, ControlPacket::decode, ControlPacket::handle); }
    public static void sendToPlayer(net.minecraft.server.level.ServerPlayer player, Action action) { CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ControlPacket(action)); }
    public enum Action { START_DEMO, STOP, SKIP }
    public record ControlPacket(Action action) {
        static void encode(ControlPacket packet, FriendlyByteBuf buf) { buf.writeEnum(packet.action); }
        static ControlPacket decode(FriendlyByteBuf buf) { return new ControlPacket(buf.readEnum(Action.class)); }
        static void handle(ControlPacket packet, Supplier<net.minecraftforge.network.NetworkEvent.Context> supplier) {
            var context = supplier.get(); context.enqueueWork(() -> { switch (packet.action) { case START_DEMO -> SceneManager.client().startDemo(); case STOP -> SceneManager.client().stop(); case SKIP -> SceneManager.client().skip(); } }); context.setPacketHandled(true);
        }
    }
}
