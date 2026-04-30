package com.denizen.worlium.mixin;

import com.denizen.worlium.util.WorldSeedHolder;
import com.denizen.worlium.worldgen.WorleyDensityFunction;
import com.denizen.worlium.worldgen.aquifer.WorliumModifiedNgs;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NoiseGeneratorSettings.class)
public abstract class NoiseGeneratorSettingsMixin implements WorliumModifiedNgs {

    @Shadow @Final @Mutable
    private NoiseRouter noiseRouter;

    @Unique
    private boolean worlium$modifiedOverworld = false;

    @Override
    public boolean worlium$isModifiedOverworld() {
        return this.worlium$modifiedOverworld;
    }

    @Override
    public void worlium$markModifiedOverworld() {
        this.worlium$modifiedOverworld = true;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void worlium$wrapFinalDensity(CallbackInfo ci) {
        if (!Boolean.TRUE.equals(WorldSeedHolder.LOADING_OVERWORLD_NGS.get())) return;
        WorldSeedHolder.LOADING_OVERWORLD_NGS.remove();

        NoiseRouter old = this.noiseRouter;
        WorleyDensityFunction.INSTANCE.setOceanGate(old.continents());
        this.noiseRouter = new NoiseRouter(
            old.barrierNoise(),
            old.fluidLevelFloodednessNoise(),
            old.fluidLevelSpreadNoise(),
            old.lavaNoise(),
            old.temperature(),
            old.vegetation(),
            old.continents(),
            old.erosion(),
            old.depth(),
            old.ridges(),
            old.preliminarySurfaceLevel(),
            DensityFunctions.min(old.finalDensity(), WorleyDensityFunction.INSTANCE),
            old.veinToggle(),
            old.veinRidged(),
            old.veinGap()
        );
        this.worlium$markModifiedOverworld();
    }
}
