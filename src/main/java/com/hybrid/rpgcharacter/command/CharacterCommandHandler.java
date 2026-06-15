package com.hybrid.rpgcharacter.command;

import com.hybrid.rpgcharacter.data.CharacterData;
import com.hybrid.rpgcharacter.network.CharacterNetwork;
import com.hybrid.rpgcharacter.network.OpenCharacterScreenPacket;
import com.hybrid.rpgcharacter.network.SyncCharacterDataPacket;
import com.hybrid.rpgcharacter.network.UpdateAgePacket;
import com.hybrid.rpgcharacter.persistence.CharacterRepository;
import com.hybrid.rpgcharacter.service.AgeService;
import com.hybrid.rpgcharacter.service.HeightService;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;

/** Registers admin-only test commands for character development and QA. */
public class CharacterCommandHandler {
    private final CharacterRepository repository = new CharacterRepository();
    private final HeightService heightService = new HeightService();
    private final AgeService ageService = new AgeService(heightService);

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    /** Adds the root command and keeps every test action behind permission level 2. */
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("character_test")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("open")
                        .executes(context -> openSelf(context.getSource()))
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(context -> openTarget(context.getSource(), EntityArgument.getPlayer(context, "target")))))
                .then(Commands.literal("sync")
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(context -> syncTarget(context.getSource(), EntityArgument.getPlayer(context, "target")))))
                .then(Commands.literal("add_age")
                        .then(Commands.argument("target", EntityArgument.player())
                                .then(Commands.argument("years", IntegerArgumentType.integer(1, 200))
                                        .executes(context -> addAge(context.getSource(), EntityArgument.getPlayer(context, "target"), IntegerArgumentType.getInteger(context, "years"))))))
                .then(Commands.literal("info")
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(context -> printInfo(context.getSource(), EntityArgument.getPlayer(context, "target"))))));
    }

    private int openSelf(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        return openTarget(source, source.getPlayerOrException());
    }

    private int openTarget(CommandSourceStack source, ServerPlayer target) {
        CharacterData data = repository.getOrCreate(target.server, target.getUUID());
        sync(target, data);
        CharacterNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> target), new OpenCharacterScreenPacket());
        source.sendSuccess(() -> Component.translatable("commands.character_test.open", target.getGameProfile().getName()), true);
        return 1;
    }

    private int syncTarget(CommandSourceStack source, ServerPlayer target) {
        CharacterData data = repository.getOrCreate(target.server, target.getUUID());
        sync(target, data);
        source.sendSuccess(() -> Component.translatable("commands.character_test.sync", target.getGameProfile().getName()), true);
        return 1;
    }

    private int addAge(CommandSourceStack source, ServerPlayer target, int years) {
        CharacterData data = repository.getOrCreate(target.server, target.getUUID());
        for (int i = 0; i < years; i++) {
            ageService.addAge(target, data);
        }
        repository.save(target.server, data);
        sync(target, data);
        CharacterNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> target), new UpdateAgePacket(data.getAge(), data.getAgeProgress()));
        source.sendSuccess(() -> Component.translatable("commands.character_test.add_age", years, target.getGameProfile().getName(), data.getAge()), true);
        return years;
    }

    private int printInfo(CommandSourceStack source, ServerPlayer target) {
        CharacterData data = repository.getOrCreate(target.server, target.getUUID());
        source.sendSuccess(() -> Component.literal("Character " + target.getGameProfile().getName()
                + ": age=" + data.getAge()
                + ", height=" + data.getHeightCm() + "cm"
                + ", gender=" + data.getGender()
                + ", traits=" + data.getTraits()), false);
        return 1;
    }

    private void sync(ServerPlayer target, CharacterData data) {
        CharacterNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> target), new SyncCharacterDataPacket(data));
    }
}
