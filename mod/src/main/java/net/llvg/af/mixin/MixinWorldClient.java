package net.llvg.af.mixin;

import net.llvg.af.AutoFish;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin (WorldClient.class)
public abstract class MixinWorldClient {
    @Inject (
      method = "onEntityAdded",
      at = @At ("HEAD")
    )
    private void onEntityAddedInject(
      Entity entityIn,
      CallbackInfo ci
    ) {
        AutoFish.onEntityJoin(entityIn);
    }
    
    @Inject (
      method = "onEntityRemoved",
      at = @At ("HEAD")
    )
    private void onEntityRemovedInject(
      Entity entityIn,
      CallbackInfo ci
    ) {
        AutoFish.onEntityDead(entityIn);
    }
}
