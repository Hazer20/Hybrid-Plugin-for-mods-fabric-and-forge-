package com.desquad.engine.npc;

import com.desquad.api.npc.NPC;
import java.util.UUID;

public final class SimpleNPC implements NPC {
    private final UUID id;
    private final String name;
    private String profession = "none";

    public SimpleNPC(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override public UUID id() { return id; }
    @Override public String name() { return name; }
    @Override public String profession() { return profession; }
    @Override public void setProfession(String profession) { this.profession = profession; }
}
