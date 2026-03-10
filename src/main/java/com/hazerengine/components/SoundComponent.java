package com.hazerengine.components;

public record SoundComponent(String soundId) implements ItemComponent { public String key(){return "sound";} }
