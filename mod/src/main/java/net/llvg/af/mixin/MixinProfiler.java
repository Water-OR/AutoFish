package net.llvg.af.mixin;

import net.llvg.af.inject.InjectProfiler;
import net.minecraft.profiler.Profiler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin (Profiler.class)
public class MixinProfiler
  implements InjectProfiler
{
    @Unique
    @Nullable
    private String $af$lastSectionName = null;
    
    @Inject (
      method = "startSection",
      at = @At ("HEAD")
    )
    private void startSectionInject(
      String name,
      CallbackInfo ci
    ) {
        $af$lastSectionName = name;
    }
    
    @Override
    @Nullable
    public String get$af$lastSectionName() {
        return $af$lastSectionName;
    }
}
