package com.hazerengine.components;

public record FoodComponent(int hunger,float saturation) implements ItemComponent { public String key(){return "food";} }
