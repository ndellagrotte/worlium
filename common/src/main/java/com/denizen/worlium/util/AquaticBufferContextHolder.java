package com.denizen.worlium.util;

import com.denizen.worlium.worldgen.AquaticBufferContext;
import net.minecraft.world.level.ChunkPos;

import java.util.concurrent.ConcurrentHashMap;

public final class AquaticBufferContextHolder {

    private static final ConcurrentHashMap<ChunkPos, AquaticBufferContext> CONTEXTS = new ConcurrentHashMap<>();

    public static void put(ChunkPos pos, AquaticBufferContext ctx) {
        CONTEXTS.put(pos, ctx);
    }

    public static void remove(ChunkPos pos) {
        CONTEXTS.remove(pos);
    }

    public static AquaticBufferContext getForBlock(int blockX, int blockZ) {
        return CONTEXTS.get(new ChunkPos(blockX >> 4, blockZ >> 4));
    }

    public static AquaticBufferContext getForChunk(ChunkPos pos) {
        return CONTEXTS.get(pos);
    }

    private AquaticBufferContextHolder() {}
}
