package com.example.cinematicscenes.scene;
import com.example.cinematicscenes.camera.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

/** A compact branching demo timeline; pauses naturally while dialogue or choices are visible. */
public final class ScenePlayer {
    public enum Stage { INTRO, DIALOGUE, CHOICE, BRANCH, OUTRO, DONE }
    private final Minecraft mc; private final CameraState saved; private final CameraController camera; private final Vec3 actor; private Stage stage=Stage.INTRO; private int tick, branch; private String speaker="UNKNOWN", text="";
    public ScenePlayer(Minecraft mc) { this.mc=mc; LocalPlayer p=mc.player; saved=CameraState.capture(p); p.noPhysics=true; actor=p.position().add(p.getLookAngle().normalize().scale(7)); camera=new CameraController(p); camera.moveTo(actor.add(-8,4,-8), 35,12,70,40,Easing.EASE_IN_OUT_CUBIC); }
    public Stage stage(){return stage;} public int tick(){return tick;} public String speaker(){return speaker;} public String text(){return text;} public int branch(){return branch;} public CameraController camera(){return camera;}
    public void tick() { if(mc.player==null) return; tick++; camera.tick(mc.player); if(stage==Stage.INTRO) { if(tick==40) camera.lookAt(actor.add(0,1.5,0),30); if(tick==70) camera.moveTo(actor.add(-3,2,-4),35,8,55,55,Easing.EASE_IN_OUT_CUBIC); if(tick>=130){stage=Stage.DIALOGUE;speaker="НЕЗНАКОМЕЦ";text="Ты действительно решил сюда прийти?";} } else if(stage==Stage.BRANCH && tick>90) { stage=Stage.OUTRO; tick=0; } else if(stage==Stage.OUTRO && tick>45) stage=Stage.DONE; }
    public void advanceDialogue() { if(stage==Stage.DIALOGUE){stage=Stage.CHOICE; tick=0;} }
    public void choose(int value) { if(stage!=Stage.CHOICE)return; branch=value;stage=Stage.BRANCH;tick=0; if(value==0){speaker="НЕЗНАКОМЕЦ";text="Тогда тебе лучше узнать правду.";camera.moveTo(actor.add(-2,2,-2),45,5,48,35,Easing.EASE_IN_OUT_CUBIC);} else if(value==1){speaker="НЕЗНАКОМЕЦ";text="Ты ещё изменишь своё мнение.";camera.moveTo(actor.add(-7,4,-7),40,10,68,35,Easing.EASE_OUT);} else {speaker="НЕЗНАКОМЕЦ";text="Ошибаешься.";camera.moveTo(actor.add(-1,1.8,-1),45,4,42,18,Easing.EASE_IN);camera.shake(.18f,20,3);} }
    public void skip() { if(stage==Stage.DIALOGUE) advanceDialogue(); else if(stage==Stage.CHOICE) choose(0); else {stage=Stage.OUTRO;tick=0;} }
    public void restore(){ if(mc.player!=null)saved.restore(mc.player); }
}
