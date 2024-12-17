package mjf.nmm.mixin.entities;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.dimension.DimensionTypes;

@Mixin(SpawnRestriction.class)
public class SpawnRestrictionMixin {
    // NOTE: Very subject to break upon new mobs being added
    @ModifyArg(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/SpawnRestriction;register(Lnet/minecraft/entity/EntityType;Lnet/minecraft/entity/SpawnLocation;Lnet/minecraft/world/Heightmap$Type;Lnet/minecraft/entity/SpawnRestriction$SpawnPredicate;)V", ordinal = 69))
    private static <T extends MobEntity> SpawnRestriction.SpawnPredicate<T> phantomsOnlySpawnInDark(SpawnRestriction.SpawnPredicate<T> predicate) {
        return (EntityType<T> type, ServerWorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) -> 
            world.getDifficulty() != Difficulty.PEACEFUL && (SpawnReason.isTrialSpawner(spawnReason) || HostileEntity.isSpawnDark(world, pos, random))
            && MobEntity.canMobSpawn(type, world, spawnReason, pos, random) && (world.isSkyVisible(pos) 
            || world.getDimension().equals(world.getRegistryManager().getOptional(DimensionTypes.THE_END.getRegistryRef()).get().get(DimensionTypes.THE_END)));
    }
}
