package net.llvg.af.mixin;

import net.llvg.af.AutoFish;
import net.minecraft.client.main.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin (
  value = Main.class,
  remap = false
)
public abstract class MixinMain {
    @Inject (
      method = "main",
      at = @At ("HEAD")
    )
    private static void mainInject(
      String[] strings,
      CallbackInfo ci
    ) {
        AutoFish.init();
        AutoFish.logger.info(
          "\nFinished initializing <|Auto Fish|>\nclass '{}'\nloader-class '{}'",
          AutoFish.class.getName(),
          AutoFish.class.getClassLoader().getClass().getName()
        );
    }
}
