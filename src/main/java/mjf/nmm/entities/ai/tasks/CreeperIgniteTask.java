package mjf.nmm.entities.ai.tasks;

import java.util.Optional;

import mjf.nmm.entities.ScalingDifficulty;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.LivingTargetCache;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.MobEntity;

public class CreeperIgniteTask {
    public static <E extends MobEntity> Task<E> create() {
        return TaskTriggerer.task(context -> context.group(context.queryMemoryOptional(MemoryModuleType.ATTACK_TARGET), context.queryMemoryOptional(MemoryModuleType.VISIBLE_MOBS)).apply(context, (attackTarget, visibleMobs) -> (world, entity, time) -> {
            Optional<LivingEntity> target = context.getOptionalValue(attackTarget);
            Optional<LivingTargetCache> visibilityCache = context.getOptionalValue(visibleMobs);
            CreeperEntity creeper = (CreeperEntity) entity;
            if (!target.isPresent() || !visibilityCache.isPresent()) {
                creeper.setFuseSpeed(-1);
                return true;
            }
            if (creeper.squaredDistanceTo(target.get()) > 16.0 + 20.0 * ScalingDifficulty.getPercentDifficulty(world, creeper.getPos())) {
                creeper.setFuseSpeed(-1);
                return true;
            }
            if (!visibilityCache.get().contains(target.get())) {
                creeper.setFuseSpeed(-1);
                return true;
            }
            creeper.setFuseSpeed(1);
            return true;
        }));
    }
}
