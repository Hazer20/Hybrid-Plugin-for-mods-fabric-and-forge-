package com.hazerengine.api;

import com.hazerengine.crafting.CustomRecipe;

import java.util.Collection;
import java.util.Optional;

/**
 * Public recipe API.
 */
public interface RecipeAPI {
    void register(CustomRecipe recipe);
    Optional<CustomRecipe> get(String id);
    Collection<CustomRecipe> all();
}
