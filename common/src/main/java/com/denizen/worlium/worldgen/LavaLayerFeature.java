package com.denizen.worlium.worldgen;

import com.denizen.worlium.Constants;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class LavaLayerFeature extends Feature<NoneFeatureConfiguration> {

    public static final Identifier ID =
        Identifier.fromNamespaceAndPath(Constants.MOD_ID, "lava_layer");

    public static final LavaLayerFeature INSTANCE =
        new LavaLayerFeature(NoneFeatureConfiguration.CODEC);

    private static final int MIN_Y = -63;
    private static final int MAX_Y = -56;

    public LavaLayerFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    public static void register() {
        Registry.register(BuiltInRegistries.FEATURE, ID, INSTANCE);
        Constants.LOG.info("Registered feature {}", ID);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        int minX = origin.getX() & ~15;
        int minZ = origin.getZ() & ~15;
        BlockState lava = Blocks.LAVA.defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        boolean placedAny = false;
        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                for (int y = MIN_Y; y <= MAX_Y; y++) {
                    pos.set(minX + dx, y, minZ + dz);
                    if (level.getBlockState(pos).isAir()) {
                        setBlock(level, pos, lava);
                        placedAny = true;
                    }
                }
            }
        }
        return placedAny;
    }
}
