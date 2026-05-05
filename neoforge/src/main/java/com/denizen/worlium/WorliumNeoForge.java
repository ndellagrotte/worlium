package com.denizen.worlium;

import com.denizen.worlium.worldgen.LavaLayerFeature;
import com.denizen.worlium.worldgen.WorleyDensityFunction;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(Constants.MOD_ID)
public class WorliumNeoForge {

    public WorliumNeoForge(IEventBus eventBus) {
        WorliumCommon.init();
        eventBus.addListener(RegisterEvent.class, WorliumNeoForge::onRegister);
    }

    private static void onRegister(RegisterEvent event) {
        if (Registries.DENSITY_FUNCTION_TYPE.equals(event.getRegistryKey())) {
            WorleyDensityFunction.register();
        } else if (Registries.FEATURE.equals(event.getRegistryKey())) {
            LavaLayerFeature.register();
        }
    }
}
