package net.llvg.af.mixin;

import net.llvg.af.AutoFish;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin (SoundManager.class)
public abstract class MixinSoundManager {
    @Shadow
    private boolean loaded;
    
    @Inject (
      method = "playSound",
      at = @At ("HEAD")
    )
    public void playSoundInject(
      ISound p_sound,
      CallbackInfo ci
    ) {
        if (loaded) AutoFish.onSound(p_sound);
    }
}
