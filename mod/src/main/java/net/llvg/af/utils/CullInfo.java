package net.llvg.af.utils;

public final class CullInfo {
    private int value = 0;
    
    private static void check(int i) {
        if (i < 0 || 6 <= i) throw new IndexOutOfBoundsException("[i] " + i + " must be in range (0, 6]");
    }
    
    public void set(
      int i,
      boolean v
    ) {
        check(i);
        if (v) value |= 1 << i;
        else value &= ~(1 << i);
    }
    
    public boolean get(int i) {
        check(i);
        return (value & 1 << i) != 0;
    }
}
