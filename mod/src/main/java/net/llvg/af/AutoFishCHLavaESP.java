package net.llvg.af;

import cc.polyfrost.oneconfig.config.core.OneColor;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import net.llvg.af.utils.AutoClosableNE;
import net.llvg.af.utils.CullInfo;
import net.llvg.af.utils.Dummy;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.llvg.af.utils.RenderUtility.*;
import static net.llvg.af.utils.Utility.*;
import static org.lwjgl.opengl.GL11.*;

final class AutoFishCHLavaESP {
    private AutoFishCHLavaESP() {
        throw new UnsupportedOperationException();
    }
    
    private static final Map<Chunk, Dummy> scannedChunks = new WeakHashMap<>();
    private static final Map<BlockPos, CullInfo> lavaBlocks = new ConcurrentSkipListMap<>();
    
    @Nullable
    private static ExecutorService executor = null;
    private static final ReadWriteLock lock = new ReentrantReadWriteLock();
    private static final AtomicBoolean scanDisabled = new AtomicBoolean();
    
    private static final int FLAMING_WORM_BEGIN_INCLUSIVE_X = 513;
    private static final int FLAMING_WORM_BEGIN_INCLUSIVE_Y = 64;
    private static final int FLAMING_WORM_BEGIN_INCLUSIVE_Z = 513;
    private static final int FLAMING_WORM_END_INCLUSIVE_X = 824;
    private static final int FLAMING_WORM_END_INCLUSIVE_Y = 189;
    private static final int FLAMING_WORM_END_INCLUSIVE_Z = 824;
    
    private static boolean isBlockLava(Block block) {
        return block == Blocks.lava || block == Blocks.flowing_lava;
    }
    
    private static void addLava(BlockPos pos) {
        synchronized (lavaBlocks) {
            CullInfo selfInfo = lavaBlocks.computeIfAbsent(pos, (k) -> new CullInfo());
            for (EnumFacing face : EnumFacing.values()) {
                CullInfo sideInfo = lavaBlocks.get(pos.offset(face));
                if (sideInfo == null) continue;
                selfInfo.set(face.index, true);
                sideInfo.set(face.opposite, true);
            }
        }
    }
    
    private static void remLava(BlockPos pos) {
        synchronized (lavaBlocks) {
            CullInfo selfInfo = lavaBlocks.remove(pos);
            if (selfInfo == null) return;
            
            for (EnumFacing face : EnumFacing.values()) {
                CullInfo sideInfo = lavaBlocks.get(pos.offset(face));
                if (sideInfo == null) continue;
                sideInfo.set(face.opposite, false);
            }
        }
    }
    
    private static void scanChunkLava(@NotNull Chunk chunk) {
        AutoFish.logger.info("[Auto Fish | CH Lava ESP] Chunk[{}, {}] scanning started", chunk.xPosition, chunk.zPosition);
        if (AutoFishConfiguration.isVerbose()) AutoFish.chat("Chunk[", chunk.xPosition, ", ", chunk.zPosition, "] scanning started");
        
        int xOffset = chunk.xPosition << 4;
        int zOffset = chunk.zPosition << 4;
        
        for (int i = 0; i < 16; ++i) {
            for (int j = 0; j < 16; ++j) {
                if (scanDisabled.get()) {
                    AutoFish.logger.info("[Auto Fish | CH Lava ESP] Chunk[{}, {}] scanning terminated since disabled", chunk.xPosition, chunk.zPosition);
                    if (AutoFishConfiguration.isVerbose()) AutoFish.chat(
                      "Chunk[", chunk.xPosition, ", ", chunk.zPosition, "] scanning terminated since disabled"
                    );
                    return;
                }
                int x = xOffset + i;
                int z = zOffset + j;
                if (
                  x < FLAMING_WORM_BEGIN_INCLUSIVE_X ||
                  z < FLAMING_WORM_BEGIN_INCLUSIVE_Z ||
                  FLAMING_WORM_END_INCLUSIVE_X < x ||
                  FLAMING_WORM_END_INCLUSIVE_Z < z
                ) continue;
                for (int y = FLAMING_WORM_BEGIN_INCLUSIVE_Y; y < FLAMING_WORM_END_INCLUSIVE_Y; ++y) {
                    if (isBlockLava(chunk.getBlock(x, y, z))) addLava(new BlockPos(x, y, z));
                }
            }
        }
        
        AutoFish.logger.info("[Auto Fish | CH Lava ESP] Chunk[{}, {}] scanning finished", chunk.xPosition, chunk.zPosition);
        if (AutoFishConfiguration.isVerbose()) AutoFish.chat("Chunk[", chunk.xPosition, ", ", chunk.zPosition, "] scanning finished");
    }
    
    static void init() { /* For early <clinit> invoke */ }
    
    static void onWorldLoad(@Nullable WorldClient newWorld) {
        ExecutorService oldExecutor;
        try (AutoClosableNE ignored = withLock(lock.writeLock())) {
            oldExecutor = executor;
            executor = null;
            scanDisabled.set(true);
        }
        
        if (oldExecutor != null) try {
            oldExecutor.shutdown();
            AutoFish.logger.info("[Auto Fish | CH Lava ESP] Shutting down executor");
            
            if (!oldExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                oldExecutor.shutdownNow();
                AutoFish.logger.warn("[Auto Fish | CH Lava ESP] Executor isn't terminated in time, forcing");
                
                if (!oldExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                    AutoFish.logger.error("[Auto Fish | CH Lava ESP] Executor isn't terminated in time even forced, this is not permitted");
                    throw new IllegalStateException("[Auto Fish | CH Lava ESP] Executor isn't terminated in time");
                } else AutoFish.logger.info("[Auto Fish | CH Lava ESP] Executor forced terminated");
            } else AutoFish.logger.info("[Auto Fish | CH Lava ESP] Executor terminated");
        } catch (InterruptedException e) {
            AutoFish.logger.info("[Auto Fish | CH Lava ESP] Thread interrupt while waiting executor to terminate", e);
            oldExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        scannedChunks.clear();
        lavaBlocks.clear();
        
        if (newWorld != null) try (AutoClosableNE ignored = withLock(lock.writeLock())) {
            executor = Executors.newCachedThreadPool(new ThreadFactory() {
                private final AtomicInteger threadNumber = new AtomicInteger(0);
                
                @Override
                public Thread newThread(@NotNull Runnable r) {
                    Thread result = new Thread(
                      () -> {
                          AutoFish.logger.info("[Auto Fish | CH Lava ESP] Thread start");
                          try {
                              r.run();
                          } finally {
                              AutoFish.logger.info("[Auto Fish | CH Lava ESP] Thread dead");
                          }
                      },
                      "Auto Fish | CH lava scan thread <" + threadNumber.incrementAndGet() + ">"
                    );
                    if (result.isDaemon()) result.setDaemon(false);
                    return result;
                }
            });
            scanDisabled.set(false);
        }
    }
    
    static void submitChunkScan(@NotNull Chunk chunk) {
        if (
          scannedChunks.get(chunk) == DUMMY ||
          chunk.xPosition < FLAMING_WORM_BEGIN_INCLUSIVE_X >> 4 ||
          chunk.zPosition < FLAMING_WORM_BEGIN_INCLUSIVE_Z >> 4 ||
          FLAMING_WORM_END_INCLUSIVE_X >> 4 < chunk.xPosition ||
          FLAMING_WORM_END_INCLUSIVE_Z >> 4 < chunk.zPosition
        ) return;
        
        if (AutoFishConfiguration.isVerbose()) AutoFish.chat("Submit chunk[", chunk.xPosition, ", ", chunk.zPosition, "] scanning");
        AutoFish.logger.info("[Auto Fish | CH Lava ESP] Submit chunk[{}, {}] scanning", chunk.xPosition, chunk.zPosition);
        try (AutoClosableNE ignored = withLock(lock.writeLock())) {
            if (executor != null) executor.submit(() -> scanChunkLava(chunk));
        }
        scannedChunks.put(chunk, DUMMY);
    }
    
    static void onBlockChange(
      @NotNull BlockPos pos,
      @NotNull IBlockState newState
    ) {
        if (
          !AutoFishConfiguration.isEnabled() ||
          !AutoFish.isInCH() ||
          pos.getX() < FLAMING_WORM_BEGIN_INCLUSIVE_X ||
          pos.getY() < FLAMING_WORM_BEGIN_INCLUSIVE_Y ||
          pos.getZ() < FLAMING_WORM_BEGIN_INCLUSIVE_Z ||
          FLAMING_WORM_END_INCLUSIVE_X < pos.getX() ||
          FLAMING_WORM_END_INCLUSIVE_Y < pos.getY() ||
          FLAMING_WORM_END_INCLUSIVE_Z < pos.getZ()
        ) return;
        
        if (isBlockLava(newState.getBlock())) addLava(pos);
        else remLava(pos);
    }
    
    static void doRender() {
        if (
          !AutoFishConfiguration.isEnabled() ||
          !AutoFish.isInCH() ||
          lavaBlocks.isEmpty()
        ) return;
        try (AutoClosableNE ignored = glWrapBlock()) {
            glDisable(GL_DEPTH_TEST);
            glDisable(GL_TEXTURE_2D);
            glDisable(GL_LIGHTING);
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            
            RenderManager rm = mc().getRenderManager();
            glTranslated(-rm.renderPosX, -rm.renderPosY, -rm.renderPosZ);
            glLineWidth(AutoFishConfiguration.getOutlineWidth());
            
            if (AutoFishConfiguration.isRenderOutline()) {
                OneColor color = AutoFishConfiguration.getOutlineColor();
                glColor4d(
                  color.getRed() / 255.,
                  color.getGreen() / 255.,
                  color.getBlue() / 255.,
                  color.getAlpha() / 255.
                );
                glLineWidth(AutoFishConfiguration.getOutlineWidth());
                glBegin(GL_LINES);
                if (AutoFishConfiguration.isDisableOutlineConnection()) {
                    lavaBlocks.forEach((pos, info) -> {
                        double x = pos.getX();
                        double y = pos.getY();
                        double z = pos.getZ();
                        processOutlinePointsNoConnection(x, y, z, x + 1, y + 1, z + 1);
                    });
                } else {
                    lavaBlocks.forEach((pos, info) -> {
                        double x = pos.getX();
                        double y = pos.getY();
                        double z = pos.getZ();
                        processOutlinePoints(x, y, z, x + 1, y + 1, z + 1, info);
                    });
                }
                glEnd();
            }
            
            if (AutoFishConfiguration.isRenderFace()) {
                OneColor color = AutoFishConfiguration.getFaceColor();
                glColor4d(
                  color.getRed() / 255.,
                  color.getGreen() / 255.,
                  color.getBlue() / 255.,
                  color.getAlpha() / 255.
                );
                glBegin(GL_QUADS);
                lavaBlocks.forEach((pos, info) -> {
                    double x = pos.getX();
                    double y = pos.getY();
                    double z = pos.getZ();
                    processFacePoints(x, y, z, x + 1, y + 1, z + 1, info);
                });
                glEnd();
            }
        }
    }
}
