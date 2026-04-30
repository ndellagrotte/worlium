package com.denizen.worlium.compat.modernerbeta;

import com.denizen.worlium.util.AquaticBufferContextHolder;
import com.denizen.worlium.worldgen.AquaticBufferContext;
import com.denizen.worlium.worldgen.WorleyDensityFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.Carvers;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public final class ModernerBetaCompat {

    public static int yBufferBlocks = 3;

    private static final ResourceKey<ConfiguredWorldCarver<?>> MB_BETA_CAVE =
        ResourceKey.create(Registries.CONFIGURED_CARVER,
            Identifier.fromNamespaceAndPath("modernerbeta", "beta_cave"));
    private static final ResourceKey<ConfiguredWorldCarver<?>> MB_BETA_CAVE_DEEP =
        ResourceKey.create(Registries.CONFIGURED_CARVER,
            Identifier.fromNamespaceAndPath("modernerbeta", "beta_cave_deep"));

    private static final Set<ResourceKey<ConfiguredWorldCarver<?>>> CAVE_CARVER_KEYS = Set.of(
        Carvers.CAVE,
        Carvers.CAVE_EXTRA_UNDERGROUND,
        MB_BETA_CAVE,
        MB_BETA_CAVE_DEEP
    );

    public static int surfaceHeight(Object mbChunkGen, int blockX, int blockZ) {
        ChunkGenerator gen = (ChunkGenerator) mbChunkGen;
        return gen.getBaseHeight(blockX, blockZ, Heightmap.Types.OCEAN_FLOOR_WG, null, null);
    }

    public static AquaticBufferContext buildYGate(Object mbChunkGen, ChunkPos pos) {
        return AquaticBufferContext.buildSimpleYGate(pos, (x, z) -> surfaceHeight(mbChunkGen, x, z), yBufferBlocks);
    }

    public static Iterable<Holder<ConfiguredWorldCarver<?>>> filterCaveCarvers(
        Iterable<Holder<ConfiguredWorldCarver<?>>> source
    ) {
        List<Holder<ConfiguredWorldCarver<?>>> kept = new ArrayList<>();
        for (Iterator<Holder<ConfiguredWorldCarver<?>>> it = source.iterator(); it.hasNext(); ) {
            Holder<ConfiguredWorldCarver<?>> h = it.next();
            ResourceKey<ConfiguredWorldCarver<?>> k = h.unwrapKey().orElse(null);
            if (k == null || !CAVE_CARVER_KEYS.contains(k)) {
                kept.add(h);
            }
        }
        return kept;
    }

    public static void applyWorleyCarve(ChunkAccess chunk) {
        ChunkPos pos = chunk.getPos();
        AquaticBufferContext ctx = AquaticBufferContextHolder.getForChunk(pos);
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        BlockState air = Blocks.CAVE_AIR.defaultBlockState();
        int minBlockX = pos.getMinBlockX();
        int minBlockZ = pos.getMinBlockZ();
        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                int x = minBlockX + dx;
                int z = minBlockZ + dz;
                for (int y = WorleyDensityFunction.MAX_CAVE_HEIGHT;
                     y >= WorleyDensityFunction.MIN_CAVE_HEIGHT; y--) {
                    if (ctx != null && ctx.shouldSuppressAt(x, y, z)) continue;
                    if (WorleyDensityFunction.INSTANCE.compute(
                            new DensityFunction.SinglePointContext(x, y, z)) >= 0.0) continue;
                    m.set(x, y, z);
                    BlockState existing = chunk.getBlockState(m);
                    if (existing.isAir()) continue;
                    if (!existing.getFluidState().isEmpty()) continue;
                    chunk.setBlockState(m, air);
                }
            }
        }
    }

    private ModernerBetaCompat() {}
}
