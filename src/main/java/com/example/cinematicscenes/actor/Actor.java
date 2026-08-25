package com.example.cinematicscenes.actor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
/** Stable target abstraction; demo targets a virtual point, later scenes may bind a real entity. */
public final class Actor { private final String id; private Entity entity; private Vec3 position; public Actor(String id,Vec3 position){this.id=id;this.position=position;} public String id(){return id;} public Vec3 position(){return entity==null?position:entity.position();} public void bind(Entity entity){this.entity=entity;} public void lookAt(Vec3 target){if(entity!=null)entity.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,target);} }
