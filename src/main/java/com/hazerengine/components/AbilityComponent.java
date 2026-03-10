package com.hazerengine.components;

import com.hazerengine.abilities.Ability;

public record AbilityComponent(Ability ability) implements ItemComponent { public String key(){return "ability";} }
