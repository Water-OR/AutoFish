package net.llvg.af.utils;

import java.util.concurrent.locks.Lock;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Utility {
    private Utility() {
        throw new UnsupportedOperationException();
    }
    
    @Nullable
    private static Minecraft _mc = null;
    
    @NotNull
    public static Minecraft mc() {
        Minecraft mc;
        if ((mc = _mc) != null) return mc;
        if ((mc = Minecraft.getMinecraft()) != null) {
            _mc = mc;
            return mc;
        }
        throw new IllegalStateException("Minecraft is not initialized yet");
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
    
    public static final Dummy DUMMY = Dummy.instance;
    
    @NotNull
    public static AutoClosableNE withLock(@NotNull Lock lock) {
        lock.lock();
        return lock::unlock;
    }
}
