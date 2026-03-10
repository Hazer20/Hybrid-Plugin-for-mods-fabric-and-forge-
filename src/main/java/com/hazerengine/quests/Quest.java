package com.hazerengine.quests;

import com.hazerengine.core.api.Identifiable;

public record Quest(String id, String type, String description, int goal) implements Identifiable { }
