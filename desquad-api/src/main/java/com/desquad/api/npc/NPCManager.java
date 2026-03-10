package com.desquad.api.npc;

import java.util.Collection;
import java.util.Optional;

public interface NPCManager {
    NPC create(String name);
    Optional<NPC> findByName(String name);
    Collection<NPC> all();
}
