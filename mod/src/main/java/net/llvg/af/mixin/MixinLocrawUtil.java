package net.llvg.af.mixin;

import cc.polyfrost.oneconfig.events.event.ChatReceiveEvent;
import cc.polyfrost.oneconfig.utils.hypixel.LocrawUtil;
import net.llvg.af.AutoFish;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin (
  value = LocrawUtil.class,
  remap = false
)
public abstract class MixinLocrawUtil {
    @Inject (
      method = "onMessageReceived",
      at = @At (
        value = "INVOKE",
        target = "Lcc/polyfrost/oneconfig/events/EventManager;post(Ljava/lang/Object;)V"
      )
    )
    private void onMessageReceivedInject(
      ChatReceiveEvent event,
      CallbackInfo ci
    ) {
        AutoFish.onLocrawChange();
    }
}
