package com.example.cinematicscenes;

import com.example.cinematicscenes.command.CutsceneCommand;
import com.example.cinematicscenes.network.SceneNetwork;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(CinematicScenes.MOD_ID)
public final class CinematicScenes {
    public static final String MOD_ID = "cinematicscenes";
    public CinematicScenes() {
        SceneNetwork.register();
        MinecraftForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> CutsceneCommand.register(event.getDispatcher()));
    }
}
