package net.llvg.af.mixin;

import net.llvg.af.AutoFish;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin (EntityRenderer.class)
public abstract class MixinEntityRenderer
  implements IResourceManagerReloadListener
{
    @Inject (
      method = "renderWorldPass",
      at = @At (
        value = "INVOKE",
        target = "Lnet/minecraftforge/client/ForgeHooksClient;dispatchRenderLast(Lnet/minecraft/client/renderer/RenderGlobal;F)V",
        remap = false
      )
    )
    private void renderWorldPassInject(
      int pass,
      float partialTicks,
      long finishTimeNano,
      CallbackInfo ci
    ) {
        AutoFish.onWorldRenderLast();
    }
}
