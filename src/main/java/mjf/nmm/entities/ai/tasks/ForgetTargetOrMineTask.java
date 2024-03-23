package mjf.nmm.entities.ai.tasks;

import java.util.List;
import java.util.Optional;

import javax.swing.text.html.Option;

import mjf.nmm.NightmareMode;
import mjf.nmm.entities.ai.sensors.CustomMemoryModuleType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.ServerWorldAccess;

public class ForgetTargetOrMineTask {
    public static <E extends MobEntity> Task<E> create() {
        return TaskTriggerer.task(context -> context.group(context.queryMemoryValue(MemoryModuleType.ATTACK_TARGET), context.queryMemoryOptional(CustomMemoryModuleType.NEAREST_TARGETABLE_PLAYERS), context.queryMemoryOptional(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE), context.queryMemoryOptional(CustomMemoryModuleType.MINE_BLOCK_LOCATION)).apply(context, (attackTarget, nearbyPlayers, cantReachWalkTargetSince, mineBlockLocation) -> (world, entity, time) -> {
            LivingEntity target = context.getValue(attackTarget);

            boolean cantReachTarget = ForgetTargetOrMineTask.cannotReachTarget(entity, context.getOptionalValue(cantReachWalkTargetSince));
            if (!entity.canTarget(target) || cantReachTarget || !target.isAlive() || target.getWorld() != entity.getWorld()) {
                Optional<List<PlayerEntity>> players = context.getOptionalValue(nearbyPlayers);
                if (cantReachTarget && players.isPresent() && players.get().contains(target)) {
                    if (entity.getNavigation().isIdle())
                        mineBlockLocation.remember(ForgetTargetOrMineTask.getMineBlockLocation(world, entity, target));
                } else {
                    attackTarget.forget();
                }
                return true;
            }
            return true;
        }));
    }

    private static Optional<BlockPos> getMineBlockLocation(ServerWorldAccess world, LivingEntity miner, LivingEntity target) {
        BlockPos minerPos = miner.getBlockPos();
        BlockPos targetPos = target.getBlockPos();
        int minerHeight = Math.round(miner.getHeight());
        int deltaX = targetPos.getX() - minerPos.getX();
        int deltaY = targetPos.getY() - minerPos.getY();
        int deltaZ = targetPos.getZ() - minerPos.getZ();
        
        if (deltaY > 0) {
            // Give yourself room to jump
            Optional<BlockPos> potentialPos = nextBlockPosInStack(world, minerPos.add(0, minerHeight, 0), 1);
            if (potentialPos.isPresent())
                return potentialPos;

            if (Math.abs(Math.abs(deltaX) - Math.abs(deltaZ)) < 3) {
                // If you're more or less directly underneath, intelligently spiral staircase up

                // Try digging in the logical direction if possible, otherwise go to the side by 1
                if (Math.abs(deltaX) > Math.abs(deltaZ)) {
                    potentialPos = nextBlockPosInStepUp(world, minerPos.add(deltaX != 0 ? Integer.signum(deltaX) : 1, 0, 0), minerHeight);
                    if (potentialPos.isPresent())
                        return potentialPos;
                    
                    potentialPos = nextBlockPosInStepUp(world, minerPos.add(0, 0, deltaZ != 0 ? Integer.signum(deltaZ) : 1), minerHeight);
                    if (potentialPos.isPresent())
                        return potentialPos;
                    
                    potentialPos = nextBlockPosInStepUp(world, minerPos.add(0, 0, deltaZ != 0 ? -Integer.signum(deltaZ) : -1), minerHeight);
                    if (potentialPos.isPresent())
                        return potentialPos;

                    potentialPos = nextBlockPosInStepUp(world, minerPos.add(deltaX != 0 ? -Integer.signum(deltaX) : -1, 0, 0), minerHeight);
                    return potentialPos;
                } else {
                    potentialPos = nextBlockPosInStepUp(world, minerPos.add(0, 0, deltaZ != 0 ? Integer.signum(deltaZ) : 1), minerHeight);
                    if (potentialPos.isPresent())
                        return potentialPos;
                    
                    potentialPos = nextBlockPosInStepUp(world, minerPos.add(deltaX != 0 ? Integer.signum(deltaX) : 1, 0, 0), minerHeight);
                    if (potentialPos.isPresent())
                        return potentialPos;
                    
                    potentialPos = nextBlockPosInStepUp(world, minerPos.add(deltaX != 0 ? -Integer.signum(deltaX) : -1, 0, 0), minerHeight);
                    if (potentialPos.isPresent())
                        return potentialPos;

                    potentialPos = nextBlockPosInStepUp(world, minerPos.add(0, 0, deltaZ != 0 ? -Integer.signum(deltaZ) : -1), minerHeight);
                    return potentialPos;
                }
            } else {
                // If not directly underneath, staircase up in direction you have further to go
                if (Math.abs(deltaX) > Math.abs(deltaZ))
                    return nextBlockPosInStack(world, minerPos.add(Integer.signum(deltaX), 1, 0), minerHeight);
                else
                    return nextBlockPosInStack(world, minerPos.add(0, 1, Integer.signum(deltaZ)), minerHeight);
            }
        } else if (deltaY < 0) {
            // Staircase down in the direction you have further to go
            if (Math.abs(deltaX) > Math.abs(deltaZ))
                return nextBlockPosInStack(world, minerPos.add(Integer.signum(deltaX), -1, 0), minerHeight + 1);
            else
                return nextBlockPosInStack(world, minerPos.add(0, -1, Integer.signum(deltaZ)), minerHeight + 1);
        } else {
            // Dig in the direction you have further to go
            if (Math.abs(deltaX) > Math.abs(deltaZ))
                return nextBlockPosInStack(world, minerPos.add(Integer.signum(deltaX), 0, 0), minerHeight);
            else
                return nextBlockPosInStack(world, minerPos.add(0, 0, Integer.signum(deltaZ)), minerHeight);
        }
    }

    // Break the block at eye level first, then at feet
    private static Optional<BlockPos> nextBlockPosInStack(BlockView world, BlockPos pos, int height) {
        for (int i = height - 1; i >= 0; --i) {
            BlockPos currPos = pos.add(0, i, 0);
            if (isEmpty(world, currPos)) 
                continue;
            return Optional.of(currPos);
        }
        return Optional.empty();
    }

    private static Optional<BlockPos> nextBlockPosInStepUp(BlockView world, BlockPos step, int height) {
        if (!isEmpty(world, step)) {
            Optional<BlockPos> potentialPos = nextBlockPosInStack(world, step.add(0, 1, 0), height);
            if (potentialPos.isPresent())
                return potentialPos;
        }
        return Optional.empty();
    }

    private static boolean isEmpty(BlockView world, BlockPos pos) {
        return world.getBlockState(pos).canPathfindThrough(world, pos, NavigationType.LAND);
    }

    private static boolean cannotReachTarget(LivingEntity livingEntity, Optional<Long> optional) {
        return optional.isPresent() && livingEntity.getWorld().getTime() - optional.get() > 0L;
    }
}
