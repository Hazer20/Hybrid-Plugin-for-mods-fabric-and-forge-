package com.desquad.api;

import com.desquad.api.blocks.BlockRegistry;
import com.desquad.api.dialogue.DialogueService;
import com.desquad.api.economy.EconomyService;
import com.desquad.api.gui.GuiService;
import com.desquad.api.items.ItemRegistry;
import com.desquad.api.mobs.MobRegistry;
import com.desquad.api.models.ModelService;
import com.desquad.api.modules.ModuleManager;
import com.desquad.api.npc.NPCManager;
import com.desquad.api.performance.PerformanceService;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Глобальная точка входа в API DESquadCore.
 */
public final class DESquadAPI {
    private static final Logger LOGGER = Logger.getLogger("DESquadAPI");
    private static ServiceContainer container;

    private DESquadAPI() {}

    public static void bootstrap(ServiceContainer serviceContainer) {
        container = Objects.requireNonNull(serviceContainer, "serviceContainer");
        LOGGER.info("DESquadAPI bootstrapped.");
    }

    public static ItemRegistry getItemRegistry() { return require().itemRegistry(); }
    public static NPCManager getNPCManager() { return require().npcManager(); }
    public static EconomyService getEconomy() { return require().economyService(); }
    public static GuiService getGuiService() { return require().guiService(); }
    public static DialogueService getDialogueService() { return require().dialogueService(); }
    public static ModelService getModelService() { return require().modelService(); }
    public static BlockRegistry getBlockRegistry() { return require().blockRegistry(); }
    public static MobRegistry getMobRegistry() { return require().mobRegistry(); }
    public static ModuleManager getModuleManager() { return require().moduleManager(); }
    public static PerformanceService getPerformanceService() { return require().performanceService(); }

    private static ServiceContainer require() {
        if (container == null) {
            throw new IllegalStateException("DESquadAPI is not initialized");
        }
        return container;
    }
}
