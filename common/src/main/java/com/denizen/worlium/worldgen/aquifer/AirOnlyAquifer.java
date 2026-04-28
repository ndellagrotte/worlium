package com.denizen.worlium.worldgen.aquifer;

import com.denizen.worlium.util.NoiseChunkContext;
import com.denizen.worlium.worldgen.WorleyDensityFunction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseChunk;
import org.jspecify.annotations.Nullable;

public final class AirOnlyAquifer implements Aquifer {
    private static final int LAVA_DEPTH = -56;
    private static final int SEA_LEVEL = 63;
    // Below preliminary_surface_level - this margin, treat density<0 as a deep carve
    // (beardifier-cleared structure footprint, etc.) and place AIR instead of letting
    // the fluid picker default to water below sea level.
    private static final int SURFACE_MARGIN = 2;

    private final Aquifer.FluidPicker fluidPicker;

    public AirOnlyAquifer(Aquifer.FluidPicker fluidPicker) {
        this.fluidPicker = fluidPicker;
    }

    @Override
    public @Nullable BlockState computeSubstance(DensityFunction.FunctionContext ctx, double density) {
        if (density > 0.0) return null;
        int x = ctx.blockX();
        int y = ctx.blockY();
        int z = ctx.blockZ();

        double worley = WorleyDensityFunction.INSTANCE.compute(ctx);
        if (worley < 0.0) {
            return y < LAVA_DEPTH
                ? Blocks.LAVA.defaultBlockState()
                : Blocks.AIR.defaultBlockState();
        }

        NoiseChunk nc = NoiseChunkContext.CURRENT.get();
        int surface = (nc != null) ? nc.preliminarySurfaceLevel(x, z) : SEA_LEVEL;
        if (y < surface - SURFACE_MARGIN) {
            return y < LAVA_DEPTH
                ? Blocks.LAVA.defaultBlockState()
                : Blocks.AIR.defaultBlockState();
        }
        return this.fluidPicker.computeFluid(x, y, z).at(y);
    }

    @Override
    public boolean shouldScheduleFluidUpdate() {
        return false;
    }
}
