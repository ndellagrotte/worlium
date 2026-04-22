package com.denizen.worlium.worldgen.aquifer;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseChunk;
import org.jspecify.annotations.Nullable;

public final class AirOnlyAquifer implements Aquifer {
    private static final int SURFACE_MARGIN = 2;

    private final NoiseChunk noiseChunk;
    private final Aquifer.FluidPicker fluidPicker;

    public AirOnlyAquifer(NoiseChunk noiseChunk, Aquifer.FluidPicker fluidPicker) {
        this.noiseChunk = noiseChunk;
        this.fluidPicker = fluidPicker;
    }

    @Override
    public @Nullable BlockState computeSubstance(DensityFunction.FunctionContext ctx, double density) {
        if (density > 0.0) return null;
        int x = ctx.blockX();
        int y = ctx.blockY();
        int z = ctx.blockZ();
        int surface = this.noiseChunk.preliminarySurfaceLevel(x, z);
        if (y < surface - SURFACE_MARGIN) {
            return Blocks.AIR.defaultBlockState();
        }
        return this.fluidPicker.computeFluid(x, y, z).at(y);
    }

    @Override
    public boolean shouldScheduleFluidUpdate() {
        return false;
    }
}
