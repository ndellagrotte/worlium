package com.denizen.worlium.mixin;

import com.denizen.worlium.util.WorldSeedHolder;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Decoder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.resources.RegistryLoadTask$PendingRegistration")
public class PendingRegistrationMixin {

    @Inject(method = "loadFromResource", at = @At("HEAD"))
    private static <T> void worlium$markOverworldNoiseSettings(
        Decoder<T> decoder,
        RegistryOps<JsonElement> ops,
        ResourceKey<T> resourceKey,
        Resource resource,
        CallbackInfoReturnable<Either<T, Exception>> cir
    ) {
        if (resourceKey.isFor(Registries.NOISE_SETTINGS)
                && resourceKey.identifier().equals(NoiseGeneratorSettings.OVERWORLD.identifier())) {
            WorldSeedHolder.LOADING_OVERWORLD_NGS.set(Boolean.TRUE);
        }
    }
}
