package com.denizen.worlium.mixin;

import com.denizen.worlium.worldgen.WorleyDensityFunction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.world.level.levelgen.Aquifer$NoiseBasedAquifer")
public abstract class NoiseBasedAquiferMixin {

    @Inject(method = "computeSubstance", at = @At("HEAD"), cancellable = true)
    private void worlium$forceAirInWorleyCaves(
        DensityFunction.FunctionContext context,
        double substance,
        CallbackInfoReturnable<BlockState> cir
    ) {
        if (substance > 0.0) return;
        if (WorleyDensityFunction.INSTANCE.compute(context) < 0.0) {
            cir.setReturnValue(Blocks.AIR.defaultBlockState());
        }
    }
}
