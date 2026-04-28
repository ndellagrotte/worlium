package com.denizen.worlium.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

import java.util.concurrent.ConcurrentHashMap;

public final class AquaticChunkGate {

    private static final TagKey<Biome> IS_AQUATIC =
        TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("c", "is_aquatic"));

    private static final ConcurrentHashMap<Long, Boolean> CACHE = new ConcurrentHashMap<>();
    private static volatile Climate.Sampler OVERWORLD_SAMPLER;

    private AquaticChunkGate() {}

    public static void setSampler(Climate.Sampler sampler) {
        OVERWORLD_SAMPLER = sampler;
        CACHE.clear();
    }

    public static boolean isAquatic(int blockX, int blockZ) {
        BiomeSource bs = BiomeSourceHolder.OVERWORLD;
        Climate.Sampler sampler = OVERWORLD_SAMPLER;
        if (bs == null || sampler == null) return false;

        int cx = blockX >> 4;
        int cz = blockZ >> 4;
        // Gate fires for the chunk itself or any of its 8 neighbors so that
        // shoreline columns near a river/ocean edge still get capped — biome
        // boundaries don't always align cleanly with chunk edges.
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (chunkIsAquatic(cx + dx, cz + dz, bs, sampler)) return true;
            }
        }
        return false;
    }

    private static boolean chunkIsAquatic(int cx, int cz, BiomeSource bs, Climate.Sampler sampler) {
        long key = ChunkPos.pack(cx, cz);
        Boolean cached = CACHE.get(key);
        if (cached != null) return cached;

        int qxStart = cx << 2;
        int qzStart = cz << 2;
        // Sea level is Y=63; sample at quart-Y for Y=64 so the lookup is in the surface
        // biome zone (above the water surface) rather than at or below it.
        int qy = 64 >> 2;
        boolean aquatic = false;
        for (int dx = 0; dx < 4 && !aquatic; dx++) {
            for (int dz = 0; dz < 4 && !aquatic; dz++) {
                if (bs.getNoiseBiome(qxStart + dx, qy, qzStart + dz, sampler).is(IS_AQUATIC)) {
                    aquatic = true;
                }
            }
        }
        CACHE.put(key, aquatic);
        return aquatic;
    }
}
