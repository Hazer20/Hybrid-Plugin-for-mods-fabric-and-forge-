package com.hybrid.rpgcharacter.event;

import com.hybrid.rpgcharacter.data.CharacterData;
import com.hybrid.rpgcharacter.data.CharacterTrait;
import com.hybrid.rpgcharacter.network.CharacterNetwork;
import com.hybrid.rpgcharacter.network.SyncCharacterDataPacket;
import com.hybrid.rpgcharacter.network.UpdateAgePacket;
import com.hybrid.rpgcharacter.persistence.CharacterRepository;
import com.hybrid.rpgcharacter.service.AgeService;
import com.hybrid.rpgcharacter.service.CharacterGenerationService;
import com.hybrid.rpgcharacter.service.HeightService;
import com.hybrid.rpgcharacter.service.TraitLimitService;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;

/** Wires Forge player lifecycle events to character services. */
public class CharacterEventHandler {
    private final HeightService heightService = new HeightService();
    private final TraitLimitService traitLimitService = new TraitLimitService();
    private final CharacterGenerationService generationService = new CharacterGenerationService(heightService, traitLimitService);
    private final AgeService ageService = new AgeService(heightService);
    private final CharacterRepository repository = new CharacterRepository();

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CharacterData data = repository.getOrCreate(player.server, player.getUUID());
            generationService.initializeIfNeeded(player, data);
            repository.save(player.server, data);
            recountTraits(player.server);
            sync(player, data);
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            recountTraits(player.server);
        }
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CharacterData data = repository.getOrCreate(player.server, player.getUUID());
            heightService.updatePlayerDimensions(player, data);
            sync(player, data);
        }
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CharacterData data = repository.getOrCreate(player.server, player.getUUID());
            repository.save(player.server, data);
            sync(player, data);
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer player) {
            CharacterData data = repository.getOrCreate(player.server, player.getUUID());
            if (data.isInitialized() && ageService.tickPlayerAge(player, data)) {
                repository.save(player.server, data);
                CharacterNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new UpdateAgePacket(data.getAge(), data.getAgeProgress()));
            }
        }
    }

    private void sync(ServerPlayer player, CharacterData data) {
        CharacterNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SyncCharacterDataPacket(data));
    }

    private void recountTraits(MinecraftServer server) {
        Map<CharacterTrait, Integer> counts = new EnumMap<>(CharacterTrait.class);
        repository.findAll(server).forEach(profile -> profile.getTraits().forEach(trait -> {
            if (trait != CharacterTrait.NONE) counts.merge(trait, 1, Integer::sum);
        }));
        traitLimitService.replaceCounts(counts);
    }
}
