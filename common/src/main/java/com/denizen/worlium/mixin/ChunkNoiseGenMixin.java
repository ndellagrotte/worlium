package com.denizen.worlium.mixin;

import com.denizen.worlium.util.SurfaceBufferContextHolder;
import com.denizen.worlium.worldgen.SurfaceBufferContext;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
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
    private void worlium$beginSurfaceBufferContext(
        Blender blender,
        StructureManager structureManager,
        RandomState randomState,
        ChunkAccess centerChunk,
        int cellMinY,
        int cellCountY,
        CallbackInfoReturnable<ChunkAccess> cir
    ) {
        SurfaceBufferContext ctx = SurfaceBufferContext.build(
            centerChunk.getPos(),
            randomState.router().preliminarySurfaceLevel());
        SurfaceBufferContextHolder.put(centerChunk.getPos(), ctx);
    }

    @Inject(method = "doFill", at = @At("RETURN"))
    private void worlium$endSurfaceBufferContext(
        Blender blender,
        StructureManager structureManager,
        RandomState randomState,
        ChunkAccess centerChunk,
        int cellMinY,
        int cellCountY,
        CallbackInfoReturnable<ChunkAccess> cir
    ) {
        SurfaceBufferContextHolder.remove(centerChunk.getPos());
    }
}
