package com.hybrid.rpgcharacter.client;

import com.hybrid.rpgcharacter.network.ClientCharacterCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Client-only hooks for opening the character screen from vanilla UI. */
public class CharacterClientEvents {
    @SubscribeEvent
    public void onScreenInit(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof PauseScreen screen) {
            int x = screen.width / 2 - 102;
            int y = screen.height / 4 + 144;
            event.addListener(Button.builder(Component.translatable("button.epicfight.character"), button ->
                            Minecraft.getInstance().setScreen(new CharacterScreen(ClientCharacterCache.getLocalCharacter())))
                    .pos(x, y)
                    .size(204, 20)
                    .build());
        }
    }
}
