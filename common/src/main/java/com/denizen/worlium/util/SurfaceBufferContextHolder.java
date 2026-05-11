package com.denizen.worlium.util;

import com.denizen.worlium.worldgen.SurfaceBufferContext;
import net.minecraft.world.level.ChunkPos;

import java.util.concurrent.ConcurrentHashMap;

public final class SurfaceBufferContextHolder {

    private static final ConcurrentHashMap<ChunkPos, SurfaceBufferContext> CONTEXTS = new ConcurrentHashMap<>();

    public static void put(ChunkPos pos, SurfaceBufferContext ctx) {
        CONTEXTS.put(pos, ctx);
    }

    public static void remove(ChunkPos pos) {
        CONTEXTS.remove(pos);
    }

    public static SurfaceBufferContext getForBlock(int blockX, int blockZ) {
        return CONTEXTS.get(new ChunkPos(blockX >> 4, blockZ >> 4));
    }

    public static SurfaceBufferContext getForChunk(ChunkPos pos) {
        return CONTEXTS.get(pos);
    }

    private SurfaceBufferContextHolder() {}
}
