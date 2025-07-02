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
    
    public static boolean isActive() {
        return active;
    }
    
    public static synchronized void toggle() {
        boolean _active;
        synchronized (stateLock) {
            _active = (active = !active);
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
    
    public static final Pattern fishingTimerWarnRegrex = Pattern.compile("§e§l(\\d+(\\.\\d+)?)");
    public static final String fishingTimerCatchString = "§c§l!!!";
    
    public static boolean matchFishingTimer(String str) {
        return fishingTimerWarnRegrex.matcher(str).matches() || str.equals(fishingTimerCatchString);
    }
    
    @Nullable
    private static EntityArmorStand timer = null;
    
    @Nullable
    public static EntityArmorStand getTimer() {
        return timer;
    }
    
    private static final Clock catchClock = new Clock();
    private static final Clock throwClock = new Clock();
    
    public static boolean checkIsHoldingRod(@NotNull EntityPlayerSP player) {
        ItemStack held;
        if ((held = player.getHeldItem()) == null) return false;
        return held.getItem() instanceof ItemFishingRod;
    }
    
    public static void onTickStart() {
        doneRightClickThisTick = false;
    }
    
    public static void onTickEnd() {
        EntityPlayerSP player;
        if ((player = mc().thePlayer) == null) return;
        if (active && hook == null) doThrow(player);
        if (!checkIsHoldingRod(player)) {
            waitingForHookJoin = false;
            waitingForHookDead = false;
        }
    }
    
    public static void onWorldLoad() {
        hook = null;
        timer = null;
        if (active) toggle();
        waitingForHookJoin = false;
        waitingForHookDead = false;
        
        AutoFishAntiAfk.onWorldLoad();
    }
    
    private static final IChatComponent VERBOSE_HOOK_JOIN = new ChatComponentText("Fish hook join");
    
    public static void onEntityJoin(Entity entity) {
        if (hook == null || entity != hook) {
            EntityPlayerSP player;
            if (entity instanceof EntityFishHook && (player = mc().thePlayer) != null &&
                ((EntityFishHook) entity).angler == player) {
                if (AutoFishConfiguration.isVerbose()) chat(VERBOSE_HOOK_JOIN);
                hook = ((EntityFishHook) entity);
                waitingForHookJoin = false;
            }
        }
    }
    
    private static final IChatComponent VERBOSE_HOOK_DEAD = new ChatComponentText("Fish hook dead");
    private static final IChatComponent VERBOSE_TIMER_DEAD = new ChatComponentText("Fish timer dead");
    
    public static void onEntityDead(
      Entity entity
    ) {
        if (entity == hook) {
            if (AutoFishConfiguration.isVerbose()) chat(VERBOSE_HOOK_DEAD);
            hook = null;
            waitingForHookDead = false;
        }
        
        if (entity == timer) {
            if (AutoFishConfiguration.isVerbose()) chat(VERBOSE_TIMER_DEAD);
            timer = null;
        }
    }
    
    private static final IChatComponent VERBOSE_TIMER_JOIN = new ChatComponentText("Fish timer join");
    private static final IChatComponent VERBOSE_FISH_TIMER_CATCH = new ChatComponentText("Active fish timer catch");
    
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
                if (AutoFishConfiguration.isVerbose()) chat(VERBOSE_TIMER_JOIN);
                timer = (EntityArmorStand) entity;
            }
        } else {
            EntityPlayerSP player;
            if (active &&
                AutoFishConfiguration.isUseFishTimerCheck() && (player = mc().thePlayer) != null &&
                entity.getName().equals(fishingTimerCatchString)) {
                if (AutoFishConfiguration.isVerbose()) chat(VERBOSE_FISH_TIMER_CATCH);
                doCatch(player);
            }
        }
    }
    
    private static final IChatComponent VERBOSE_NOTE_SOUND_CATCH = new ChatComponentText("Active note sound catch");
    private static final IChatComponent VERBOSE_SWIM_SOUND_CATCH = new ChatComponentText("Active swim sound catch");
    
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
                if (AutoFishConfiguration.isVerbose()) chat(VERBOSE_NOTE_SOUND_CATCH);
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
              Math.abs(sound.getZPosF() - hook1.posZ) < expand) {
                if (AutoFishConfiguration.isVerbose()) chat(VERBOSE_SWIM_SOUND_CATCH);
                doCatch((EntityPlayerSP) hook1.angler);
            }
        }
    }
    
    private static final IChatComponent VERBOSE_RIGHT_CLICK = new ChatComponentText("Do right click");
    
    private static void doRightClick() {
        if (doneRightClickThisTick) return;
        if (AutoFishConfiguration.isVerbose()) chat(VERBOSE_RIGHT_CLICK);
        mc().rightClickMouse();
    }
    
    public static synchronized void markDoneRightClick() {
        doneRightClickThisTick = true;
    }
    
    private static final IChatComponent VERBOSE_DO_CATCH = new ChatComponentText("Try catch");
    
    private static synchronized void doCatch(@NotNull EntityPlayerSP player) {
        if (
          hook != null &&
          !waitingForHookDead &&
          checkIsHoldingRod(player) &&
          !doneRightClickThisTick &&
          AutoFishConfiguration.isAutoCatch() &&
          catchClock.ended(AutoFishConfiguration.getAutoCatchDelay())
        ) {
            if (AutoFishConfiguration.isVerbose()) chat(VERBOSE_DO_CATCH);
            doRightClick();
            waitingForHookDead = true;
            throwClock.update();
            if (AutoFishConfiguration.isAntiAfk()) AutoFishAntiAfk.trigger(player);
        }
    }
    
    private static final IChatComponent VERBOSE_DO_THROW = new ChatComponentText("Try throw");
    
    private static synchronized void doThrow(@NotNull EntityPlayerSP player) {
        if (
          hook == null &&
          !waitingForHookJoin &&
          checkIsHoldingRod(player) &&
          !doneRightClickThisTick &&
          AutoFishConfiguration.isAutoThrow() &&
          throwClock.ended(AutoFishConfiguration.getAutoThrowDelay())
        ) {
            if (AutoFishConfiguration.isVerbose()) chat(VERBOSE_DO_THROW);
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
