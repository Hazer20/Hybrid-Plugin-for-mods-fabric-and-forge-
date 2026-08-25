package com.example.cinematicscenes.camera;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

/** Tick-driven virtual camera. Its state is applied only while a scene owns the local player. */
public final class CinematicCamera {
    public enum Mode { FREE, FIXED, FOLLOW, LOOK_AT, ORBIT, PATH }
    private Vec3 position, target; private float yaw, pitch, roll, fov = 70, zoom = 1; private Mode mode = Mode.FREE;
    public CinematicCamera(Vec3 start, float yaw, float pitch) { this.position=start; this.yaw=yaw; this.pitch=pitch; }
    public Vec3 position() { return position; } public float yaw() { return yaw; } public float pitch() { return pitch; } public float fov() { return fov; } public Mode mode() { return mode; }
    public void set(Vec3 position, float yaw, float pitch, float fov) { this.position=position; this.yaw=yaw; this.pitch=pitch; this.fov=fov; }
    public void lookAt(Vec3 point) { target=point; mode=Mode.LOOK_AT; rotateTo(point); }
    public void follow(Vec3 point, Vec3 offset) { mode=Mode.FOLLOW; position=point.add(offset); lookAt(point); }
    public void orbit(Vec3 point, double radius, double height, double degrees) { mode=Mode.ORBIT; double a=Math.toRadians(degrees); position=point.add(Math.cos(a)*radius,height,Math.sin(a)*radius); lookAt(point); }
    public void apply(LocalPlayer player, float shakeX, float shakeY) { player.setPos(position.add(shakeX, 0, shakeY)); player.setYRot(yaw + shakeX * 3); player.setXRot(pitch + shakeY * 3); player.yRotO=player.getYRot(); player.xRotO=player.getXRot(); }
    private void rotateTo(Vec3 point) { Vec3 d=point.subtract(position); double horizontal=Math.sqrt(d.x*d.x+d.z*d.z); yaw=(float)(Math.toDegrees(Math.atan2(d.z,d.x))-90); pitch=(float)-Math.toDegrees(Math.atan2(d.y,horizontal)); }
}
