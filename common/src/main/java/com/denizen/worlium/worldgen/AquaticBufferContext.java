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

public final class AquaticBufferContext {

    public static final TagKey<Biome> IS_AQUATIC =
        TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("c", "is_aquatic"));

    static final int BUFFER_BLOCKS = 8;
    static final int BUFFER_QUARTS = (BUFFER_BLOCKS + 3) >> 2;       // 2
    static final int CHUNK_QUARTS = 16 >> 2;                         // 4
    static final int MASK_SIDE = CHUNK_QUARTS + 2 * BUFFER_QUARTS;   // 8

    private final int maskOriginQuartX;
    private final int maskOriginQuartZ;
    private final boolean[] withinBuffer;

    private AquaticBufferContext(int maskOriginQuartX, int maskOriginQuartZ, boolean[] withinBuffer) {
        this.maskOriginQuartX = maskOriginQuartX;
        this.maskOriginQuartZ = maskOriginQuartZ;
        this.withinBuffer = withinBuffer;
    }

    public boolean isWithinBufferAt(int blockX, int blockZ) {
        int qx = QuartPos.fromBlock(blockX) - maskOriginQuartX;
        int qz = QuartPos.fromBlock(blockZ) - maskOriginQuartZ;
        if (qx < 0 || qz < 0 || qx >= MASK_SIDE || qz >= MASK_SIDE) return false;
        return withinBuffer[qz * MASK_SIDE + qx];
    }

    public static AquaticBufferContext build(ChunkPos chunkPos, BiomeResolver resolver, Climate.Sampler sampler) {
        int chunkOriginQuartX = QuartPos.fromBlock(chunkPos.getMinBlockX());
        int chunkOriginQuartZ = QuartPos.fromBlock(chunkPos.getMinBlockZ());

        int maskOriginQuartX = chunkOriginQuartX - BUFFER_QUARTS;
        int maskOriginQuartZ = chunkOriginQuartZ - BUFFER_QUARTS;

        // Sample on a wider grid that includes a BUFFER_QUARTS ring around the mask, so the
        // dilation step below has neighbour data to look at on every side of every mask cell.
        int sampleSide = MASK_SIDE + 2 * BUFFER_QUARTS;
        int sampleOriginQuartX = maskOriginQuartX - BUFFER_QUARTS;
        int sampleOriginQuartZ = maskOriginQuartZ - BUFFER_QUARTS;

        boolean[] aquaticSample = new boolean[sampleSide * sampleSide];
        boolean anyAquatic = false;
        int sampleY = QuartPos.fromBlock(64); // arbitrary; biome lookup is 3D but aquatic biomes are vertical columns

        for (int qz = 0; qz < sampleSide; qz++) {
            for (int qx = 0; qx < sampleSide; qx++) {
                Holder<Biome> biome = resolver.getNoiseBiome(
                    sampleOriginQuartX + qx, sampleY, sampleOriginQuartZ + qz, sampler);
                boolean aquatic = biome.is(IS_AQUATIC);
                aquaticSample[qz * sampleSide + qx] = aquatic;
                anyAquatic |= aquatic;
            }
        }

        boolean[] mask = new boolean[MASK_SIDE * MASK_SIDE];
        if (!anyAquatic) {
            return new AquaticBufferContext(maskOriginQuartX, maskOriginQuartZ, mask);
        }

        // Chebyshev dilation: a mask cell is buffered if any sample within BUFFER_QUARTS is aquatic.
        for (int mz = 0; mz < MASK_SIDE; mz++) {
            for (int mx = 0; mx < MASK_SIDE; mx++) {
                // Sample-grid coords of the corresponding mask cell.
                int sx = mx + BUFFER_QUARTS;
                int sz = mz + BUFFER_QUARTS;
                boolean buffered = false;
                outer:
                for (int dz = -BUFFER_QUARTS; dz <= BUFFER_QUARTS; dz++) {
                    int rz = sz + dz;
                    int rowOffset = rz * sampleSide;
                    for (int dx = -BUFFER_QUARTS; dx <= BUFFER_QUARTS; dx++) {
                        if (aquaticSample[rowOffset + sx + dx]) {
                            buffered = true;
                            break outer;
                        }
                    }
                }
                mask[mz * MASK_SIDE + mx] = buffered;
            }
        }

        return new AquaticBufferContext(maskOriginQuartX, maskOriginQuartZ, mask);
    }
}
