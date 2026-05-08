package com.denizen.worlium.mixin;

import com.denizen.worlium.util.WorldSeedHolder;
import net.minecraft.core.HolderGetter;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RandomState.class)
public class RandomStateMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void worlium$captureLevelSeed(
        NoiseGeneratorSettings settings,
        HolderGetter<NormalNoise.NoiseParameters> noises,
        long seed,
        CallbackInfo ci
    ) {
        WorldSeedHolder.SEED = seed;
        WorldSeedHolder.HAS_SEED = true;
    }
}
