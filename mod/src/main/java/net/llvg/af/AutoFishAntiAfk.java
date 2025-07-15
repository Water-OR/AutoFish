package net.llvg.af;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import net.minecraft.client.entity.EntityPlayerSP;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.llvg.af.utils.Utility.*;

final class AutoFishAntiAfk {
    private AutoFishAntiAfk() {
        throw new UnsupportedOperationException();
    }
    
    static void init() {
        MoveThread.instance.start();
    }
    
    @SuppressWarnings ("EmptyFinallyBlock")
    static void stop() {
        try {
            MoveThread.stop = true;
            try {
                MoveThread.instance.join();
            } catch (InterruptedException ignored) { }
        } catch (Throwable e) {
            try {
                AutoFish.logger.warn("Failure occur when stopping MoveThread", e);
            } finally {
                // do nothing
            }
        }
    }
    
    private static final Random rand = new Random();
    private static final Object lock = new Object();
    
    private static float lastYawOffset = 0;
    private static float lastPitchOffset = 0;
    
    static void trigger(@NotNull EntityPlayerSP player) {
        synchronized (lock) {
            MoveThread.clear(player);
            float yaw = player.rotationYaw - lastYawOffset;
            float pitch = player.rotationPitch - lastPitchOffset;
            float currYawOffset = (rand.nextFloat() * 2 - 1) * AutoFishConfiguration.getAntiAfkRotationYaw();
            float currPitchOffset = (rand.nextFloat() * 2 - 1) * AutoFishConfiguration.getAntiAfkRotationPitch();
            if (AutoFishConfiguration.isVerbose()) AutoFish.chat("Facing to (", yaw, ", ", pitch, ") + (", currYawOffset, ", ", currPitchOffset, ")");
            MoveThread.put(
              player,
              new Rotation(
                yaw + currYawOffset,
                pitch + currPitchOffset,
                System.currentTimeMillis() + AutoFishConfiguration.getAntiAfkRotationTime()
              )
            );
            lastYawOffset = currYawOffset;
            lastPitchOffset = currPitchOffset;
        }
    }
    
    static void reset(
      @Nullable EntityPlayerSP player,
      boolean faceTo
    ) {
        synchronized (lock) {
            MoveThread.clear(player);
            if (player != null && faceTo) {
                float yaw = player.rotationYaw - lastYawOffset;
                float pitch = player.rotationPitch - lastPitchOffset;
                if (AutoFishConfiguration.isVerbose()) AutoFish.chat("Facing to (", yaw, ", ", pitch, ")");
                MoveThread.put(
                  player,
                  new Rotation(
                    yaw,
                    pitch,
                    System.currentTimeMillis() + AutoFishConfiguration.getAntiAfkRotationTime()
                  )
                );
            }
            lastYawOffset = 0;
            lastPitchOffset = 0;
        }
    }
    
    private static float clampPitch(float value) {
        return clampF(value, -90, 90);
    }
    
    private static final class Rotation {
        public final float yaw;
        public final float pitch;
        public final long time;
        
        public Rotation(
          float yaw,
          float pitch,
          long time
        ) {
            this.yaw = yaw;
            this.pitch = clampPitch(pitch);
            this.time = time;
        }
        
        public static final Comparator<Rotation> comparator = Comparator.comparingLong(it -> it.time);
    }
    
    private static final class MoveThread
      extends Thread
    {
        private static final MoveThread instance = new MoveThread();
        
        private static final Queue<Rotation> que = new PriorityQueue<>(Rotation.comparator);
        private static long lastTime = 0;
        private static float lastYaw = 0;
        private static float lastPitch = 0;
        private static boolean stop = false;
        
        public static void clear(@Nullable EntityPlayerSP player) {
            synchronized (que) {
                que.clear();
                if (player != null) update(player, System.currentTimeMillis());
            }
        }
        
        public static void put(
          @NotNull EntityPlayerSP player,
          Rotation... values
        ) {
            synchronized (que) {
                if (que.isEmpty()) update(player, System.currentTimeMillis());
                for (Rotation value : values) if (value != null) que.offer(value);
            }
        }
        
        private static final long delay = 1;
        
        private MoveThread() {
            super("Auto Fish | anti afk move thread");
        }
        
        @Override
        public synchronized void run() {
            try {
                AutoFish.logger.info("Thread start");
                long curr = System.currentTimeMillis(), next = curr;
                while (!stop) {
                    if ((curr = System.currentTimeMillis()) < next) continue;
                    try {
                        _loop(curr);
                    } catch (Throwable e) {
                        AutoFish.logger.info("Failure occur during loop", e);
                    }
                    next += delay;
                }
            } finally {
                AutoFish.logger.info("Thread dead");
            }
        }
        
        private void _loop(long time) {
            EntityPlayerSP player;
            if ((player = mc().thePlayer) == null) {
                clear(null);
                return;
            }
            
            Rotation first;
            Rotation last = null;
            synchronized (que) {
                while ((first = que.peek()) != null && first.time <= time) last = que.poll();
            }
            if (last != null) {
                rotate(player, last.yaw, last.pitch);
                update(player, time);
            }
            if (first == null) return;
            float progress = clampF(1f - (float) (first.time - time) / (first.time - lastTime), 0f, 1f);
            rotate(
              player,
              lastYaw + wrapTo180F(first.yaw - lastYaw) * progress,
              lastPitch + (first.pitch - lastPitch) * progress
            );
        }
        
        private static void rotate(
          EntityPlayerSP player,
          float yaw,
          float pitch
        ) {
            player.rotationYaw = yaw; // No wrap here or weird rotation
            player.rotationPitch = clampPitch(pitch);
        }
        
        private static void update(
          EntityPlayerSP player,
          long time
        ) {
            lastTime = time;
            lastYaw = player.rotationYaw;
            lastPitch = player.rotationPitch;
        }
    }
}
