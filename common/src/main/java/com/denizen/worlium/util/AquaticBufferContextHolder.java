package com.denizen.worlium.util;

import com.denizen.worlium.worldgen.AquaticBufferContext;

public final class AquaticBufferContextHolder {

    public static final ThreadLocal<AquaticBufferContext> CURRENT = new ThreadLocal<>();

    private AquaticBufferContextHolder() {}
}
