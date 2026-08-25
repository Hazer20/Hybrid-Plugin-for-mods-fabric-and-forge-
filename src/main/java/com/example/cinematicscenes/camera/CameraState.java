package com.example.cinematicscenes.camera;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.player.LocalPlayer;
public record CameraState(Vec3 position, float yaw, float pitch, float fov, boolean noPhysics) { public static CameraState capture(LocalPlayer p) { return new CameraState(p.position(),p.getYRot(),p.getXRot(),p.getAbilities().getWalkingSpeed(),p.noPhysics); } public void restore(LocalPlayer p) { p.setPos(position); p.setYRot(yaw); p.setXRot(pitch); p.yRotO=yaw; p.xRotO=pitch; p.noPhysics=noPhysics; } }
