package net.llvg.af.utils;

public final class Dummy {
    @SuppressWarnings ("InstantiationOfUtilityClass")
    public static final Dummy instance = new Dummy();
    
    private Dummy() {
        StackTraceElement trace = new Throwable().getStackTrace()[1];
        if (
          !Dummy.class.getName().equals(trace.getClassName()) ||
          !"<clinit>".equals(trace.getMethodName())
        ) throw new UnsupportedOperationException();
    }
}