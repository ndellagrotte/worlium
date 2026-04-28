package com.denizen.worlium.mixin;

import com.denizen.worlium.util.BiomeSourceHolder;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NoiseBasedChunkGenerator.class)
public abstract class NoiseBasedChunkGeneratorMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void worlium$captureBiomeSource(
        BiomeSource biomeSource,
        Holder<NoiseGeneratorSettings> settings,
        CallbackInfo ci
    ) {
        if (settings.is(NoiseGeneratorSettings.OVERWORLD) && biomeSource instanceof MultiNoiseBiomeSource) {
            BiomeSourceHolder.OVERWORLD = biomeSource;
        }
    }
}
