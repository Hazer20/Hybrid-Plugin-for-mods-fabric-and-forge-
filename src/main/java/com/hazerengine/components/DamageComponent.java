package com.hazerengine.components;

public record DamageComponent(double damage) implements ItemComponent { public String key(){return "damage";} }
