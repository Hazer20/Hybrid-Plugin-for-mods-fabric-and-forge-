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

/** Набор сервисов ядра. */
public record ServiceContainer(
        ItemRegistry itemRegistry,
        NPCManager npcManager,
        EconomyService economyService,
        GuiService guiService,
        DialogueService dialogueService,
        ModelService modelService,
        BlockRegistry blockRegistry,
        MobRegistry mobRegistry,
        ModuleManager moduleManager,
        PerformanceService performanceService
) {}
