package ru.liko.wrbbasemod.client.gui.overlay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import ru.liko.wrbbasemod.Wrbbasemod;
import ru.liko.wrbbasemod.common.item.ModItems;

@Mod.EventBusSubscriber(modid = Wrbbasemod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class HudOverlay {

// Современная милитари-палитра (tactical/HUD style)
private static final int TICK_MAIN  = 0xFF6B705C;   // крупные деления (olive drab темный)
private static final int TICK_SUB   = 0xFF8B9080;   // мелкие деления (приглушённый olive)
private static final int DIR_MAIN   = 0xFFCED4B3;   // N E S W (tactical tan/desert sand)
private static final int DIR_SUB    = 0xFF9FA590;   // NE SE SW NW (sage green)
private static final int NORTH_CLR  = 0xFFFF4444;   // выделенный «N» (тактический красный NATO)
private static final int POINTER_CLR= 0xFF00FF00;   // линия + стрелка (ярко-зелёный HUD)
private static final int DEG_CLR    = 0xFFB8C5B0;   // цифры (приглушённый светлый)


    // Геометрия
    private static final int WIDTH  = 300;
    private static final int HEIGHT = 25;
    private static final int TOP    = 10;
    private static final float SCALE = 2.0f;

    private static float smYaw;
    private static long  last;

    @SubscribeEvent
    public static void onRender(RenderGuiOverlayEvent.Post e) {
        if (!e.getOverlay().id().equals(VanillaGuiOverlay.HOTBAR.id())) return;
        LocalPlayer pl = Minecraft.getInstance().player;
        if (pl == null || Minecraft.getInstance().options.hideGui) return;
        
        // Находим КПК в инвентаре и проверяем его NBT тег Enabled
        ItemStack pdaStack = findMilitaryPDA(pl);
        if (pdaStack.isEmpty()) return;
        
        // Проверяем NBT тег Enabled напрямую
        boolean pdaEnabled = pdaStack.hasTag() && pdaStack.getTag().getBoolean("Enabled");
        if (!pdaEnabled) return;

        GuiGraphics g = e.getGuiGraphics();
        int sw = e.getWindow().getGuiScaledWidth();
        renderCompass(g, pl, sw);
    }

    private static void renderCompass(GuiGraphics g, LocalPlayer p, int sw) {
        Font f   = Minecraft.getInstance().font;
        int cx   = sw / 2;
        int left = cx - WIDTH / 2;
        int right= cx + WIDTH / 2;
        int top  = TOP;
        int bot  = top + HEIGHT;

        float yaw = (p.getYRot() % 360 + 360) % 360;
        smooth(yaw);

        /* ---- деления каждые 15° ---- */
        for (int a = 0; a < 360; a += 15) {
            float d = diff(smYaw, a);
            if (Math.abs(d) > 80) continue;
            int x = cx + Math.round(d * SCALE);
            if (x < left+4 || x > right-4) continue;

            int h = (a % 90 == 0) ? 10 : (a % 45 == 0 ? 7 : 4);
            int col = (a % 90 == 0) ? TICK_MAIN : TICK_SUB;
            float fade = 1f - Math.abs(d)/80f*0.6f;
            g.fill(x, top+3, x+1, top+3+h, alpha(col,fade));
        }

        /* ---- направления ---- */
        String[] dir = {"N","NE","E","SE","S","SW","W","NW"};
        int[]    ang = { 0 , 45 , 90 ,135 ,180 ,225 ,270 ,315};
        for (int i=0;i<dir.length;i++){
            float d = diff(smYaw, ang[i]);
            if (Math.abs(d) > 70) continue;
            int x = cx + Math.round(d * SCALE);
            if (x < left+10 || x > right-10) continue;

            int clr = (dir[i].length()==1) ? DIR_MAIN : DIR_SUB;
            if (dir[i].equals("N")) clr = NORTH_CLR;
            float fade = 1f - Math.abs(d)/70f*0.5f;
            g.drawString(f, dir[i], x - f.width(dir[i])/2, top+8, alpha(clr,fade));
        }

        /* ---- градусы каждые 30° ---- */
        for (int a=0; a<360; a+=30){
            if (a%45==0) continue;
            float d = diff(smYaw,a);
            if (Math.abs(d)>60) continue;
            int x = cx + Math.round(d*SCALE);
            String t = String.valueOf(a);
            float fade = 1f - Math.abs(d)/60f*0.5f;
            g.drawString(f,t,x-f.width(t)/2,top+15,alpha(DEG_CLR,fade));
        }

        /* ---- центральный указатель ---- */
        g.fill(cx, top+1, cx+1, bot-1, POINTER_CLR);
        triUp(g,cx,top,6,POINTER_CLR);

        /* ---- цифровой азимут ---- */
        int b = Math.round((360-smYaw)%360);
    String bs = String.format("%03d", b);
        int tw = f.width(bs);
        g.drawString(f, bs, cx - tw/2, bot + 3, DIR_MAIN);
    }

    /* рисуем равнобедренный треугольник вверх */
    private static void triUp(GuiGraphics g,int cx,int y,int h,int c){
        for(int i=0;i<h;i++){
            g.fill(cx-i, y-i, cx+i+1, y-i+1, c);
        }
    }

    private static void smooth(float t){
        long n = System.currentTimeMillis();
        if(last==0){last=n; smYaw=t; return;}
        float dt = Math.min((n-last)/1000f,0.06f); last=n;
        float d = diff(smYaw,t);
        smYaw = (smYaw + d*dt*7f) % 360;
        if (smYaw<0) smYaw+=360;
    }
    private static float diff(float a,float b){
        float d=b-a; if(d>180)d-=360; if(d<-180)d+=360; return d;
    }
    private static int alpha(int clr,float a){
        a=Mth.clamp(a,0f,1f);
        int A=(int)(((clr>>>24)&0xFF)*a);
        return (clr&0x00FFFFFF)|(A<<24);
    }
    
    /**
     * Находит Military PDA в инвентаре игрока и возвращает его
     * Если КПК не найден, возвращает ItemStack.EMPTY
     */
    private static ItemStack findMilitaryPDA(LocalPlayer player) {
        // Проверяем основную руку
        if (player.getMainHandItem().is(ModItems.MILITARY_PDA.get())) {
            return player.getMainHandItem();
        }
        
        // Проверяем вторую руку
        if (player.getOffhandItem().is(ModItems.MILITARY_PDA.get())) {
            return player.getOffhandItem();
        }
        
        // Проверяем инвентарь (включая хотбар и основной инвентарь)
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(ModItems.MILITARY_PDA.get())) {
                return stack;
            }
        }
        
        return ItemStack.EMPTY;
    }
}
