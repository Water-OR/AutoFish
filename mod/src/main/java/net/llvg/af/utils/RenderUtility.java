package net.llvg.af.utils;

import net.minecraft.util.BlockPos;
import org.jetbrains.annotations.NotNull;

import static org.lwjgl.opengl.GL11.*;

public final class RenderUtility {
    private RenderUtility() {
        throw new UnsupportedOperationException();
    }
    
    public static AutoClosableNE glWrapBlock() {
        glPushAttrib(GL_ALL_ATTRIB_BITS);
        glPushMatrix();
        
        return () -> {
            glPopAttrib();
            glPopMatrix();
        };
    }
    
    public static void processOutlinePoints(
      double minX,
      double minY,
      double minZ,
      double maxX,
      double maxY,
      double maxZ,
      @NotNull CullInfo cull
    ) {
        boolean keepNorth = !cull.getNorth();
        boolean keepSouth = !cull.getSouth();
        boolean keepWest = !cull.getWest();
        boolean keepEast = !cull.getEast();
        
        if (!cull.getDown()) {
            if (keepNorth) {
                glVertex3d(minX, minY, minZ);
                glVertex3d(maxX, minY, minZ);
            }
            if (keepSouth) {
                glVertex3d(maxX, minY, maxZ);
                glVertex3d(minX, minY, maxZ);
            }
            if (keepWest) {
                glVertex3d(minX, minY, minZ);
                glVertex3d(minX, minY, maxZ);
            }
            if (keepEast) {
                glVertex3d(maxX, minY, maxZ);
                glVertex3d(maxX, minY, minZ);
            }
        }
        
        if (!cull.getUp()) {
            if (keepNorth) {
                glVertex3d(maxX, maxY, minZ);
                glVertex3d(minX, maxY, minZ);
            }
            if (keepSouth) {
                glVertex3d(minX, maxY, maxZ);
                glVertex3d(maxX, maxY, maxZ);
            }
            if (keepWest) {
                glVertex3d(minX, maxY, maxZ);
                glVertex3d(minX, maxY, minZ);
            }
            if (keepEast) {
                glVertex3d(maxX, maxY, minZ);
                glVertex3d(maxX, maxY, maxZ);
            }
        }
        
        if (keepNorth) {
            if (keepWest) {
                glVertex3d(minX, minY, minZ);
                glVertex3d(minX, maxY, minZ);
            }
            if (keepEast) {
                glVertex3d(maxX, maxY, minZ);
                glVertex3d(maxX, minY, minZ);
            }
        }
        
        if (keepSouth) {
            if (keepWest) {
                glVertex3d(minX, maxY, maxZ);
                glVertex3d(minX, minY, maxZ);
            }
            if (keepEast) {
                glVertex3d(maxX, minY, maxZ);
                glVertex3d(maxX, maxY, maxZ);
            }
        }
    }
    
    public static void processBlockOutlinePoints(
      @NotNull BlockPos pos,
      @NotNull CullInfo cull
    ) {
        processOutlinePoints(
          pos.getX(),
          pos.getY(),
          pos.getZ(),
          pos.getX() + 1,
          pos.getY() + 1,
          pos.getZ() + 1,
          cull
        );
    }
    
    public static void processOutlinePointsNoConnection(
      double minX,
      double minY,
      double minZ,
      double maxX,
      double maxY,
      double maxZ
    ) {
        // D + N
        glVertex3d(minX, minY, minZ);
        glVertex3d(maxX, minY, minZ);
        // D + S
        glVertex3d(maxX, minY, maxZ);
        glVertex3d(minX, minY, maxZ);
        // D + W
        glVertex3d(minX, minY, minZ);
        glVertex3d(minX, minY, maxZ);
        // D + E
        glVertex3d(maxX, minY, maxZ);
        glVertex3d(maxX, minY, minZ);
        
        // U + N
        glVertex3d(maxX, maxY, minZ);
        glVertex3d(minX, maxY, minZ);
        // U + S
        glVertex3d(minX, maxY, maxZ);
        glVertex3d(maxX, maxY, maxZ);
        // U + W
        glVertex3d(minX, maxY, maxZ);
        glVertex3d(minX, maxY, minZ);
        // U + E
        glVertex3d(maxX, maxY, minZ);
        glVertex3d(maxX, maxY, maxZ);
        
        // N + W
        glVertex3d(minX, minY, minZ);
        glVertex3d(minX, maxY, minZ);
        // N + E
        glVertex3d(maxX, maxY, minZ);
        glVertex3d(maxX, minY, minZ);
        
        // S + W
        glVertex3d(minX, maxY, maxZ);
        glVertex3d(minX, minY, maxZ);
        // S + E
        glVertex3d(maxX, minY, maxZ);
        glVertex3d(maxX, maxY, maxZ);
    }
    
    public static void processBlockOutlinePointsNoConnection(@NotNull BlockPos pos) {
        processOutlinePointsNoConnection(
          pos.getX(),
          pos.getY(),
          pos.getZ(),
          pos.getX() + 1,
          pos.getY() + 1,
          pos.getZ() + 1
        );
    }
    
    public static void processFacePoints(
      double minX,
      double minY,
      double minZ,
      double maxX,
      double maxY,
      double maxZ,
      @NotNull CullInfo cull
    ) {
        if (!cull.getDown()) {
            glVertex3d(minX, minY, minZ);
            glVertex3d(maxX, minY, minZ);
            glVertex3d(minX, minY, maxZ);
            glVertex3d(maxX, minY, maxZ);
            
            glVertex3d(maxX, minY, maxZ);
            glVertex3d(minX, minY, maxZ);
            glVertex3d(maxX, minY, minZ);
            glVertex3d(minX, minY, minZ);
        }
        
        if (!cull.getUp()) {
            glVertex3d(maxX, maxY, maxZ);
            glVertex3d(minX, maxY, maxZ);
            glVertex3d(maxX, maxY, minZ);
            glVertex3d(minX, maxY, minZ);
            
            glVertex3d(minX, maxY, minZ);
            glVertex3d(maxX, maxY, minZ);
            glVertex3d(minX, maxY, maxZ);
            glVertex3d(maxX, maxY, maxZ);
        }
        
        if (!cull.getNorth()) {
            glVertex3d(minX, minY, minZ);
            glVertex3d(minX, maxY, minZ);
            glVertex3d(maxX, minY, minZ);
            glVertex3d(maxX, maxY, minZ);
            
            glVertex3d(maxX, maxY, minZ);
            glVertex3d(maxX, minY, minZ);
            glVertex3d(minX, maxY, minZ);
            glVertex3d(minX, minY, minZ);
        }
        
        if (!cull.getSouth()) {
            glVertex3d(maxX, maxY, maxZ);
            glVertex3d(maxX, minY, maxZ);
            glVertex3d(minX, maxY, maxZ);
            glVertex3d(minX, minY, maxZ);
            
            glVertex3d(minX, minY, maxZ);
            glVertex3d(minX, maxY, maxZ);
            glVertex3d(maxX, minY, maxZ);
            glVertex3d(maxX, maxY, maxZ);
        }
        
        if (!cull.getWest()) {
            glVertex3d(minX, minY, minZ);
            glVertex3d(minX, minY, maxZ);
            glVertex3d(minX, maxY, minZ);
            glVertex3d(minX, maxY, maxZ);
            
            glVertex3d(minX, maxY, maxZ);
            glVertex3d(minX, maxY, minZ);
            glVertex3d(minX, minY, maxZ);
            glVertex3d(minX, minY, minZ);
        }
        
        if (!cull.getEast()) {
            glVertex3d(maxX, maxY, maxZ);
            glVertex3d(maxX, maxY, minZ);
            glVertex3d(maxX, minY, maxZ);
            glVertex3d(maxX, minY, minZ);
            
            glVertex3d(maxX, minY, minZ);
            glVertex3d(maxX, minY, maxZ);
            glVertex3d(maxX, maxY, minZ);
            glVertex3d(maxX, maxY, maxZ);
        }
    }
    
    public static void processBlockFacePoints(
      @NotNull BlockPos pos,
      @NotNull CullInfo cull
    ) {
        processFacePoints(
          pos.getX(),
          pos.getY(),
          pos.getZ(),
          pos.getX() + 1,
          pos.getY() + 1,
          pos.getZ() + 1,
          cull
        );
    }
}
