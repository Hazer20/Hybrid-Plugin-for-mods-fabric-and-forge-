package com.example.cinematicscenes.scene;
import com.example.cinematicscenes.camera.CameraController;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
public final class SceneContext { public final Minecraft minecraft; public final CameraController camera; public final Vec3 actor; public SceneContext(Minecraft minecraft,CameraController camera,Vec3 actor){this.minecraft=minecraft;this.camera=camera;this.actor=actor;} }
