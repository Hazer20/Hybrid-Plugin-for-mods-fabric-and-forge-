package com.hybrid.rpgcharacter.client;

import com.hybrid.rpgcharacter.network.ClientCharacterCache;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import org.lwjgl.glfw.GLFW;

/** Client-only hooks for opening the character screen with a configurable key binding. */
public class CharacterClientEvents {
    private static final KeyMapping OPEN_CHARACTER_SCREEN = new KeyMapping(
            "key.rpgcharacter.open_character_screen",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            "key.categories.rpgcharacter");

    /** Registers each client hook on the event bus that owns its event type. */
    public static void register(IEventBus modEventBus) {
        CharacterClientEvents handler = new CharacterClientEvents();
        modEventBus.addListener(handler::onRegisterKeyMappings);
        MinecraftForge.EVENT_BUS.addListener(handler::onClientTick);
    }

    public void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_CHARACTER_SCREEN);
    }

    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        while (OPEN_CHARACTER_SCREEN.consumeClick()) {
            Minecraft.getInstance().setScreen(new CharacterScreen(ClientCharacterCache.getLocalCharacter()));
        }
    }
}
