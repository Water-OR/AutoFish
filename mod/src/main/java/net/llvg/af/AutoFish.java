package net.llvg.af;

import java.util.regex.Pattern;
import net.llvg.af.utils.Clock;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S1CPacketEntityMetadata;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.llvg.af.utils.Utility.*;

public final class AutoFish {
    public static final Logger logger = LogManager.getLogger("<|Auto Fish|>");
    
    private AutoFish() {
        throw new UnsupportedOperationException();
    }
    
    private static final IChatComponent[] prefix = new IChatComponent[] {
      new ChatComponentText("Auto Fish "),
      new ChatComponentText("|>")
        .setChatStyle(
        new ChatStyle()
          .setColor(EnumChatFormatting.AQUA)
          .setBold(true)
      ),
      new ChatComponentText(" ")
    };
    
    public static synchronized void chat(Object... msgs) {
        if (msgs == null) return;
        GuiIngame ui;
        if ((ui = mc().ingameGUI) == null) return;
        IChatComponent msg = new ChatComponentText("");
        for (IChatComponent i : prefix) if (i != null) msg.appendSibling(i);
        for (Object i : msgs) {
            if (i == null) {
                msg.appendSibling(new ChatComponentText("null"));
            } else if (i instanceof IChatComponent) {
                msg.appendSibling((IChatComponent) i);
            } else if (i instanceof String) {
                msg.appendSibling(new ChatComponentText((String) i));
            } else {
                msg.appendSibling(new ChatComponentText(i.toString()));
            }
        }
        ui.getChatGUI().printChatMessage(msg);
    }
    
    private static final Object stateLock = new Object();
    private static boolean active = false;
    private static boolean waitingForHookJoin = false;
    private static boolean waitingForHookDead = false;
    private static boolean doneRightClickThisTick = false;
    
    @Nullable
    private static ItemStack holdingItem = null;
    private static boolean holdingRod = false;
    
    public static boolean isActive() {
        return active;
    }
    
    public static synchronized void toggle() {
        boolean _active;
        synchronized (stateLock) {
            _active = (active = !active);
        }
        if (active) {
            throwFirstClock.update();
        } else {
            AutoFishAntiAfk.reset(mc().thePlayer, AutoFishConfiguration.isResetFacingWhenNotFishing());
            waitingForHookJoin = false;
            waitingForHookDead = false;
        }
        chat(
          "Auto fish is now ",
          _active
          ? new ChatComponentText("on")
            .setChatStyle(
              new ChatStyle()
                .setColor(EnumChatFormatting.GREEN)
            )
          : new ChatComponentText("off")
            .setChatStyle(
              new ChatStyle()
                .setColor(EnumChatFormatting.RED)
            )
        );
    }
    
    static synchronized void onConfigurationDisable() {
        if (active) toggle();
        waitingForHookDead = false;
        waitingForHookJoin = false;
    }
    
    @Nullable
    private static EntityFishHook hook = null;
    
    public static final Pattern fishingTimerWarnRegex = Pattern.compile("§e§l(\\d+(\\.\\d+)?)");
    public static final String fishingTimerCatchString = "§c§l!!!";
    
    public static boolean matchFishingTimer(String str) {
        return fishingTimerWarnRegex.matcher(str).matches() || str.equals(fishingTimerCatchString);
    }
    
    @Nullable
    private static EntityArmorStand timer = null;
    
    @Nullable
    public static EntityArmorStand getTimer() {
        return timer;
    }
    
    private static final Clock catchClock = new Clock();
    private static final Clock throwClock = new Clock();
    
    private static final Clock throwFirstClock = new Clock();
    
    public static void onTickStart() {
        doneRightClickThisTick = false;
    }
    
    public static void onTickEnd() {
        EntityPlayerSP player;
        if ((player = mc().thePlayer) == null) return;
        
        ItemStack currHoldingItem = player.getHeldItem();
        if (holdingItem == null || holdingItem != currHoldingItem) {
            boolean currHoldingRod = currHoldingItem != null && currHoldingItem.getItem() instanceof ItemFishingRod;
            
            if (active) {
                if (!holdingRod) throwFirstClock.update();
                if (!currHoldingRod) {
                    AutoFishAntiAfk.reset(player, holdingRod && AutoFishConfiguration.isResetFacingWhenNotFishing());
                    waitingForHookJoin = false;
                    waitingForHookDead = false;
                }
            }
            
            holdingItem = currHoldingItem;
            holdingRod = currHoldingRod;
        }
        
        if (
          active &&
          holdingRod &&
          (hook == null || AutoFishConfiguration.isDoNotWaitHookDead() && waitingForHookDead)
        ) {
            doThrow();
        }
    }
    
    public static void onWorldLoad() {
        hook = null;
        timer = null;
        holdingItem = null;
        
        if (active) {
            throwFirstClock.update();
            AutoFishAntiAfk.reset(mc().thePlayer, false);
            toggle();
        }
    }
    
    public static void onEntityJoin(Entity entity) {
        if (hook == null || entity != hook) {
            EntityPlayerSP player;
            if (
              entity instanceof EntityFishHook &&
              (player = mc().thePlayer) != null &&
              ((EntityFishHook) entity).angler == player
            ) {
                if (AutoFishConfiguration.isVerbose()) chat("Fish hook join");
                hook = ((EntityFishHook) entity);
                waitingForHookDead = false;
                waitingForHookJoin = false;
            }
        }
    }
    
    public static void onEntityDead(
      Entity entity
    ) {
        if (entity == hook) {
            if (AutoFishConfiguration.isVerbose()) chat("Fish hook dead");
            hook = null;
            waitingForHookDead = false;
        }
        
        if (entity == timer) {
            if (AutoFishConfiguration.isVerbose()) chat("Fish timer dead");
            timer = null;
        }
    }
    
    public static void onS1CPacket(
      S1CPacketEntityMetadata ignored,
      Entity entity
    ) {
        if (timer == null || entity != timer) {
            String name;
            if (
              entity instanceof EntityArmorStand &&
              entity.hasCustomName() &&
              !(name = entity.getName()).isEmpty() &&
              matchFishingTimer(name)
            ) {
                if (AutoFishConfiguration.isVerbose()) chat("Fish timer join");
                timer = (EntityArmorStand) entity;
            }
        } else {
            EntityPlayerSP player;
            if (
              active &&
              AutoFishConfiguration.isUseFishTimerCheck() && (player = mc().thePlayer) != null &&
              entity.getName().equals(fishingTimerCatchString)
            ) {
                if (AutoFishConfiguration.isVerbose()) chat("Active fish timer catch");
                doCatch(player);
            }
        }
    }
    
    public static void onSound(ISound sound) {
        String name = sound.getSoundLocation().getResourcePath();
        EntityFishHook hook1;
        if ((hook1 = hook) == null) return;
        
        if (
          active &&
          AutoFishConfiguration.isUseNoteSoundCheck() &&
          name.equals("note.pling")
        ) {
            if (
              sound.getPitch() == 1 &&
              sound.getVolume() == 1
            ) {
                if (AutoFishConfiguration.isVerbose()) chat("Active note sound catch");
                doCatch((EntityPlayerSP) hook1.angler);
            }
        }
        if (
          active &&
          AutoFishConfiguration.isUseSwimSoundCheck() &&
          name.equals("game.player.swim.splash")
        ) {
            float expand = AutoFishConfiguration.getExpandSwimSoundCheckRange();
            if (
              Math.abs(sound.getXPosF() - hook1.posX) < expand &&
              Math.abs(sound.getYPosF() - hook1.posY) < expand &&
              Math.abs(sound.getZPosF() - hook1.posZ) < expand
            ) {
                if (AutoFishConfiguration.isVerbose()) chat("Active swim sound catch");
                doCatch((EntityPlayerSP) hook1.angler);
            }
        }
    }
    
    private static void doRightClick() {
        if (doneRightClickThisTick) return;
        if (AutoFishConfiguration.isVerbose()) chat("Do right click, time=", System.currentTimeMillis());
        mc().rightClickMouse();
    }
    
    public static synchronized void markDoneRightClick() {
        doneRightClickThisTick = true;
    }
    
    private static synchronized void doCatch(@NotNull EntityPlayerSP player) {
        if (
          hook != null &&
          holdingRod &&
          !waitingForHookDead &&
          !doneRightClickThisTick &&
          AutoFishConfiguration.isAutoCatch() &&
          catchClock.ended(AutoFishConfiguration.getAutoCatchDelay())
        ) {
            if (AutoFishConfiguration.isVerbose()) chat("Do catch, time=", System.currentTimeMillis());
            doRightClick();
            waitingForHookDead = true;
            throwClock.update();
            if (AutoFishConfiguration.isAntiAfk()) AutoFishAntiAfk.trigger(player);
        }
    }
    
    private static synchronized void doThrow() {
        if (
          !waitingForHookJoin &&
          !doneRightClickThisTick &&
          AutoFishConfiguration.isAutoThrow() &&
          throwClock.ended(AutoFishConfiguration.getAutoThrowDelay()) &&
          throwFirstClock.ended(AutoFishConfiguration.getAutoThrowFirstDelay())
        ) {
            if (AutoFishConfiguration.isVerbose()) chat("Do throw, time=", System.currentTimeMillis());
            doRightClick();
            waitingForHookJoin = true;
            catchClock.update();
        }
    }
    
    public static void init() { }
    
    public static void onGameStarted() {
        AutoFishConfiguration.init();
        AutoFishAntiAfk.init();
    }
}
