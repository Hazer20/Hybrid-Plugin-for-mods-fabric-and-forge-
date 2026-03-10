package com.hazerengine.api.impl;

import com.hazerengine.abilities.Ability;
import com.hazerengine.api.AbilityAPI;
import com.hazerengine.core.registry.AbilityRegistry;

import java.util.Collection;
import java.util.Optional;

public class AbilityAPIImpl implements AbilityAPI {
    private final AbilityRegistry registry;

    public AbilityAPIImpl(AbilityRegistry registry) { this.registry = registry; }

    @Override
    public void register(Ability ability) { registry.register(ability); }

    @Override
    public Optional<Ability> get(String id) { return registry.get(id); }

    @Override
    public Collection<Ability> all() { return registry.all(); }
}
