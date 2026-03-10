package com.hazerengine.components;

public record TextureComponent(int modelData,String texturePath) implements ItemComponent { public String key(){return "texture";} }
