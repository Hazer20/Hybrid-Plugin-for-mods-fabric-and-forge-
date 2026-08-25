package com.example.cinematicscenes.gui;
import com.example.cinematicscenes.scene.SceneManager;
import com.example.cinematicscenes.scene.ScenePlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

/** HUD overlay with fade, letterbox, typewriter dialogue and keyboard/mouse-equivalent choices. */
@Mod.EventBusSubscriber(modid = "cinematicscenes", value = Dist.CLIENT)
public final class CinematicOverlay {
 @SubscribeEvent public static void render(RenderGuiOverlayEvent.Post e){SceneManager m=SceneManager.client();if(!m.active())return; ScenePlayer p=m.player();GuiGraphics g=e.getGuiGraphics();int w=e.getWindow().getGuiScaledWidth(),h=e.getWindow().getGuiScaledHeight();int bars=Math.min(42,p.tick()*42/30);g.fill(0,0,w,bars,0xFF000000);g.fill(0,h-bars,w,h,0xFF000000);int fade=p.stage()==ScenePlayer.Stage.INTRO?Math.max(0,255-p.tick()*255/35):(p.stage()==ScenePlayer.Stage.OUTRO?Math.min(255,p.tick()*255/40):0);if(fade>0)g.fill(0,0,w,h,fade<<24);if(p.stage()==ScenePlayer.Stage.DIALOGUE||p.stage()==ScenePlayer.Stage.BRANCH){int y=h-bars-78;g.fill(w/2-180,y,w/2+180,y+62,0xD9151820);g.renderOutline(w/2-180,y,360,62,0xFF777E91);g.drawString(Minecraft.getInstance().font,p.speaker(),w/2-168,y+9,0xFFB8C7E8);String shown=p.text().substring(0,Math.min(p.text().length(),Math.max(1,p.tick()*35/20)));g.drawString(Minecraft.getInstance().font,shown,w/2-168,y+34,0xFFFFFFFF);}if(p.stage()==ScenePlayer.Stage.CHOICE){String[] c={"1. Я пришёл поговорить.","2. Мне здесь нечего делать.","3. Это тебя не касается."};g.drawCenteredString(Minecraft.getInstance().font,"ЧТО ТЫ ОТВЕТИШЬ?",w/2,h/2-65,0xFFFFFFFF);for(int i=0;i<3;i++){int y=h/2-35+i*28;g.fill(w/2-150,y,w/2+150,y+22,0xDD151820);g.renderOutline(w/2-150,y,300,22,0xFF677188);g.drawString(Minecraft.getInstance().font,c[i],w/2-138,y+7,0xFFFFFFFF);}} }
 @SubscribeEvent public static void key(InputEvent.Key e){if(e.getAction()!=GLFW.GLFW_PRESS)return;SceneManager m=SceneManager.client();if(!m.active())return;ScenePlayer p=m.player();if(e.getKey()==GLFW.GLFW_KEY_ESCAPE){m.stop();return;}if(p.stage()==ScenePlayer.Stage.CHOICE&&e.getKey()>=GLFW.GLFW_KEY_1&&e.getKey()<=GLFW.GLFW_KEY_3)m.choose(e.getKey()-GLFW.GLFW_KEY_1);else if(e.getKey()==GLFW.GLFW_KEY_ENTER||e.getKey()==GLFW.GLFW_KEY_SPACE)m.advance();}
}
