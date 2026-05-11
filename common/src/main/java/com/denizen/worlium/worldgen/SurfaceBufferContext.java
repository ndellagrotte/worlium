package com.denizen.worlium.worldgen;

import net.minecraft.core.QuartPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.DensityFunction;

import java.util.function.IntBinaryOperator;

public final class SurfaceBufferContext {

    static final int CHUNK_QUARTS = 16 >> 2;                     // 4 quarts per chunk side
    // TODO(buffer-depth): re-derive from vanilla cave_entrance reach (y range, vertical_rotation, horizontal_radius).
    static final int Y_BUFFER_BLOCKS = 8;
    private static final int NO_SURFACE_CAP = Integer.MAX_VALUE;

    // Sane clamp for surface-Y readings; guards against pathological density-function output.
    private static final int SURFACE_Y_MIN = WorleyDensityFunction.MIN_CAVE_HEIGHT;
    private static final int SURFACE_Y_MAX = WorleyDensityFunction.MAX_CAVE_HEIGHT + 32;

    private final int chunkOriginQuartX;
    private final int chunkOriginQuartZ;
    private final int[] surfaceCap; // CHUNK_QUARTS² entries, indexed by chunk-local quart

    private SurfaceBufferContext(int chunkOriginQuartX, int chunkOriginQuartZ, int[] surfaceCap) {
        this.chunkOriginQuartX = chunkOriginQuartX;
        this.chunkOriginQuartZ = chunkOriginQuartZ;
        this.surfaceCap = surfaceCap;
    }

    public boolean shouldSuppressAt(int blockX, int blockY, int blockZ) {
        int qx = QuartPos.fromBlock(blockX) - chunkOriginQuartX;
        int qz = QuartPos.fromBlock(blockZ) - chunkOriginQuartZ;
        if (qx < 0 || qz < 0 || qx >= CHUNK_QUARTS || qz >= CHUNK_QUARTS) return false;
        int cap = surfaceCap[qz * CHUNK_QUARTS + qx];
        return cap != NO_SURFACE_CAP && blockY >= cap;
    }

    public static SurfaceBufferContext build(ChunkPos chunkPos, DensityFunction preliminarySurfaceLevel) {
        return build(chunkPos, preliminarySurfaceLevel, Y_BUFFER_BLOCKS);
    }

    public static SurfaceBufferContext build(ChunkPos chunkPos, DensityFunction preliminarySurfaceLevel, int yBufferBlocks) {
        return buildFromHeightLookup(chunkPos, (x, z) -> {
            int surfaceY = (int) Math.round(
                preliminarySurfaceLevel.compute(new DensityFunction.SinglePointContext(x, 0, z)));
            return Math.max(SURFACE_Y_MIN, Math.min(SURFACE_Y_MAX, surfaceY));
        }, yBufferBlocks);
    }

    public static SurfaceBufferContext buildFromHeightLookup(ChunkPos chunkPos, IntBinaryOperator surfaceLevelAt) {
        return buildFromHeightLookup(chunkPos, surfaceLevelAt, Y_BUFFER_BLOCKS);
    }

    public static SurfaceBufferContext buildFromHeightLookup(ChunkPos chunkPos, IntBinaryOperator surfaceLevelAt, int yBufferBlocks) {
        int chunkOriginQuartX = QuartPos.fromBlock(chunkPos.getMinBlockX());
        int chunkOriginQuartZ = QuartPos.fromBlock(chunkPos.getMinBlockZ());
        int[] caps = new int[CHUNK_QUARTS * CHUNK_QUARTS];
        for (int mz = 0; mz < CHUNK_QUARTS; mz++) {
            for (int mx = 0; mx < CHUNK_QUARTS; mx++) {
                int blockX = QuartPos.toBlock(chunkOriginQuartX + mx) + 2;
                int blockZ = QuartPos.toBlock(chunkOriginQuartZ + mz) + 2;
                caps[mz * CHUNK_QUARTS + mx] = surfaceLevelAt.applyAsInt(blockX, blockZ) - yBufferBlocks;
            }
        }
        return new SurfaceBufferContext(chunkOriginQuartX, chunkOriginQuartZ, caps);
    }
}
