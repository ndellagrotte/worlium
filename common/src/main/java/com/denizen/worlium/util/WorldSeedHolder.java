package com.denizen.worlium.util;

public final class WorldSeedHolder {

    public static volatile long SEED;
    public static volatile boolean HAS_SEED;

    public static final ThreadLocal<Boolean> LOADING_OVERWORLD_NGS =
        ThreadLocal.withInitial(() -> Boolean.FALSE);

    private WorldSeedHolder() {}
}
