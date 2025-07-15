package net.llvg.af.utils;

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
      CullInfo cull
    ) {
        boolean cullNorth = cull.get(2);
        boolean cullSouth = cull.get(3);
        boolean callWest = cull.get(4);
        boolean cullEast = cull.get(5);
        
        if (!cull.get(0)) {
            if (!cullNorth) {
                glVertex3d(minX, minY, minZ);
                glVertex3d(maxX, minY, minZ);
            }
            if (!cullSouth) {
                glVertex3d(maxX, minY, maxZ);
                glVertex3d(minX, minY, maxZ);
            }
            if (!callWest) {
                glVertex3d(minX, minY, minZ);
                glVertex3d(minX, minY, maxZ);
            }
            if (!cullEast) {
                glVertex3d(maxX, minY, maxZ);
                glVertex3d(maxX, minY, minZ);
            }
        }
        
        if (!cull.get(1)) {
            if (!cullNorth) {
                glVertex3d(maxX, maxY, minZ);
                glVertex3d(minX, maxY, minZ);
            }
            if (!cullSouth) {
                glVertex3d(minX, maxY, maxZ);
                glVertex3d(maxX, maxY, maxZ);
            }
            if (!callWest) {
                glVertex3d(minX, maxY, maxZ);
                glVertex3d(minX, maxY, minZ);
            }
            if (!cullEast) {
                glVertex3d(maxX, maxY, minZ);
                glVertex3d(maxX, maxY, maxZ);
            }
        }
        
        if (!cullNorth) {
            if (!callWest) {
                glVertex3d(minX, minY, minZ);
                glVertex3d(minX, maxY, minZ);
            }
            if (!cullEast) {
                glVertex3d(maxX, maxY, minZ);
                glVertex3d(maxX, minY, minZ);
            }
        }
        
        if (!cullSouth) {
            if (!callWest) {
                glVertex3d(minX, maxY, maxZ);
                glVertex3d(minX, minY, maxZ);
            }
            if (!cullEast) {
                glVertex3d(maxX, minY, maxZ);
                glVertex3d(maxX, maxY, maxZ);
            }
        }
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
    
    public static void processFacePoints(
      double minX,
      double minY,
      double minZ,
      double maxX,
      double maxY,
      double maxZ,
      CullInfo cull
    ) {
        if (!cull.get(0)) {
            glVertex3d(minX, minY, minZ);
            glVertex3d(maxX, minY, minZ);
            glVertex3d(minX, minY, maxZ);
            glVertex3d(maxX, minY, maxZ);
            
            glVertex3d(maxX, minY, maxZ);
            glVertex3d(minX, minY, maxZ);
            glVertex3d(maxX, minY, minZ);
            glVertex3d(minX, minY, minZ);
        }
        
        if (!cull.get(1)) {
            glVertex3d(maxX, maxY, maxZ);
            glVertex3d(minX, maxY, maxZ);
            glVertex3d(maxX, maxY, minZ);
            glVertex3d(minX, maxY, minZ);
            
            glVertex3d(minX, maxY, minZ);
            glVertex3d(maxX, maxY, minZ);
            glVertex3d(minX, maxY, maxZ);
            glVertex3d(maxX, maxY, maxZ);
        }
        
        if (!cull.get(2)) {
            glVertex3d(minX, minY, minZ);
            glVertex3d(minX, maxY, minZ);
            glVertex3d(maxX, minY, minZ);
            glVertex3d(maxX, maxY, minZ);
            
            glVertex3d(maxX, maxY, minZ);
            glVertex3d(maxX, minY, minZ);
            glVertex3d(minX, maxY, minZ);
            glVertex3d(minX, minY, minZ);
        }
        
        if (!cull.get(3)) {
            glVertex3d(maxX, maxY, maxZ);
            glVertex3d(maxX, minY, maxZ);
            glVertex3d(minX, maxY, maxZ);
            glVertex3d(minX, minY, maxZ);
            
            glVertex3d(minX, minY, maxZ);
            glVertex3d(minX, maxY, maxZ);
            glVertex3d(maxX, minY, maxZ);
            glVertex3d(maxX, maxY, maxZ);
        }
        
        if (!cull.get(4)) {
            glVertex3d(minX, minY, minZ);
            glVertex3d(minX, minY, maxZ);
            glVertex3d(minX, maxY, minZ);
            glVertex3d(minX, maxY, maxZ);
            
            glVertex3d(minX, maxY, maxZ);
            glVertex3d(minX, maxY, minZ);
            glVertex3d(minX, minY, maxZ);
            glVertex3d(minX, minY, minZ);
        }
        
        if (!cull.get(5)) {
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
}
