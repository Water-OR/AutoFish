package net.llvg.af.mixin;

import net.llvg.af.AutoFish;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.WorldSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin (WorldClient.class)
public abstract class MixinWorldClient {
    @Inject (
      method = "<init>",
      at = @At ("RETURN")
    )
    private void _init_Inject(
      NetHandlerPlayClient p_i45063_1_,
      WorldSettings p_i45063_2_,
      int p_i45063_3_,
      EnumDifficulty p_i45063_4_,
      Profiler p_i45063_5_,
      CallbackInfo ci
    ) {
        AutoFish.onWorldLoad();
    }
    
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
