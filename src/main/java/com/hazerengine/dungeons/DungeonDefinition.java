package com.hazerengine.dungeons;

import com.hazerengine.core.api.Identifiable;

import java.util.List;

public record DungeonDefinition(String id, int rooms, String boss, List<String> rewards) implements Identifiable { }
