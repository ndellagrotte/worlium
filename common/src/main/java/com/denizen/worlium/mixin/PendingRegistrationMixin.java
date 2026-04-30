package com.denizen.worlium.mixin;

import com.denizen.worlium.util.WorldSeedHolder;
import com.mojang.serialization.Decoder;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RegistryDataLoader.class)
public class PendingRegistrationMixin {

    @Inject(method = "loadElementFromResource", at = @At("HEAD"))
    private static <E> void worlium$markOverworldNoiseSettings(
        WritableRegistry<E> registry,
        Decoder<E> decoder,
        RegistryOps<?> ops,
        ResourceKey<E> resourceKey,
        Resource resource,
        RegistrationInfo registrationInfo,
        CallbackInfo ci
    ) {
        if (resourceKey.isFor(Registries.NOISE_SETTINGS)
                && resourceKey.identifier().equals(NoiseGeneratorSettings.OVERWORLD.identifier())) {
            WorldSeedHolder.LOADING_OVERWORLD_NGS.set(Boolean.TRUE);
        }
    }
}
