package com.example.cinematicscenes.command;

import com.example.cinematicscenes.network.SceneNetwork;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public final class CutsceneCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("cutscene").then(Commands.literal("demo").executes(c -> send(c.getSource(), SceneNetwork.Action.START_DEMO))).then(Commands.literal("stop").executes(c -> send(c.getSource(), SceneNetwork.Action.STOP))).then(Commands.literal("skip").executes(c -> send(c.getSource(), SceneNetwork.Action.SKIP))));
    }
    private static int send(CommandSourceStack source, SceneNetwork.Action action) {
        var player = source.getPlayer(); if (player == null) { source.sendFailure(Component.literal("This command must be used by a player.")); return 0; }
        SceneNetwork.sendToPlayer(player, action); return 1;
    }
}
