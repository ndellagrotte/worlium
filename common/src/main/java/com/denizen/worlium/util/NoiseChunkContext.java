package com.denizen.worlium.util;

import net.minecraft.world.level.levelgen.NoiseChunk;

public final class NoiseChunkContext {
    public static final ThreadLocal<NoiseChunk> CURRENT = new ThreadLocal<>();

    private NoiseChunkContext() {}
}
