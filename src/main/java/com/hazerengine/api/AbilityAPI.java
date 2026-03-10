package com.hazerengine.api;

import com.hazerengine.abilities.Ability;

import java.util.Collection;
import java.util.Optional;

/**
 * Public ability API.
 */
public interface AbilityAPI {
    void register(Ability ability);
    Optional<Ability> get(String id);
    Collection<Ability> all();
}
