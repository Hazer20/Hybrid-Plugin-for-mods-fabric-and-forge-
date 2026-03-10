package com.hazerengine.crafting;

import com.hazerengine.core.api.Identifiable;

import java.util.List;

public record CustomRecipe(String id, String type, List<String> shape, String result) implements Identifiable { }
