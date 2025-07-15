package net.llvg.af.mixin;

import net.llvg.af.AutoFish;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin (Chunk.class)
public abstract class MixinChunk {
    @Inject (
      method = "fillChunk",
      at = @At ("RETURN")
    )
    private void fillChunkInject(
      byte[] p_177439_1_,
      int p_177439_2_,
      boolean p_177439_3_,
      CallbackInfo ci
    ) {
        AutoFish.onChunkLoad((Chunk) (Object) this);
    }
    
    @Inject (
      method = "setBlockState",
      at = @At (value = "HEAD")
    )
    private void setBlockStateInject(
      BlockPos pos,
      IBlockState newState,
      CallbackInfoReturnable<IBlockState> cir
    ) {
        AutoFish.onBlockChange(pos, newState);
    }
}
