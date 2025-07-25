package net.llvg.af.mixin;

import net.llvg.af.AutoFish;
import net.llvg.af.inject.InjectProfiler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.profiler.Profiler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin (Minecraft.class)
public abstract class MixinMinecraft {
    @Shadow
    @Final
    public Profiler mcProfiler;
    
    @Inject (
      method = "startGame",
      at = @At ("TAIL")
    )
    private void startGameInject(CallbackInfo ci) {
        AutoFish.onGameStarted();
    }
    
    @Inject (
      method = "shutdownMinecraftApplet",
      at = @At ("HEAD")
    )
    @SuppressWarnings ("EmptyFinallyBlock")
    private void shutdownMinecraftAppletInject(CallbackInfo ci) {
        try {
            AutoFish.onGameStop();
        } catch (Throwable e) {
            try {
                AutoFish.logger.warn("Failure occur when game stop", e);
            } finally {
                // do nothing
            }
        }
    }
    
    @Inject (
      method = "runTick",
      at = @At (
        value = "INVOKE",
        target = "Lnet/minecraftforge/fml/common/FMLCommonHandler;onPreClientTick()V",
        remap = false
      )
    )
    private void runTickInject0(CallbackInfo ci) {
        AutoFish.onTickStart();
    }
    
    @Inject (
      method = "runTick",
      at = @At (
        value = "INVOKE",
        target = "Lnet/minecraftforge/fml/common/FMLCommonHandler;onPostClientTick()V",
        remap = false
      )
    )
    private void runTickInject1(CallbackInfo ci) {
        AutoFish.onTickEnd();
    }
    
    @Inject (
      method = "runGameLoop",
      at = @At (
        value = "INVOKE",
        target = "Lnet/minecraft/profiler/Profiler;startSection(Ljava/lang/String;)V",
        shift = At.Shift.AFTER
      )
    )
    private void runGameLoopInject(CallbackInfo ci) {
        if ("tick".equals(InjectProfiler.getLastSectionName(mcProfiler))) AutoFish.onGameLoop();
    }
    
    @Inject (
      method = "rightClickMouse",
      at = @At (value = "HEAD")
    )
    private void rightClickMouseInject(CallbackInfo ci) {
        AutoFish.onRightClick();
    }
    
    @Inject (
      method = "loadWorld(Lnet/minecraft/client/multiplayer/WorldClient;Ljava/lang/String;)V",
      at = @At ("HEAD")
    )
    private void loadWorldInject(
      WorldClient worldClientIn,
      String loadingMessage,
      CallbackInfo ci
    ) {
        AutoFish.onWorldLoad(worldClientIn);
    }
}
