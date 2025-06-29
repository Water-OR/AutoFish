package net.llvg.af.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Utility {
    private Utility() {
        throw new UnsupportedOperationException();
    }
    
    public static void call(Object ignored) { }
    
    @NotNull
    public static Minecraft mc() {
        return Minecraft.getMinecraft();
    }
    
    @Nullable
    public static WorldClient world() {
        return mc().theWorld;
    }
    
    @Nullable
    public static EntityPlayerSP player() {
        return mc().thePlayer;
    }
    
    public static float wrapTo180F(float value) {
        value %= 360f;
        if (value >= 180f) value -= 360f;
        if (value < -180f) value += 360f;
        return value;
    }
    
    public static float clampF(
      float value,
      float min,
      float max
    ) {
        return Math.max(Math.min(value, max), min);
    }
}
