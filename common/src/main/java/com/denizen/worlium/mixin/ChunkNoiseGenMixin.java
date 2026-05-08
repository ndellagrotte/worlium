package com.denizen.worlium.mixin;

import com.denizen.worlium.util.AquaticBufferContextHolder;
import com.denizen.worlium.worldgen.AquaticBufferContext;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NoiseBasedChunkGenerator.class)
public abstract class ChunkNoiseGenMixin {

    @Inject(method = "doFill", at = @At("HEAD"))
    private void worlium$beginAquaticBufferContext(
        Blender blender,
        StructureManager structureManager,
        RandomState randomState,
        ChunkAccess centerChunk,
        int cellMinY,
        int cellCountY,
        CallbackInfoReturnable<ChunkAccess> cir
    ) {
        ChunkGenerator self = (ChunkGenerator) (Object) this;
        AquaticBufferContext ctx = AquaticBufferContext.build(
            centerChunk.getPos(),
            self.getBiomeSource(),
            randomState.sampler(),
            randomState.router().preliminarySurfaceLevel());
        AquaticBufferContextHolder.put(centerChunk.getPos(), ctx);
    }

    @Inject(method = "doFill", at = @At("RETURN"))
    private void worlium$endAquaticBufferContext(
        Blender blender,
        StructureManager structureManager,
        RandomState randomState,
        ChunkAccess centerChunk,
        int cellMinY,
        int cellCountY,
        CallbackInfoReturnable<ChunkAccess> cir
    ) {
        AquaticBufferContextHolder.remove(centerChunk.getPos());
    }
}
