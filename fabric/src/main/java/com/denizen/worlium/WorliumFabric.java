package com.denizen.worlium;

import com.denizen.worlium.worldgen.LavaLayerFeature;
import com.denizen.worlium.worldgen.WorleyDensityFunction;
import net.fabricmc.api.ModInitializer;

public class WorliumFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        WorliumCommon.init();
        WorleyDensityFunction.register();
        LavaLayerFeature.register();
    }
}
