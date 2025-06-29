package net.llvg.af.utils;

@SuppressWarnings("unused")
public final class Clock {
    private final long delay;
    private long begin;
    
    public Clock(long delay) {
        this.delay = delay;
    }
    
    public Clock() {
        this.delay = 0;
    }
    
    public long getTimePassed() {
        return System.currentTimeMillis() - begin;
    }
    
    public void update() {
        begin = System.currentTimeMillis();
    }
    
    public boolean ended() {
        return getTimePassed() >= delay;
    }
    
    public boolean ended(boolean reset) {
        long curr;
        if ((curr = System.currentTimeMillis()) - begin >= delay) {
            if (reset) begin = curr;
            return true;
        }
        return false;
    }
    
    public boolean ended(long delay) {
        return getTimePassed() >= delay;
    }
    
    public boolean ended(
      long delay,
      boolean reset
    ) {
        long curr;
        if ((curr = System.currentTimeMillis()) - begin >= delay) {
            if (reset) begin = curr;
            return true;
        }
        return false;
    }
}
