package com.hazerengine.api.impl;

import com.hazerengine.api.NPCAPI;
import com.hazerengine.core.registry.NPCRegistry;
import com.hazerengine.npc.EngineNPC;

import java.util.Collection;
import java.util.Optional;

public class NPCAPIImpl implements NPCAPI {
    private final NPCRegistry registry;

    public NPCAPIImpl(NPCRegistry registry) { this.registry = registry; }

    @Override
    public void register(EngineNPC npc) { registry.register(npc); }

    @Override
    public Optional<EngineNPC> get(String id) { return registry.get(id); }

    @Override
    public Collection<EngineNPC> all() { return registry.all(); }
}
