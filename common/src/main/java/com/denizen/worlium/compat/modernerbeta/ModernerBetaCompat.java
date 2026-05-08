package com.denizen.worlium.compat.modernerbeta;

import com.denizen.worlium.util.AquaticBufferContextHolder;
import com.denizen.worlium.worldgen.AquaticBufferContext;
import com.denizen.worlium.worldgen.WorleyDensityFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;

public final class ModernerBetaCompat {

    public static int yBufferBlocks = 3;

    public static int surfaceHeight(Object mbChunkGen, int blockX, int blockZ,
                                    LevelHeightAccessor level, RandomState random) {
        ChunkGenerator gen = (ChunkGenerator) mbChunkGen;
        return gen.getBaseHeight(blockX, blockZ, Heightmap.Types.OCEAN_FLOOR_WG, level, random);
    }

    public static AquaticBufferContext buildYGate(Object mbChunkGen, ChunkPos pos,
                                                  LevelHeightAccessor level, RandomState random) {
        return AquaticBufferContext.buildSimpleYGate(pos,
            (x, z) -> surfaceHeight(mbChunkGen, x, z, level, random), yBufferBlocks);
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
