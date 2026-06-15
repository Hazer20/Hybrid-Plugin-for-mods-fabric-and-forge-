package com.hybrid.rpgcharacter.network;

import com.hybrid.rpgcharacter.data.BodyType;
import com.hybrid.rpgcharacter.data.CharacterData;
import com.hybrid.rpgcharacter.data.CharacterTrait;
import com.hybrid.rpgcharacter.data.Gender;
import com.hybrid.rpgcharacter.persistence.CharacterRepository;
import com.hybrid.rpgcharacter.service.CharacterGenerationService;
import com.hybrid.rpgcharacter.service.HeightService;
import com.hybrid.rpgcharacter.service.TraitLimitService;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

public record SubmitCharacterCreationPacket(String characterName, Gender gender, BodyType bodyType) {
    public static void encode(SubmitCharacterCreationPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.characterName, 32);
        buffer.writeEnum(packet.gender);
        buffer.writeEnum(packet.bodyType);
    }

    public static SubmitCharacterCreationPacket decode(FriendlyByteBuf buffer) {
        return new SubmitCharacterCreationPacket(buffer.readUtf(32), buffer.readEnum(Gender.class), buffer.readEnum(BodyType.class));
    }

    public static void handle(SubmitCharacterCreationPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player == null) return;
            CharacterRepository repository = new CharacterRepository();
            HeightService heightService = new HeightService();
            TraitLimitService traitLimitService = new TraitLimitService();
            recountTraits(repository, player, traitLimitService);
            CharacterGenerationService generationService = new CharacterGenerationService(heightService, traitLimitService);
            CharacterData data = repository.getOrCreate(player.server, player.getUUID());
            generationService.initializeWithChoices(player, data, packet.characterName, packet.gender, packet.bodyType);
            repository.save(player.server, data);
            CharacterNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SyncCharacterDataPacket(data));
            CharacterNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new OpenCharacterScreenPacket());
        });
        context.get().setPacketHandled(true);
    }

    private static void recountTraits(CharacterRepository repository, ServerPlayer player, TraitLimitService traitLimitService) {
        Map<CharacterTrait, Integer> counts = new EnumMap<>(CharacterTrait.class);
        repository.findAll(player.server).forEach(profile -> profile.getTraits().forEach(trait -> {
            if (trait != CharacterTrait.NONE) counts.merge(trait, 1, Integer::sum);
        }));
        traitLimitService.replaceCounts(counts);
    }
}
