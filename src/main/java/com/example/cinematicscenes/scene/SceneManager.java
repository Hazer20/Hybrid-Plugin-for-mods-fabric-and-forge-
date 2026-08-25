package com.example.cinematicscenes.scene;
import com.example.cinematicscenes.gui.CinematicOverlay;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class SceneManager {
 private static final SceneManager CLIENT=new SceneManager(); private ScenePlayer player;
 private SceneManager(){MinecraftForge.EVENT_BUS.register(this);}
 public static SceneManager client(){return CLIENT;}
 public boolean active(){return player!=null;} public ScenePlayer player(){return player;}
 public void startDemo(){stop(); if(Minecraft.getInstance().player!=null)player=new ScenePlayer(Minecraft.getInstance());}
 public void stop(){if(player!=null)player.restore();player=null;}
 public void skip(){if(player!=null)player.skip();}
 public void choose(int choice){if(player!=null)player.choose(choice);}
 public void advance(){if(player!=null)player.advanceDialogue();}
 @SubscribeEvent public void clientTick(TickEvent.ClientTickEvent event){if(event.phase!=TickEvent.Phase.END||player==null)return; if(Minecraft.getInstance().player==null){stop();return;}player.tick();if(player.stage()==ScenePlayer.Stage.DONE)stop();}
}
