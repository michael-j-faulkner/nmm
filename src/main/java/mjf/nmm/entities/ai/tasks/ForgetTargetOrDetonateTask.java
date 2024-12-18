package mjf.nmm.entities.ai.tasks;

import java.util.Optional;

import mjf.nmm.entities.ai.sensors.CustomMemoryModuleType;

import java.util.List;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.MobEntity;

public class ForgetTargetOrDetonateTask {
    public static <E extends MobEntity> Task<E> create() {
        return TaskTriggerer.task(context -> context.group(context.queryMemoryValue(MemoryModuleType.ATTACK_TARGET), context.queryMemoryOptional(CustomMemoryModuleType.NEAREST_TARGETABLE_PLAYERS), context.queryMemoryOptional(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)).apply(context, (attackTarget, nearbyPlayers, cantReachWalkTargetSince) -> (world, entity, time) -> {
            LivingEntity target = context.getValue(attackTarget);
            CreeperEntity creeper = (CreeperEntity)entity;
            Optional<List<PlayerEntity>> players = context.getOptionalValue(nearbyPlayers);
            boolean cantReachTarget = ForgetTargetOrDetonateTask.cannotReachTarget(entity, context.getOptionalValue(cantReachWalkTargetSince));
            if (!entity.canTarget(target) || cantReachTarget || !target.isAlive() || target.getWorld() != entity.getWorld()) {
                if (cantReachTarget && players.isPresent() && players.get().contains(target))
                    creeper.ignite();
                attackTarget.forget();
                return true;
            }
            return true;
        }));
    }

    private static boolean cannotReachTarget(LivingEntity livingEntity, Optional<Long> optional) {
        return optional.isPresent() && livingEntity.getWorld().getTime() - optional.get() > 200L;
    }
}
