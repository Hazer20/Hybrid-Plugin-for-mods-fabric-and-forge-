package com.example.cinematicscenes.camera;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

public final class CameraController {
    private final CinematicCamera camera; private Vec3 from, to; private float fromYaw, fromPitch, fromFov, toYaw, toPitch, toFov; private int elapsed, duration; private Easing easing=Easing.LINEAR; private int shakeTicks; private float shakeIntensity; private int shakeFrequency=2;
    public CameraController(LocalPlayer player) { camera=new CinematicCamera(player.position(),player.getYRot(),player.getXRot()); }
    public CinematicCamera camera() { return camera; }
    public void moveTo(Vec3 position, float yaw, float pitch, float fov, int ticks, Easing easing) { from=camera.position(); to=position; fromYaw=camera.yaw(); fromPitch=camera.pitch(); fromFov=camera.fov(); toYaw=yaw; toPitch=pitch; toFov=fov; duration=Math.max(1,ticks); elapsed=0; this.easing=easing; }
    public void lookAt(Vec3 target, int ticks) { Vec3 d=target.subtract(camera.position()); double h=Math.sqrt(d.x*d.x+d.z*d.z); moveTo(camera.position(),(float)(Math.toDegrees(Math.atan2(d.z,d.x))-90),(float)-Math.toDegrees(Math.atan2(d.y,h)),camera.fov(),ticks,Easing.EASE_IN_OUT_CUBIC); }
    public void orbit(Vec3 target, double radius, double height, double startDegrees, double endDegrees, int ticks) { double a=Math.toRadians(endDegrees); Vec3 p=target.add(Math.cos(a)*radius,height,Math.sin(a)*radius); moveTo(p, camera.yaw(), camera.pitch(), camera.fov(), ticks, Easing.EASE_IN_OUT_CUBIC); }
    public void shake(float intensity, int ticks, int frequency) { shakeIntensity=intensity; shakeTicks=ticks; shakeFrequency=Math.max(1,frequency); }
    public void tick(LocalPlayer player) { if (elapsed<duration) { float t=easing.apply(++elapsed/(float)duration); camera.set(from.lerp(to,t), lerpAngle(fromYaw,toYaw,t), fromPitch+(toPitch-fromPitch)*t,fromFov+(toFov-fromFov)*t); } float sx=0, sy=0; if(shakeTicks-- > 0) { float decay=shakeTicks/(float)Math.max(1,duration); sx=(float)Math.sin(shakeTicks*shakeFrequency)*shakeIntensity*decay; sy=(float)Math.cos(shakeTicks*shakeFrequency*.73)*shakeIntensity*decay; } camera.apply(player,sx,sy); }
    private static float lerpAngle(float a,float b,float t) { float d=(b-a)%360; if(d>180)d-=360;if(d<-180)d+=360;return a+d*t; }
}
