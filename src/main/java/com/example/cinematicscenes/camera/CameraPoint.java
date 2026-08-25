package com.example.cinematicscenes.camera;
import net.minecraft.world.phys.Vec3;
public record CameraPoint(Vec3 position, float yaw, float pitch, float roll, float fov, int durationTicks, int holdTicks, Easing easing) {}
