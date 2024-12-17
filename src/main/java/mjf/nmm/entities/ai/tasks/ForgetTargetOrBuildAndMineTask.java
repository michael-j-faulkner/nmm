package mjf.nmm.entities.ai.tasks;

import java.util.List;
import java.util.Optional;

import com.jcraft.jorbis.Block;

import mjf.nmm.entities.ai.sensors.CustomMemoryModuleType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.MemoryQueryResult;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.ServerWorldAccess;

public class ForgetTargetOrBuildAndMineTask {
    public static <E extends MobEntity> Task<E> create() {
        return TaskTriggerer.task(context -> 
            context.group(
                context.queryMemoryValue(MemoryModuleType.ATTACK_TARGET), 
                context.queryMemoryOptional(CustomMemoryModuleType.NEAREST_TARGETABLE_PLAYERS), 
                context.queryMemoryOptional(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE), 
                context.queryMemoryOptional(CustomMemoryModuleType.MINE_BLOCK_LOCATION),
                context.queryMemoryOptional(CustomMemoryModuleType.PLACE_BLOCK_LOCATION))
            .apply(context, 
                (attackTarget, nearbyPlayers, cantReachWalkTargetSince, mineBlockLocation, placeBlockLocation) -> (world, entity, time) -> {
                    LivingEntity target = context.getValue(attackTarget);

                    boolean cantReachTarget = ForgetTargetOrBuildAndMineTask.cannotReachTarget(entity, context.getOptionalValue(cantReachWalkTargetSince));
                    if (!entity.canTarget(target) || cantReachTarget || !target.isAlive() || target.getWorld() != entity.getWorld()) {
                        Optional<List<PlayerEntity>> players = context.getOptionalValue(nearbyPlayers);
                        if (cantReachTarget && players.isPresent() && players.get().contains(target)) {
                            if (entity.getNavigation().isIdle()) {
                                ForgetTargetOrBuildAndMineTask.updateBlockTargets(world, entity, target, mineBlockLocation, placeBlockLocation);
                                // mineBlockLocation.remember(ForgetTargetOrBuildAndMineTask.getMineBlockLocation(world, entity, target));
                                // placeBlockLocation.remember(ForgetTargetOrBuildAndMineTask.getPlaceBlockLocation(world, entity, target));
                            }
                        } else {
                            attackTarget.forget();
                        }
                        return true;
                    }
                    return true;
                }));
    }

    private static boolean tryGetBlocksForTargePos(BlockPos targetPos, ServerWorld world, MemoryQueryResult<?, BlockPos> mineBlockLocation,  MemoryQueryResult<?, BlockPos> placeBlockLocation, int deltaYSign, int mobHeight, boolean avoidPillaring) {
        if (deltaYSign > 0 && avoidPillaring && !isEmpty(world, targetPos.down(3))) {
            return false;
        }
        switch (deltaYSign) {
        case 1:
            for (int i = 0; i < mobHeight; ++i) {
                BlockPos pos = targetPos.add(0, i, 0);
                if (!isEmpty(world, pos)) {
                    mineBlockLocation.remember(pos);
                    return true;
                }
            }
            if (isEmpty(world, targetPos.down(2))) {
                placeBlockLocation.remember(targetPos.down(2));
                return true;
            }
            if (isEmpty(world, targetPos.down())) {
                placeBlockLocation.remember(targetPos.down());
                return true;
            }
            break;
        case 0:
            for (int i = mobHeight - 1; i >= 0; --i) {
                BlockPos pos = targetPos.add(0, i, 0);
                if (!isEmpty(world, pos)) {
                    mineBlockLocation.remember(pos);
                    return true;
                }
            }
            if (isEmpty(world, targetPos.down())) {
                placeBlockLocation.remember(targetPos.down());
                return true;
            }
            break;
        case -1:
            for (int i = mobHeight; i >= 0; --i) {
                BlockPos pos = targetPos.add(0, i, 0);
                if (!isEmpty(world, pos)) {
                    mineBlockLocation.remember(pos);
                    return true;
                }
            }
            break;
        }
        return false;
    }

    private static boolean updateBlockTargets(ServerWorld world, LivingEntity entity, LivingEntity target, MemoryQueryResult<?, BlockPos> mineBlockLocation,  MemoryQueryResult<?, BlockPos> placeBlockLocation) {
        BlockPos entityPos = entity.getBlockPos();
        BlockPos targetPos = target.getBlockPos();
        int entityHeight = (int)Math.ceil(entity.getHeight());
        int deltaX = targetPos.getX() - entityPos.getX();
        int deltaY = targetPos.getY() - entityPos.getY();
        int deltaZ = targetPos.getZ() - entityPos.getZ();
        int deltaXSign = Integer.signum(deltaX);
        int deltaYSign = Integer.signum(deltaY);
        int deltaZSign = Integer.signum(deltaZ);

        // Head room for jumps
        if (deltaYSign > 0 && !isEmpty(world, entityPos.up(entityHeight))) {
            mineBlockLocation.remember(entityPos.up(entityHeight));
            return true;
        }

        // Try and dig the correct direction
        if (
            tryGetBlocksForTargePos( 
                Math.abs(deltaX) > Math.abs(deltaZ) ? 
                entityPos.add(deltaXSign != 0 ? deltaXSign : 1, deltaYSign, 0) :
                entityPos.add(0, deltaYSign, deltaZSign != 0 ? deltaZSign : 1), world, mineBlockLocation, placeBlockLocation, deltaYSign, entityHeight, true) ||
            tryGetBlocksForTargePos( 
                Math.abs(deltaX) > Math.abs(deltaZ) ? 
                entityPos.add(0, deltaYSign, deltaZSign != 0 ? deltaZSign : 1) :
                entityPos.add(deltaXSign != 0 ? deltaXSign : 1, deltaYSign, 0), world, mineBlockLocation, placeBlockLocation, deltaYSign, entityHeight, true) ||
            tryGetBlocksForTargePos( 
                Math.abs(deltaX) > Math.abs(deltaZ) ? 
                entityPos.add(0, deltaYSign, -(deltaZSign != 0 ? deltaZSign : 1)) :
                entityPos.add(-(deltaXSign != 0 ? deltaXSign : 1), deltaYSign, 0), world, mineBlockLocation, placeBlockLocation, deltaYSign, entityHeight, true) ||
            tryGetBlocksForTargePos( 
                Math.abs(deltaX) > Math.abs(deltaZ) ? 
                entityPos.add(-(deltaXSign != 0 ? deltaXSign : 1), deltaYSign, 0) :
                entityPos.add(0, deltaYSign, -(deltaZSign != 0 ? deltaZSign : 1)), world, mineBlockLocation, placeBlockLocation, deltaYSign, entityHeight, true) ||
            // Non avoid pillaring copies
            tryGetBlocksForTargePos( 
                Math.abs(deltaX) > Math.abs(deltaZ) ? 
                entityPos.add(deltaXSign != 0 ? deltaXSign : 1, deltaYSign, 0) :
                entityPos.add(0, deltaYSign, deltaZSign != 0 ? deltaZSign : 1), world, mineBlockLocation, placeBlockLocation, deltaYSign, entityHeight, false) ||
            tryGetBlocksForTargePos( 
                Math.abs(deltaX) > Math.abs(deltaZ) ? 
                entityPos.add(0, deltaYSign, deltaZSign != 0 ? deltaZSign : 1) :
                entityPos.add(deltaXSign != 0 ? deltaXSign : 1, deltaYSign, 0), world, mineBlockLocation, placeBlockLocation, deltaYSign, entityHeight, false) ||
            tryGetBlocksForTargePos( 
                Math.abs(deltaX) > Math.abs(deltaZ) ? 
                entityPos.add(0, deltaYSign, -(deltaZSign != 0 ? deltaZSign : 1)) :
                entityPos.add(-(deltaXSign != 0 ? deltaXSign : 1), deltaYSign, 0), world, mineBlockLocation, placeBlockLocation, deltaYSign, entityHeight, false) ||
            tryGetBlocksForTargePos( 
                Math.abs(deltaX) > Math.abs(deltaZ) ? 
                entityPos.add(-(deltaXSign != 0 ? deltaXSign : 1), deltaYSign, 0) :
                entityPos.add(0, deltaYSign, -(deltaZSign != 0 ? deltaZSign : 1)), world, mineBlockLocation, placeBlockLocation, deltaYSign, entityHeight, false)
            ) {
            return true;
        }

        // Dig down
        if (deltaYSign < 0 && !isEmpty(world, entityPos.down())) {
            mineBlockLocation.remember(entityPos.down());
            return true;
        }

        return false;
    }

    private static boolean isEmpty(BlockView world, BlockPos pos) {
        return world.getBlockState(pos).canPathfindThrough(NavigationType.LAND);
    }

    private static boolean cannotReachTarget(LivingEntity livingEntity, Optional<Long> optional) {
        return optional.isPresent() && livingEntity.getWorld().getTime() - optional.get() > 0L;
    }
}
