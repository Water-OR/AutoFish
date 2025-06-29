package net.llvg.af;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;
import net.minecraft.client.entity.EntityPlayerSP;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.llvg.af.utils.Utility.*;

public final class AutoFishAntiAfk {
    private AutoFishAntiAfk() {
        throw new UnsupportedOperationException();
    }
    
    public static void startThread() {
        MoveThread.instance.start();
    }
    
    static void init() { }
    
    static void trigger(@NotNull EntityPlayerSP player) {
        long time = System.currentTimeMillis();
        float yaw = MoveThread.getBeginYaw();
        float pitch = MoveThread.getBeginPitch();
        MoveThread.clear(player, time);
        float offset = AutoFishConfiguration.getAntiAfkRotation();
        int time1 = AutoFishConfiguration.getAntiAfkRotationTime();
        MoveThread.put(
          new Rotation(yaw - offset, pitch, time + (time1 / 4)),
          new Rotation(yaw + offset, pitch, time + (time1 / 2)),
          new Rotation(yaw, pitch, time + (time1 - time1 / 4))
        );
    }
    
    static void onWorldLoad() {
        MoveThread.clear(player(), System.currentTimeMillis());
    }
    
    private static float clampPitch(float value) {
        return clampF(value, -90, 90);
    }
    
    private static class Rotation {
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
    
    private final static class MoveThread
      extends Thread
    {
        private static final MoveThread instance = new MoveThread();
        
        private static final Queue<Rotation> que = new PriorityQueue<>(Rotation.comparator);
        private static long lastTime = 0;
        private static float lastYaw = 0;
        private static float lastPitch = 0;
        
        private static float beginYaw = 0;
        private static float beginPitch = 0;
        
        public static float getBeginYaw() {
            return beginYaw;
        }
        
        public static float getBeginPitch() {
            return beginPitch;
        }
        
        public static void clear(
          @Nullable EntityPlayerSP player,
          long time
        ) {
            synchronized (que) {
                que.clear();
                if (player != null) {
                    update(player, time);
                    beginYaw = player.rotationYaw;
                    beginPitch = clampPitch(player.rotationPitch);
                }
            }
        }
        
        public static void put(Rotation... values) {
            synchronized (que) {
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
                long curr = System.currentTimeMillis(), next = curr;
                while (!isInterrupted()) {
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
            if ((player = player()) == null) {
                clear(null, time);
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
            if (first == null) {
                update(player, time);
                beginYaw = player.rotationYaw;
                beginPitch = clampPitch(player.rotationPitch);
                return;
            }
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
