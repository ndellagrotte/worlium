package com.denizen.worlium.modernerbeta_mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "mod.bluestaggo.modernerbeta.api.level.chunk.ChunkProvider", remap = false)
public abstract class ChunkProviderMixin {

    @ModifyExpressionValue(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lmod/bluestaggo/modernerbeta/settings/component/CaveGeneration;useCarvers()Z"
        )
    )
    private boolean worlium$forceNoUseCarvers(boolean original) {
        return false;
    }
}
