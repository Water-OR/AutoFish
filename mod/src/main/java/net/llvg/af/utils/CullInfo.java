package net.llvg.af.utils;

@SuppressWarnings ("BooleanMethodIsAlwaysInverted")
public final class CullInfo {
    private int value = 0;
    
    private static void check(int i) {
        if (i < 0 || 6 <= i) throw new IndexOutOfBoundsException("[i] " + i + " must be in range (0, 6]");
    }
    
    public void set(
      int bit,
      boolean v
    ) {
        check(bit);
        if (v) value |= 1 << bit;
        else value &= ~(1 << bit);
    }
    
    @SuppressWarnings ("unused")
    public boolean get(int bit) {
        check(bit);
        return (value & 1 << bit) != 0;
    }
    
    public boolean getDown() {
        return (value & 0x01) == 0;
    }
    
    public boolean getUp() {
        return (value & 0x02) == 0;
    }
    
    public boolean getNorth() {
        return (value & 0x04) == 0;
    }
    
    public boolean getSouth() {
        return (value & 0x08) == 0;
    }
    
    public boolean getWest() {
        return (value & 0x10) == 0;
    }
    
    public boolean getEast() {
        return (value & 0x20) == 0;
    }
}
