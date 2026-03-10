package com.desquad.api.npc;

import java.util.UUID;

public interface NPC {
    UUID id();
    String name();
    String profession();
    void setProfession(String profession);
}
