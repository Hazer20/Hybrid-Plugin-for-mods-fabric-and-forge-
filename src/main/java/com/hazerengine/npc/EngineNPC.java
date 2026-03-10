package com.hazerengine.npc;

import com.hazerengine.core.api.Identifiable;

public record EngineNPC(String id, String displayName, String dialogue) implements Identifiable { }
