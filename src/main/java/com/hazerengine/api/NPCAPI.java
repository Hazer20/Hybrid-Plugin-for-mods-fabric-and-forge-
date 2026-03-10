package com.hazerengine.api;

import com.hazerengine.npc.EngineNPC;

import java.util.Collection;
import java.util.Optional;

/**
 * Public NPC API.
 */
public interface NPCAPI {
    void register(EngineNPC npc);
    Optional<EngineNPC> get(String id);
    Collection<EngineNPC> all();
}
