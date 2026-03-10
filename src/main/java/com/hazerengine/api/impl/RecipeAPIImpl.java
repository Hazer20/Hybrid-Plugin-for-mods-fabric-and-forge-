package com.hazerengine.api.impl;

import com.hazerengine.api.RecipeAPI;
import com.hazerengine.core.registry.RecipeRegistry;
import com.hazerengine.crafting.CustomRecipe;

import java.util.Collection;
import java.util.Optional;

public class RecipeAPIImpl implements RecipeAPI {
    private final RecipeRegistry registry;

    public RecipeAPIImpl(RecipeRegistry registry) { this.registry = registry; }

    @Override
    public void register(CustomRecipe recipe) { registry.register(recipe); }

    @Override
    public Optional<CustomRecipe> get(String id) { return registry.get(id); }

    @Override
    public Collection<CustomRecipe> all() { return registry.all(); }
}
