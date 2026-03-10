package com.hazerengine.components;

public record DurabilityComponent(int maxDurability) implements ItemComponent { public String key(){return "durability";} }
