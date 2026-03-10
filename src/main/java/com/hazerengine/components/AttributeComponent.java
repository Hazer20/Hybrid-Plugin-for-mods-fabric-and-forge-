package com.hazerengine.components;

import java.util.Map;

public record AttributeComponent(Map<String, Double> attributes) implements ItemComponent { public String key(){return "attribute";} }
