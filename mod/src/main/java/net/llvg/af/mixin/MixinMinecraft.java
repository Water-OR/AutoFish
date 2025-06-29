package net.llvg.af.mixin;

import net.llvg.af.AutoFish;
import net.llvg.af.AutoFishAntiAfk;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin (Minecraft.class)
public abstract class MixinMinecraft {
    @Inject (
      method = "run",
      at = @At ("HEAD")
    )
    private void runInject(CallbackInfo ci) {
        AutoFishAntiAfk.startThread();
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
      method = "rightClickMouse",
      at = @At (value = "HEAD")
    )
    private void rightClickMouseInject(CallbackInfo ci) {
        AutoFish.markDoneRightClick();
    }
}
