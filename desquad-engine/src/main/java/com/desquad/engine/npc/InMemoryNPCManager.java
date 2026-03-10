package com.desquad.engine.npc;

import com.desquad.api.npc.NPC;
import com.desquad.api.npc.NPCManager;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryNPCManager implements NPCManager {
    private final Map<UUID, NPC> npcMap = new ConcurrentHashMap<>();

    @Override
    public NPC create(String name) {
        NPC npc = new SimpleNPC(UUID.randomUUID(), name);
        npcMap.put(npc.id(), npc);
        return npc;
    }

    @Override
    public Optional<NPC> findByName(String name) {
        return npcMap.values().stream().filter(n -> n.name().equalsIgnoreCase(name)).findFirst();
    }

    @Override
    public Collection<NPC> all() {
        return npcMap.values();
    }
}
