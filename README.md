# Worlium
[Modrinth](https://modrinth.com/mod/worlium) · [Discord](https://discord.gg/CRTpUpjSTR)

Worlium replaces all vanilla cave generators with a Worley-noise-based carver modeled on the classic [Worley's Caves](https://www.curseforge.com/minecraft/mc-mods/worleys-caves) mod by fluke. The Worlium carver produces sprawling, winding, interconnected tunnels; from a gameplay perspective, the underground becomes a massive, labyrinth-like cave system that feels simultaneously claustrophobic and expansive.

The mod ships for both Fabric and NeoForge through a shared common module that implements the carver as a custom `DensityFunction` (`worlium:worley_caves`) plugging into Minecraft's modern worldgen pipeline. Under the hood, the density function samples a seeded 3D Worley F1/F3-edge cellular noise warped by a 2D Perlin displacement field, applies a depth-scaled warp amplitude so caves become more chaotic closer to bedrock, and eases the cutoff back toward solid near the surface to avoid carving terrain into shreds. The result is a drop-in cave overhaul that preserves the visual identity of the original Worley's Caves while living entirely inside the 1.18+ density-function framework.
<img width="1920" height="1008" alt="the_worliuming" src="https://github.com/user-attachments/assets/765c9399-0cf2-47ea-891f-3233f52ecf66" />

screenshot


## Features
- replaces all vanilla caves with a Worley noise-based carver
- custom aquifer logic (prevents caves from being flooded with water)
- increased diamond rarity
- removal of diorite, andesite, and granite blobs

## Known Issues
- when close to aquatic biomes (oceans, rivers, etc) the carver will occasionally terminate with a flat stone wall
- the current aquifer removal logic uses biome tags, which works pretty well but is horrible for compatibility with other worldgen mods

## Compatibility
- **Terralith**: excellent
- **CliffTree**: good
- **Tectonic**: suboptimal
- **Cave Overhaul**: no

## License

_Worlium_ is Free Software: You can use, study share and improve it at your
will. Specifically you can redistribute and/or modify it under the terms of the
[GNU General Public License, Version 3](https://www.gnu.org/licenses/gpl-3.0.en.html) as
published by the Free Software Foundation.
