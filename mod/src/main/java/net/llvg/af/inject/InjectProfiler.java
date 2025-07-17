package net.llvg.af.inject;

import net.minecraft.profiler.Profiler;
import org.jetbrains.annotations.Nullable;

public interface InjectProfiler {
    @Nullable
    String get$af$lastSectionName();
    
    @Nullable
    static String getLastSectionName(Profiler instance) {
        return ((InjectProfiler) instance).get$af$lastSectionName();
    }
}
