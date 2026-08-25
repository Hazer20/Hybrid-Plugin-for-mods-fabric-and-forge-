package com.example.cinematicscenes.actor;
import java.util.*; public final class ActorManager { private final Map<String,Actor> actors=new HashMap<>(); public Actor create(String id,net.minecraft.world.phys.Vec3 p){Actor a=new Actor(id,p);actors.put(id,a);return a;} public Actor get(String id){return actors.get(id);} public void clear(){actors.clear();} }
