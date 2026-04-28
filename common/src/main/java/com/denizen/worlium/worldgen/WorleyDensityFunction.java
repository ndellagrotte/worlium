package com.denizen.worlium.worldgen;

import com.denizen.worlium.Constants;
import com.denizen.worlium.util.FastNoiseLite;
import com.denizen.worlium.util.NoiseChunkContext;
import com.denizen.worlium.util.WorldSeedHolder;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseChunk;

public final class WorleyDensityFunction implements DensityFunction.SimpleFunction {

    public static final WorleyDensityFunction INSTANCE = new WorleyDensityFunction();

    public static final Identifier ID =
        Identifier.fromNamespaceAndPath(Constants.MOD_ID, "worley_caves");

    public static final MapCodec<WorleyDensityFunction> CODEC = MapCodec.unit(INSTANCE);
    public static final KeyDispatchDataCodec<WorleyDensityFunction> KD_CODEC = KeyDispatchDataCodec.of(CODEC);

    public static void register() {
        Registry.register(BuiltInRegistries.DENSITY_FUNCTION_TYPE, ID, CODEC);
        Constants.LOG.info("Registered density function type {}", ID);
    }

    // Verbatim from WorleyCaves ServerConfig defaults (1.16.5).
    private static final float CELLULAR_FREQUENCY = 0.016f;
    private static final float WARP_FREQUENCY = 0.05f;
    private static final float WARP_AMPLIFIER = 8.0f;
    private static final float Y_COMPRESSION = 2.0f;
    private static final double NOISE_CUTOFF = -0.18;
    private static final double SURFACE_CUTOFF = -0.081;
    private static final int EASE_IN_DEPTH = 15;
    private static final int MIN_CAVE_HEIGHT = -64;
    private static final int MAX_CAVE_HEIGHT = 128;
    // Buffer below preliminary_surface_level — absorbs find_top_surface cell_height=8 coarseness
    // so caves can't breach into ocean/river surfaces.
    private static final int SURFACE_HARD_STOP_MARGIN = 8;
    // Surface-water detection. The surface gate is active only on columns where water is
    // present above the surface; pure land columns carve to the surface naturally
    // (original WorleyCaves behavior — entrances on hillsides/mountains).
    private static final int SEA_LEVEL = 63;
    private static final int RIVER_SURFACE_BUFFER = 1;
    // Sample preliminary_surface_level at the center and at ±RIVER_DETECT_RADIUS in each
    // cardinal direction; if any neighbor is at/below sea level, this column is "near water".
    // Five blocks matches the empirical accuracy of the river biome boundary.
    private static final int RIVER_DETECT_RADIUS = 5;
    // Vanilla "near_inland" continentalness boundary — values below this are ocean-like.
    private static final double OCEAN_CONTINENTALNESS_THRESHOLD = -0.11;

    private static final double SOLID = 64.0;
    private static final double AIR = -64.0;

    private volatile WorleyNoise worley;
    private volatile FastNoiseLite warp;
    private volatile DensityFunction continents;

    private WorleyDensityFunction() {}

    public void setOceanGate(DensityFunction continents) {
        this.continents = continents;
    }

    private void ensureSeeded() {
        if (worley != null) return;
        synchronized (this) {
            if (worley != null) return;
            int seed = WorldSeedHolder.HAS_SEED ? (int) WorldSeedHolder.SEED : 0;
            WorleyNoise w = new WorleyNoise(seed);
            w.setFrequency(CELLULAR_FREQUENCY);
            FastNoiseLite f = new FastNoiseLite(seed);
            f.SetNoiseType(FastNoiseLite.NoiseType.Perlin);
            f.SetFrequency(WARP_FREQUENCY);
            warp = f;
            worley = w;
        }
    }

    @Override
    public double compute(FunctionContext context) {
        int x = context.blockX();
        int y = context.blockY();
        int z = context.blockZ();

        if (y < MIN_CAVE_HEIGHT || y > MAX_CAVE_HEIGHT) return SOLID;

        // Surface gate is conditional. On land columns, caves carve up to the surface naturally
        // (WorleyCaves' original behavior — surface entrances on hillsides/mountains). On water
        // columns (ocean, river, anything where surface is at/below sea level), apply a hard stop
        // a margin below the actual surface so caves can't breach into surface water.
        NoiseChunk nc = NoiseChunkContext.CURRENT.get();
        int surface = (nc != null) ? nc.preliminarySurfaceLevel(x, z) : MAX_CAVE_HEIGHT;
        boolean waterColumn = false;
        if (nc != null) {
            int near = surface;
            near = Math.min(near, nc.preliminarySurfaceLevel(x + RIVER_DETECT_RADIUS, z));
            near = Math.min(near, nc.preliminarySurfaceLevel(x - RIVER_DETECT_RADIUS, z));
            near = Math.min(near, nc.preliminarySurfaceLevel(x, z + RIVER_DETECT_RADIUS));
            near = Math.min(near, nc.preliminarySurfaceLevel(x, z - RIVER_DETECT_RADIUS));
            waterColumn = near <= SEA_LEVEL + RIVER_SURFACE_BUFFER;
        }
        if (!waterColumn) {
            DensityFunction c = this.continents;
            if (c != null && c.compute(context) < OCEAN_CONTINENTALNESS_THRESHOLD) {
                waterColumn = true;
            }
        }
        int easeTop;
        if (waterColumn) {
            int hardStopY = Math.min(surface - SURFACE_HARD_STOP_MARGIN, MAX_CAVE_HEIGHT);
            if (y > hardStopY) return SOLID;
            easeTop = hardStopY;
        } else {
            easeTop = MAX_CAVE_HEIGHT;
        }

        ensureSeeded();

        // Reference formula was calibrated for Y ∈ [1, 128]; clamp so the extended floor below
        // Y=1 mirrors the reference's deepest warp (~9.37) rather than extrapolating past it.
        int ampY = Math.max(y, 1);
        float dispAmp = WARP_AMPLIFIER * ((MAX_CAVE_HEIGHT - ampY * 0.5f) / (MAX_CAVE_HEIGHT * 0.85f));
        float dx = warp.GetNoise(x, z) * dispAmp;
        float dy = warp.GetNoise(x, z + 67f) * dispAmp;
        float dz = warp.GetNoise(x, z + 149f) * dispAmp;

        float wx = x + dx;
        float wy = y * Y_COMPRESSION + dy;
        float wz = z + dz;

        float n = worley.sample(wx, wy, wz);

        double threshold = NOISE_CUTOFF;
        int easeStart = easeTop - EASE_IN_DEPTH;
        if (y > easeStart) {
            double t = (y - easeStart) / (double) EASE_IN_DEPTH;
            threshold = NOISE_CUTOFF * (1.0 - t) + SURFACE_CUTOFF * t;
        }
        int floorSoftenTop = MIN_CAVE_HEIGHT + 5;
        if (y < floorSoftenTop) {
            threshold += 0.05 * (floorSoftenTop - y);
        }

        return n > threshold ? AIR : SOLID;
    }

    @Override
    public double minValue() {
        return AIR;
    }

    @Override
    public double maxValue() {
        return SOLID;
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return KD_CODEC;
    }
}
