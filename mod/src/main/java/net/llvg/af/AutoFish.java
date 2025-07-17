package net.llvg.af;

import cc.polyfrost.oneconfig.utils.hypixel.HypixelUtils;
import cc.polyfrost.oneconfig.utils.hypixel.LocrawInfo;
import cc.polyfrost.oneconfig.utils.hypixel.LocrawUtil;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.regex.Pattern;
import net.llvg.af.utils.Clock;
import net.llvg.af.utils.Dummy;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S1CPacketEntityMetadata;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.chunk.Chunk;
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
    
    private static final String prefix = "Auto Fish §b§l|>§r ";
    
    public static void chat(Object... args) {
        if (args == null) return;
        GuiIngame ui;
        
        if ((ui = mc().ingameGUI) == null) return;
        StringBuilder builder = new StringBuilder(prefix);
        for (Object arg : args) {
            if (arg instanceof IChatComponent) builder.append(((IChatComponent) arg).getFormattedText());
            else builder.append(arg);
        }
        ui.getChatGUI().printChatMessage(new ChatComponentText(builder.toString()));
    }
    
    private static final Object stateLock = new Object();
    private static volatile boolean active = false;
    
    public static boolean isActive() {
        return active;
    }
    
    static void toggle() {
        boolean wasActive;
        synchronized (stateLock) {
            wasActive = (active = !active && AutoFishConfiguration.isEnabled());
        }
        if (wasActive) {
            chat("Auto fish is now §aon");
        } else {
            chat("Auto fish is now §coff");
            AutoFishAntiAfk.reset(AutoFishConfiguration.isResetFacingWhenNotFishing());
        }
    }
    
    @Nullable
    private static WeakReference<EntityFishHook> hookRef = null;
    
    @Nullable
    public static EntityFishHook getHook() {
        WeakReference<EntityFishHook> ref;
        return (ref = hookRef) == null ? null : ref.get();
    }
    
    private enum HookWaitingState {
        WAITING_JOIN,
        WAITING_DEAD,
    }
    
    @Nullable
    private static HookWaitingState hookWaitingState = null;
    
    public static final Pattern fishingTimerWarnRegex = Pattern.compile("§e§l(\\d+(\\.\\d+)?)");
    public static final String fishingTimerCatchString = "§c§l!!!";
    
    public static boolean matchFishingTimer(String str) {
        return fishingTimerWarnRegex.matcher(str).matches() || str.equals(fishingTimerCatchString);
    }
    
    @Nullable
    private static WeakReference<EntityArmorStand> timerRef = null;
    
    @Nullable
    public static EntityArmorStand getTimer() {
        WeakReference<EntityArmorStand> ref;
        return (ref = timerRef) == null ? null : ref.get();
    }
    
    private static void setTimer(@Nullable EntityArmorStand value) {
        timerRef = value == null ? null : new WeakReference<>(value);
    }
    
    private static final Clock catchClock = new Clock();
    private static final Clock throwClock = new Clock();
    
    private static final Clock throwFirstClock = new Clock();
    
    private static boolean doneRightClickThisTick = false;
    
    private static void doRightClick() {
        if (doneRightClickThisTick) return;
        if (AutoFishConfiguration.isVerbose()) chat("Do right click, time=", System.currentTimeMillis());
        mc().rightClickMouse();
    }
    
    public static synchronized void onRightClick() {
        if (AutoFishConfiguration.isVerbose()) chat("Done right click, time=", System.currentTimeMillis());
        doneRightClickThisTick = true;
    }
    
    @Nullable
    private static ItemStack heldItem = null;
    private static boolean heldRod = false;
    
    public static void onTickStart() {
        doneRightClickThisTick = false;
    }
    
    public static void onTickEnd() {
        EntityPlayerSP player;
        if ((player = mc().thePlayer) == null) return;
        
        ItemStack holdingItem;
        if (heldItem != (holdingItem = player.getHeldItem())) {
            boolean holdingRod = holdingItem != null && holdingItem.getItem() instanceof ItemFishingRod;
            if (isActive() && heldRod && !holdingRod) AutoFishAntiAfk.reset(AutoFishConfiguration.isResetFacingWhenNotFishing());
            heldRod = holdingRod;
            heldItem = holdingItem;
            throwFirstClock.update();
        }
        
        if (isActive()) doThrow();
    }
    
    private static final Map<Chunk, Dummy> loadedChunks = new WeakHashMap<>();
    
    public static Set<Chunk> getLoadedChunks() {
        return Collections.unmodifiableSet(loadedChunks.keySet());
    }
    
    @SuppressWarnings ("BooleanMethodIsAlwaysInverted")
    public static boolean isInCH() {
        if (!HypixelUtils.INSTANCE.isHypixel()) return false;
        LocrawInfo i;
        
        return
          (i = LocrawUtil.INSTANCE.getLocrawInfo()) != null &&
          LocrawInfo.GameType.SKYBLOCK == i.getGameType() &&
          "crystal_hollows".equals(i.getGameMode());
    }
    
    public static void onLocrawChange() {
        if (AutoFishConfiguration.isVerbose()) chat("New Locraw: ", LocrawUtil.INSTANCE.getLocrawInfo());
        if (
          AutoFishConfiguration.isEnabled() &&
          AutoFishConfiguration.isScanCHLava() &&
          AutoFish.isInCH()
        ) getLoadedChunks().forEach(AutoFishCHLavaESP::submitChunkScan);
    }
    
    public static void onWorldLoad(@Nullable WorldClient newWorld) {
        hookRef = null;
        timerRef = null;
        heldItem = null;
        hookWaitingState = null;
        loadedChunks.clear();
        
        if (newWorld != null && isActive()) {
            throwFirstClock.update();
            toggle();
        }
        
        AutoFishAntiAfk.reset(false);
        AutoFishCHLavaESP.onWorldLoad(newWorld);
    }
    
    public static void onChunkLoad(@NotNull Chunk chunk) {
        loadedChunks.put(chunk, Dummy.instance);
        if (
          AutoFishConfiguration.isEnabled() &&
          AutoFishConfiguration.isScanCHLava() &&
          AutoFish.isInCH()
        ) AutoFishCHLavaESP.submitChunkScan(chunk);
    }
    
    public static void onBlockChange(
      @NotNull BlockPos pos,
      @NotNull IBlockState newState
    ) {
        AutoFishCHLavaESP.onBlockChange(pos, newState);
    }
    
    public static void onWorldRenderLast() {
        AutoFishCHLavaESP.doRender();
    }
    
    public static void onEntityJoin(@NotNull Entity entity) {
        EntityFishHook hook1;
        if (
          (hook1 = getHook()) != null && entity == hook1 ||
          !(entity instanceof EntityFishHook) ||
          ((EntityFishHook) entity).angler != mc().thePlayer
        ) return;
        
        if (AutoFishConfiguration.isVerbose()) chat("Fishing hook join");
        hookRef = new WeakReference<>((EntityFishHook) entity);
        hookWaitingState = null;
    }
    
    public static void onEntityDead(@NotNull Entity entity) {
        if (entity == getHook()) {
            if (AutoFishConfiguration.isVerbose()) chat("Fishing hook dead");
            hookRef = null;
            if (hookWaitingState != HookWaitingState.WAITING_JOIN) hookWaitingState = null;
            return;
        }
        
        if (entity == getTimer()) {
            if (AutoFishConfiguration.isVerbose()) chat("Fishing timer dead");
            timerRef = null;
        }
    }
    
    public static void onS1CPacket(
      S1CPacketEntityMetadata ignored,
      Entity entity
    ) {
        EntityArmorStand timer;
        if ((timer = getTimer()) != null && entity == timer) {
            if (
              isActive() &&
              AutoFishConfiguration.isUseFishTimerCheck() &&
              entity.getCustomNameTag().equals(fishingTimerCatchString)
            ) {
                if (AutoFishConfiguration.isVerbose()) chat("Active fishing timer catch");
                doCatch();
            }
        } else if (
          entity instanceof EntityArmorStand &&
          matchFishingTimer(entity.getCustomNameTag())
        ) {
            if (AutoFishConfiguration.isVerbose()) chat("Fishing timer join");
            setTimer((EntityArmorStand) entity);
        }
    }
    
    public static void onSound(ISound sound) {
        if (!isActive()) return;
        EntityFishHook hook1;
        if ((hook1 = getHook()) == null) return;
        
        switch (sound.getSoundLocation().getResourcePath()) {
            case "note.pling":
                if (
                  !AutoFishConfiguration.isUseNoteSoundCheck() ||
                  sound.getPitch() != 1 ||
                  sound.getVolume() != 1
                ) break;
                if (AutoFishConfiguration.isVerbose()) chat("Active note sound catch");
                doCatch();
                break;
            
            case "game.player.swim.splash":
                if (!AutoFishConfiguration.isUseSwimSoundCheck()) break;
                float expand = AutoFishConfiguration.getExpandSwimSoundCheckRange();
                if (
                  Math.abs(sound.getXPosF() - hook1.posX) >= expand ||
                  Math.abs(sound.getYPosF() - hook1.posY) >= expand ||
                  Math.abs(sound.getZPosF() - hook1.posZ) >= expand
                ) break;
                if (AutoFishConfiguration.isVerbose()) chat("Active swim sound catch");
                doCatch();
                break;
        }
    }
    
    private static void doCatch() {
        if (
          !AutoFishConfiguration.isAutoCatch() ||
          doneRightClickThisTick ||
          !heldRod ||
          getHook() == null ||
          hookWaitingState == null ||
          !catchClock.ended(AutoFishConfiguration.getAutoCatchDelay())
        ) return;
        if (AutoFishConfiguration.isVerbose()) chat("Do catch, time=", System.currentTimeMillis());
        doRightClick();
        hookWaitingState = HookWaitingState.WAITING_DEAD;
        throwClock.update();
        if (AutoFishConfiguration.isAntiAfk()) AutoFishAntiAfk.trigger();
    }
    
    private static void doThrow() {
        if (
          !AutoFishConfiguration.isAutoThrow() ||
          doneRightClickThisTick ||
          !heldRod ||
          hookWaitingState == HookWaitingState.WAITING_JOIN ||
          getHook() != null && !(AutoFishConfiguration.isDoNotWaitHookDead() && hookWaitingState == HookWaitingState.WAITING_DEAD) ||
          !throwClock.ended(AutoFishConfiguration.getAutoThrowDelay()) ||
          !throwFirstClock.ended(AutoFishConfiguration.getAutoThrowFirstDelay())
        ) return;
        if (AutoFishConfiguration.isVerbose()) chat("Do throw, time=", System.currentTimeMillis());
        doRightClick();
        hookWaitingState = HookWaitingState.WAITING_JOIN;
        catchClock.update();
    }
    
    public static void init() { /* For <clinit> invoke */ }
    
    static {
        AutoFishCHLavaESP.init();
        AutoFishAntiAfk.init();
    }
    
    public static void onGameStarted() {
        AutoFishConfiguration.init();
    }
    
    public static void onGameStop() {
        AutoFishAntiAfk.stop();
    }
}
