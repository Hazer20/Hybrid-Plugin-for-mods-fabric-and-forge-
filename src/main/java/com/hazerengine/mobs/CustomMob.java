package com.hazerengine.mobs;

import com.hazerengine.core.api.Identifiable;

import java.util.List;

public record CustomMob(String id, String entityType, List<String> abilities, List<String> drops) implements Identifiable { }
