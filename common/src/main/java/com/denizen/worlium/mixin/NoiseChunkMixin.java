package com.denizen.worlium.mixin;

import com.denizen.worlium.worldgen.aquifer.AirOnlyAquifer;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.RandomState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NoiseChunk.class)
public abstract class NoiseChunkMixin {

    @Shadow @Final @Mutable
    private Aquifer aquifer;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void worlium$replaceAquifer(
        int cellCountXZ,
        RandomState randomState,
        int chunkMinBlockX,
        int chunkMinBlockZ,
        NoiseSettings noiseSettings,
        DensityFunctions.BeardifierOrMarker beardifier,
        NoiseGeneratorSettings settings,
        Aquifer.FluidPicker globalFluidPicker,
        Blender blender,
        CallbackInfo ci
    ) {
        if (!(((Object) settings) instanceof WorliumModifiedNgs marker) || !marker.worlium$isModifiedOverworld()) return;
        this.aquifer = new AirOnlyAquifer((NoiseChunk) (Object) this, globalFluidPicker);
    }
}
