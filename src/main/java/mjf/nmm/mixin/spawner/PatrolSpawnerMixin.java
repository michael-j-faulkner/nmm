package mjf.nmm.mixin.spawner;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import mjf.nmm.entities.ScalingDifficulty;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.EvokerEntity;
import net.minecraft.entity.mob.PatrolEntity;
import net.minecraft.entity.mob.PillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameRules;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.spawner.PatrolSpawner;
import net.minecraft.world.Heightmap;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.spawner.SpecialSpawner;

@Mixin(PatrolSpawner.class)
public class PatrolSpawnerMixin implements SpecialSpawner {
	private int cooldown;

    protected boolean canSpawn(ServerWorld world, boolean spawnMonsters) {
        if (!spawnMonsters 
            || !world.getGameRules().getBoolean(GameRules.DO_PATROL_SPAWNING)
            || world.getPlayers().isEmpty())
			return false;
        
        // Wait for the cooldown
        --this.cooldown;
        if (this.cooldown > 0)
            return false;

        return true;
    }

    /**
     * Custom patrol spawner to make them more frequent, scale
     * with the custom difficulty and include other mobs
     * @author
     * @reason
     */
	@Overwrite
	public int spawn(ServerWorld world, boolean spawnMonsters, boolean spawnAnimals) {
		if (this.canSpawn(world, spawnMonsters)) {
            Random random = world.random;
            this.cooldown = this.cooldown + 12000 + random.nextInt(12000);
            PlayerEntity playerEntity = (PlayerEntity)world.getRandomAlivePlayer();
            if (playerEntity == null || playerEntity.isSpectator()) {
                return 0;
            } else {
                // Get spawn location
                BlockPos.Mutable mutableBlockPos = playerEntity.getBlockPos().mutableCopy().move(
                    (64 + random.nextInt(32)) * (random.nextBoolean() ? -1 : 1), 
                    0, 
                    (64 + random.nextInt(32)) * (random.nextBoolean() ? -1 : 1));

                RegistryEntry<Biome> registryEntry = world.getBiome(mutableBlockPos);
                if (registryEntry.isIn(BiomeTags.WITHOUT_PATROL_SPAWNS)) {
                    return 0;
                } 
                
                int numPatrollers = (int)Math.ceil(19 * ScalingDifficulty.getPercentDifficulty(world, mutableBlockPos.toCenterPos())) + 1;
                int numSuccessfullySpawned = 0;
                for (int i = 0; i < numPatrollers; i++) {
                    mutableBlockPos.setY(world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, mutableBlockPos).getY());
                    if (this.spawnIllager(world, mutableBlockPos, random, numSuccessfullySpawned == 0)) {
                        numSuccessfullySpawned++;
                    }

                    mutableBlockPos.setX(mutableBlockPos.getX() + random.nextInt(5) - random.nextInt(5));
                    mutableBlockPos.setZ(mutableBlockPos.getZ() + random.nextInt(5) - random.nextInt(5));
                }

                return numSuccessfullySpawned;
            }
        }
        return 0;
	}

	/**
	 * @param captain whether the pillager is the captain of a patrol
	 */
	private boolean spawnIllager(ServerWorld world, BlockPos pos, Random random, boolean captain) {
        PatrolEntity patrolEntity = null;
        BlockState blockState = world.getBlockState(pos);

        switch (random.nextInt(10)) {
        case 0: case 1: case 2: 
            if (!SpawnHelper.isClearForSpawn(world, pos, blockState, blockState.getFluidState(), EntityType.PILLAGER)
                || !PatrolEntity.canSpawn(EntityType.PILLAGER, world, SpawnReason.PATROL, pos, random)) {
                return false;
            }
            patrolEntity = EntityType.PILLAGER.create(world);
            break;
        case 3: case 4: case 5:
            if (!SpawnHelper.isClearForSpawn(world, pos, blockState, blockState.getFluidState(), EntityType.VINDICATOR)
                || !PatrolEntity.canSpawn(EntityType.VINDICATOR, world, SpawnReason.PATROL, pos, random)) {
                return false;
            }
            patrolEntity = EntityType.VINDICATOR.create(world);
            break;
        case 6: case 7:
            if (!SpawnHelper.isClearForSpawn(world, pos, blockState, blockState.getFluidState(), EntityType.WITCH)
                || !PatrolEntity.canSpawn(EntityType.WITCH, world, SpawnReason.PATROL, pos, random)) {
                return false;
            }
            patrolEntity = EntityType.WITCH.create(world);
            break;
        case 8:
            if (!SpawnHelper.isClearForSpawn(world, pos, blockState, blockState.getFluidState(), EntityType.EVOKER)
                || !PatrolEntity.canSpawn(EntityType.EVOKER, world, SpawnReason.PATROL, pos, random)) {
                return false;
            }
            patrolEntity = EntityType.EVOKER.create(world);
            break;
        case 9:
            if (!SpawnHelper.isClearForSpawn(world, pos, blockState, blockState.getFluidState(), EntityType.RAVAGER)
                || !PatrolEntity.canSpawn(EntityType.RAVAGER, world, SpawnReason.PATROL, pos, random)) {
                return false;
            }
            patrolEntity = EntityType.RAVAGER.create(world);
            if (random.nextBoolean()) {
                PillagerEntity pillager = EntityType.PILLAGER.create(world);
                if (pillager != null) {
                    pillager.setPersistent();
                    pillager.setPosition((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
                    pillager.initialize(world, world.getLocalDifficulty(pos), SpawnReason.PATROL, null);
                    pillager.startRiding(patrolEntity);
                }
            } else {
                EvokerEntity evoker = EntityType.EVOKER.create(world);
                if (evoker != null) {
                    evoker.setPersistent();
                    evoker.setPosition((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
                    evoker.initialize(world, world.getLocalDifficulty(pos), SpawnReason.PATROL, null);
                    evoker.startRiding(patrolEntity);
                }
            }
            break;
        }

        if (patrolEntity != null) {
            if (captain) {
                patrolEntity.setPatrolLeader(true);
                patrolEntity.setRandomPatrolTarget();
            }

            patrolEntity.setPersistent();
            patrolEntity.setPosition((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
            patrolEntity.initialize(world, world.getLocalDifficulty(pos), SpawnReason.PATROL, null);
            world.spawnEntityAndPassengers(patrolEntity);
            return true;
        } else {
            return false;
        }
	}
}
