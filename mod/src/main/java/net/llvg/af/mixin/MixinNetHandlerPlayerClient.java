package net.llvg.af.mixin;

import net.llvg.af.AutoFish;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.server.S1CPacketEntityMetadata;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin (NetHandlerPlayClient.class)
public class MixinNetHandlerPlayerClient {
    @Inject (
      method = "handleEntityMetadata",
      at = @At ("TAIL"),
      locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void handleEntityMetadataInject(
      S1CPacketEntityMetadata packetIn,
      CallbackInfo ci,
      Entity entity
    ) {
        AutoFish.onS1CPacket(packetIn, entity);
    }
}
