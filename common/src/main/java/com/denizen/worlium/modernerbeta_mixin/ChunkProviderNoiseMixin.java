package com.denizen.worlium.modernerbeta_mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "mod.bluestaggo.modernerbeta.api.level.chunk.ChunkProviderNoise", remap = false)
public abstract class ChunkProviderNoiseMixin {

    @ModifyExpressionValue(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lmod/bluestaggo/modernerbeta/settings/component/CaveGeneration;useNoiseCaves()Z"
        )
    )
    private boolean worlium$forceNoNoiseCaves(boolean original) {
        return false;
    }
}
