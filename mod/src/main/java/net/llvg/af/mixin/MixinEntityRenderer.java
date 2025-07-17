package net.llvg.af.mixin;

import net.llvg.af.AutoFish;
import net.llvg.af.inject.InjectProfiler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin (EntityRenderer.class)
public abstract class MixinEntityRenderer
  implements IResourceManagerReloadListener
{
    @Shadow
    private Minecraft mc;
    
    @Inject (
      method = "renderWorldPass",
      at = @At (
        value = "INVOKE",
        target = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V",
        shift = At.Shift.AFTER
      )
    )
    private void renderWorldPassInject(
      int pass,
      float partialTicks,
      long finishTimeNano,
      CallbackInfo ci
    ) {
        if ("hand".equals(InjectProfiler.getLastSectionName(mc.mcProfiler))) AutoFish.onWorldRenderLast();
    }
}
