package net.llvg.af;

import java.lang.ref.WeakReference;
import java.util.Random;
import java.util.function.Consumer;
import net.minecraft.client.entity.EntityPlayerSP;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.llvg.af.utils.Utility.*;

final class AutoFishAntiAfk {
    private AutoFishAntiAfk() {
        throw new UnsupportedOperationException();
    }
    
    static void init() { /* For <clinit> invoke */ }
    
    private static final Thread thread = new Thread(AutoFishAntiAfk::asyncLoop, "Auto Fish | Anti afk rotator thread");
    
    static {
        thread.start();
    }
    
    private enum ThreadState {
        RUNNING,
        SUSPEND,
        STOPPED,
    }
    
    @NotNull
    private static ThreadState state = ThreadState.SUSPEND;
    private static final Object stateLock = new Object();
    
    private static volatile int waitForCancel = 0;
    private static final Object waitForCancelLock = new Object();
    
    @Nullable
    private static Consumer<@NotNull EntityPlayerSP> blockLoop = null;
    
    static void stop() {
        if (!thread.isAlive()) {
            AutoFish.logger.warn("Anti afk rotator thread is not alive, this is not permitted");
            return;
        }
        cancel();
        thread.interrupt();
        
        synchronized (stateLock) {
            if (state != ThreadState.STOPPED) try {
                stateLock.wait();
            } catch (InterruptedException e) {
                AutoFish.logger.info("Interrupted while waiting stopping", e);
                Thread.currentThread().interrupt();
            }
        }
    }
    
    
    @Nullable
    private static volatile Rotation rotation = null;
    
    private static float prevYawOffset = 0;
    private static float prevPitchOffset = 0;
    
    private static final Random rand = new Random();
    
    private static void cancel() {
        synchronized (stateLock) {
            ++waitForCancel;
            blockLoop = null;
            if (state == ThreadState.RUNNING) try {
                stateLock.wait();
            } catch (InterruptedException e) {
                AutoFish.logger.warn("Thread interrupt while waiting cancelling", e);
                Thread.currentThread().interrupt();
            } finally {
                if (--waitForCancel <= 0) synchronized (waitForCancelLock) {
                    rotation = null;
                    waitForCancelLock.notifyAll();
                }
            }
        }
    }
    
    private static void rotate(
      float yaw,
      float pitch
    ) {
        Rotation newRotation;
        synchronized (stateLock) {
            if (state != ThreadState.SUSPEND) return;
            rotation = newRotation = new Rotation(yaw, pitch, AutoFishConfiguration.getAntiAfkRotationTime());
            if (AutoFishConfiguration.isUseAsyncRotation()) stateLock.notifyAll();
            else blockLoop = new Consumer<@NotNull EntityPlayerSP>() {
                private final float beginYawOffset = prevYawOffset;
                private final float beginPitchOffset = prevPitchOffset;
                
                @NotNull
                private final Rotation r = newRotation;
                
                long currTime;
                long prevTime = System.currentTimeMillis();
                
                @Override
                public void accept(@NotNull EntityPlayerSP player) {
                    if (
                      (player = mc().thePlayer) == null ||
                      (currTime = System.currentTimeMillis()) - r.begin > r.duration ||
                      currTime - prevTime < 1
                    ) return;
                    
                    float progress = clampF((float) (currTime - r.begin) / r.duration, 0f, 1f);
                    float currYawOffset = progress * (r.yaw - beginYawOffset) + beginYawOffset;
                    float currPitchOffset = progress * (r.pitch - beginPitchOffset) + beginPitchOffset;
                    
                    player.rotationYaw = (player.rotationYaw + currYawOffset - prevYawOffset) % 360;
                    player.rotationPitch = clampF(player.rotationPitch + currPitchOffset - prevPitchOffset, -90, 90);
                    
                    prevTime = currTime;
                    prevYawOffset = currYawOffset;
                    prevPitchOffset = currPitchOffset;
                }
            };
        }
    }
    
    static void trigger() {
        cancel();
        rotate(
          (rand.nextFloat() * 2 - 1) * AutoFishConfiguration.getAntiAfkRotationYaw(),
          (rand.nextFloat() * 2 - 1) * AutoFishConfiguration.getAntiAfkRotationPitch()
        );
    }
    
    static void reset(boolean rotate) {
        cancel();
        if (rotate) rotate(0, 0);
    }
    
    static void onGameLoop() {
        Consumer<@NotNull EntityPlayerSP> loop;
        if ((loop = blockLoop) == null) return;
        
        EntityPlayerSP player;
        if ((player = mc().thePlayer) == null) return;
        
        loop.accept(player);
    }
    
    private static void asyncLoop() {
        AutoFish.logger.info("Thread start");
loop:
        while (true) {
            Rotation r;
            synchronized (stateLock) {
                while ((r = rotation) == null) {
                    try {
                        stateLock.wait();
                    } catch (InterruptedException e) {
                        AutoFish.logger.info("Interrupted, stopping", e);
                        Thread.currentThread().interrupt();
                        break loop;
                    }
                    AutoFish.logger.info("Wake up, rotation: {}", rotation);
                    if (AutoFishConfiguration.isVerbose()) AutoFish.chat("Wake up, rotation: ", rotation);
                }
                state = ThreadState.RUNNING;
            }
            
            float beginYawOffset = prevYawOffset;
            float beginPitchOffset = prevPitchOffset;
            
            long currTime;
            long prevTime = System.currentTimeMillis();
            WeakReference<EntityPlayerSP> playerRef = new WeakReference<>(mc().thePlayer);
            
            while (waitForCancel == 0 && (currTime = System.currentTimeMillis()) - r.begin <= r.duration) {
                EntityPlayerSP player;
                if ((player = playerRef.get()) == null) break;
                if (currTime - prevTime < 1) continue;
                
                float progress = clampF((float) (currTime - r.begin) / r.duration, 0f, 1f);
                float currYawOffset = progress * (r.yaw - beginYawOffset) + beginYawOffset;
                float currPitchOffset = progress * (r.pitch - beginPitchOffset) + beginPitchOffset;
                
                player.rotationYaw = (player.rotationYaw + currYawOffset - prevYawOffset) % 360;
                player.rotationPitch = clampF(player.rotationPitch + currPitchOffset - prevPitchOffset, -90, 90);
                
                prevTime = currTime;
                prevYawOffset = currYawOffset;
                prevPitchOffset = currPitchOffset;
            }
            
            rotation = null;
            synchronized (stateLock) {
                state = ThreadState.SUSPEND;
                stateLock.notifyAll();
            }
            
            synchronized (waitForCancelLock) {
                while (waitForCancel > 0) {
                    try {
                        waitForCancelLock.wait();
                    } catch (InterruptedException e) {
                        AutoFish.logger.info("Interrupted, stopping", e);
                        Thread.currentThread().interrupt();
                        break loop;
                    }
                }
            }
        }
        
        AutoFish.logger.info("Thread dead");
        synchronized (stateLock) {
            state = ThreadState.STOPPED;
            stateLock.notifyAll();
        }
    }
    
    private static final class Rotation {
        public final float yaw;
        public final float pitch;
        public final long begin;
        public final long duration;
        
        public Rotation(
          float yaw,
          float pitch,
          long duration
        ) {
            this.begin = System.currentTimeMillis();
            this.yaw = yaw;
            this.pitch = pitch;
            this.duration = duration;
        }
        
        @Override
        public String toString() {
            return "Rotation{begin=" + begin + ",duration=" + duration + ",yaw=" + yaw + ",pitch=" + pitch + "}";
        }
    }
}
