package com.hybrid.rpgcharacter;

import com.hybrid.rpgcharacter.config.CharacterConfig;
import com.hybrid.rpgcharacter.event.CharacterEventHandler;
import com.hybrid.rpgcharacter.network.CharacterNetwork;
import com.hybrid.rpgcharacter.persistence.CharacterSavedData;
import com.hybrid.rpgcharacter.registry.CharacterCapabilities;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/** Main entry point for the medieval RPG character foundation mod. */
@Mod(RpgCharacterMod.MOD_ID)
public final class RpgCharacterMod {
    public static final String MOD_ID = "epicfight";

    public RpgCharacterMod() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, CharacterConfig.SERVER_SPEC);
        CharacterCapabilities.register(FMLJavaModLoadingContext.get().getModEventBus());
        CharacterNetwork.register();
        CharacterSavedData.registerFactory();
        MinecraftForge.EVENT_BUS.register(new CharacterEventHandler());
    }
}
