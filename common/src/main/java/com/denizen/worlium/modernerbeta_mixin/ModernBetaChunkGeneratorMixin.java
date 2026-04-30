package com.denizen.worlium.modernerbeta_mixin;

import com.denizen.worlium.compat.modernerbeta.ModernerBetaCompat;
import com.denizen.worlium.util.AquaticBufferContextHolder;
import com.denizen.worlium.worldgen.AquaticBufferContext;
import net.minecraft.core.Holder;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(targets = "mod.bluestaggo.modernerbeta.level.chunk.ModernBetaChunkGenerator", remap = false)
public abstract class ModernBetaChunkGeneratorMixin {

    @Inject(method = "fillFromNoise", at = @At("HEAD"), remap = true)
    private void worlium$buildYGate(
        Blender blender,
        RandomState noiseConfig,
        StructureManager structureAccessor,
        ChunkAccess chunk,
        CallbackInfoReturnable<CompletableFuture<ChunkAccess>> cir
    ) {
        AquaticBufferContext ctx = ModernerBetaCompat.buildYGate(this, chunk.getPos());
        AquaticBufferContextHolder.put(chunk.getPos(), ctx);
    }

    @Inject(method = "fillFromNoise", at = @At("RETURN"), cancellable = true, remap = true)
    private void worlium$applyWorleyAndCleanup(
        Blender blender,
        RandomState noiseConfig,
        StructureManager structureAccessor,
        ChunkAccess chunk,
        CallbackInfoReturnable<CompletableFuture<ChunkAccess>> cir
    ) {
        CompletableFuture<ChunkAccess> orig = cir.getReturnValue();
        cir.setReturnValue(orig.thenApply(c -> {
            try {
                ModernerBetaCompat.applyWorleyCarve(c);
            } finally {
                AquaticBufferContextHolder.remove(c.getPos());
            }
            return c;
        }));
    }

    @Redirect(
        method = "applyCarvers",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/biome/BiomeGenerationSettings;getCarvers()Ljava/lang/Iterable;",
            remap = true
        ),
        remap = true
    )
    private Iterable<Holder<ConfiguredWorldCarver<?>>> worlium$filterCaveCarvers(BiomeGenerationSettings genSettings) {
        return ModernerBetaCompat.filterCaveCarvers(genSettings.getCarvers());
    }
}
