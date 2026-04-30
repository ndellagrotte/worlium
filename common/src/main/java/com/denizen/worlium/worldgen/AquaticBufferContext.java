package com.denizen.worlium.worldgen;

import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.DensityFunction;

public final class AquaticBufferContext {

    public static final TagKey<Biome> IS_AQUATIC =
        TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("c", "is_aquatic"));

    static final int CHUNK_QUARTS = 16 >> 2;                     // 4 quarts per chunk side
    static final int NEIGHBOR_QUARTS = CHUNK_QUARTS;             // 1-chunk dilation = 4 quarts
    static final int SAMPLE_SIDE = CHUNK_QUARTS + 2 * NEIGHBOR_QUARTS;  // 12 quarts (3 chunks wide)
    static final int Y_BUFFER_BLOCKS = 8;
    private static final int NO_SURFACE_CAP = Integer.MAX_VALUE;

    private final int chunkOriginQuartX;
    private final int chunkOriginQuartZ;
    private final boolean chunkBuffered;
    private final int[] surfaceCap; // CHUNK_QUARTS² entries, indexed by chunk-local quart

    private AquaticBufferContext(int chunkOriginQuartX, int chunkOriginQuartZ,
                                 boolean chunkBuffered, int[] surfaceCap) {
        this.chunkOriginQuartX = chunkOriginQuartX;
        this.chunkOriginQuartZ = chunkOriginQuartZ;
        this.chunkBuffered = chunkBuffered;
        this.surfaceCap = surfaceCap;
    }

    public boolean shouldSuppressAt(int blockX, int blockY, int blockZ) {
        if (!chunkBuffered) return false;
        int qx = QuartPos.fromBlock(blockX) - chunkOriginQuartX;
        int qz = QuartPos.fromBlock(blockZ) - chunkOriginQuartZ;
        if (qx < 0 || qz < 0 || qx >= CHUNK_QUARTS || qz >= CHUNK_QUARTS) return false;
        int cap = surfaceCap[qz * CHUNK_QUARTS + qx];
        return cap != NO_SURFACE_CAP && blockY >= cap;
    }

    public static AquaticBufferContext build(ChunkPos chunkPos, BiomeResolver resolver,
                                             Climate.Sampler sampler,
                                             DensityFunction preliminarySurfaceLevel) {
        int chunkOriginQuartX = QuartPos.fromBlock(chunkPos.getMinBlockX());
        int chunkOriginQuartZ = QuartPos.fromBlock(chunkPos.getMinBlockZ());

        // Sample 3×3 chunks centred on this chunk; origin = chunk - 1 chunk in each direction.
        int sampleOriginQuartX = chunkOriginQuartX - NEIGHBOR_QUARTS;
        int sampleOriginQuartZ = chunkOriginQuartZ - NEIGHBOR_QUARTS;
        int sampleY = QuartPos.fromBlock(64); // arbitrary; aquatic biomes are vertical columns

        boolean anyAquatic = false;
        outer:
        for (int qz = 0; qz < SAMPLE_SIDE; qz++) {
            for (int qx = 0; qx < SAMPLE_SIDE; qx++) {
                Holder<Biome> biome = resolver.getNoiseBiome(
                    sampleOriginQuartX + qx, sampleY, sampleOriginQuartZ + qz, sampler);
                if (biome.is(IS_AQUATIC)) {
                    anyAquatic = true;
                    break outer;
                }
            }
        }

        int[] caps = new int[CHUNK_QUARTS * CHUNK_QUARTS];
        if (!anyAquatic) {
            java.util.Arrays.fill(caps, NO_SURFACE_CAP);
            return new AquaticBufferContext(chunkOriginQuartX, chunkOriginQuartZ, false, caps);
        }

        // Per-cell surface caps for the chunk's 4×4 quart grid.
        for (int mz = 0; mz < CHUNK_QUARTS; mz++) {
            for (int mx = 0; mx < CHUNK_QUARTS; mx++) {
                int blockX = QuartPos.toBlock(chunkOriginQuartX + mx) + 2;
                int blockZ = QuartPos.toBlock(chunkOriginQuartZ + mz) + 2;
                int surfaceY = (int) Math.round(
                    preliminarySurfaceLevel.compute(
                        new DensityFunction.SinglePointContext(blockX, 0, blockZ)));
                caps[mz * CHUNK_QUARTS + mx] = surfaceY - Y_BUFFER_BLOCKS;
            }
        }

        return new AquaticBufferContext(chunkOriginQuartX, chunkOriginQuartZ, true, caps);
    }
}
