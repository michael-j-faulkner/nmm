package mjf.nmm.world;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.tag.BiomeTags;

public class AlterSpawns {
    public static void alterSpawns() {
        BiomeModifications.addSpawn(biome -> biome.hasTag(BiomeTags.IS_OVERWORLD), SpawnGroup.MONSTER, EntityType.PHANTOM, 10, 8, 12);
        BiomeModifications.addSpawn(biome -> biome.hasTag(BiomeTags.IS_END), SpawnGroup.MONSTER, EntityType.PHANTOM, 2, 4, 4);
        BiomeModifications.addSpawn(biome -> biome.hasTag(BiomeTags.IS_OCEAN) || biome.hasTag(BiomeTags.IS_DEEP_OCEAN) || biome.hasTag(BiomeTags.IS_RIVER), SpawnGroup.MONSTER, EntityType.DROWNED, 100, 4, 4);
        BiomeModifications.addSpawn(biome -> biome.hasTag(BiomeTags.IS_DEEP_OCEAN), SpawnGroup.MONSTER, EntityType.GUARDIAN, 10, 4, 4);
    }
}
