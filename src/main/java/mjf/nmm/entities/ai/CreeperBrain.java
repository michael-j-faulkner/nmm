package mjf.nmm.entities.ai;

import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;

import mjf.nmm.entities.ai.sensors.CustomSensorType;
import mjf.nmm.entities.ai.tasks.CreeperIgniteTask;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.ai.brain.task.ForgetAttackTargetTask;
import net.minecraft.entity.ai.brain.task.LookAroundTask;
import net.minecraft.entity.ai.brain.task.LookAtMobTask;
import net.minecraft.entity.ai.brain.task.RandomTask;
import net.minecraft.entity.ai.brain.task.RangedApproachTask;
import net.minecraft.entity.ai.brain.task.StrollTask;
import net.minecraft.entity.ai.brain.task.UpdateAttackTargetTask;
import net.minecraft.entity.ai.brain.task.WaitTask;
import net.minecraft.entity.ai.brain.task.WanderAroundTask;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.CreeperEntity;

public class CreeperBrain {
	public static final ImmutableList<SensorType<? extends Sensor<? super CreeperEntity>>> SENSORS = ImmutableList.of(
		CustomSensorType.FOLLOW_RANGE_ENTITIES, CustomSensorType.FOLLOW_RANGE_PLAYERS);
    public static final ImmutableList<MemoryModuleType<?>> MEMORY_MODULES = ImmutableList.of(
        MemoryModuleType.MOBS, MemoryModuleType.VISIBLE_MOBS,
        MemoryModuleType.NEAREST_PLAYERS, MemoryModuleType.NEAREST_VISIBLE_PLAYER, 
        MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER,
        MemoryModuleType.LOOK_TARGET, MemoryModuleType.WALK_TARGET, 
		MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.ATTACK_TARGET);

    public static Brain<CreeperEntity> create(CreeperEntity creeper, Brain<CreeperEntity> brain) {
        CreeperBrain.addCoreActivities(creeper, brain);
        CreeperBrain.addIdleActivities(creeper, brain);
        CreeperBrain.addFightActivities(creeper, brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.resetPossibleActivities();
        return brain;
    }

    private static void addCoreActivities(CreeperEntity creeper, Brain<CreeperEntity> brain) {
        brain.setTaskList(Activity.CORE, 0, ImmutableList.of(
            new LookAroundTask(45, 90),
            new WanderAroundTask(),
            CreeperIgniteTask.create()));
    }

    private static void addIdleActivities(CreeperEntity creeper, Brain<CreeperEntity> brain) {
        brain.setTaskList(Activity.IDLE, 10, ImmutableList.of(
            UpdateAttackTargetTask.create(CreeperBrain::getTarget), 
            LookAtMobTask.create((float)creeper.getAttributeValue(EntityAttributes.GENERIC_FOLLOW_RANGE)),
            new RandomTask<CreeperEntity>(ImmutableList.of(
                Pair.of(StrollTask.create(1.0f), 1),
                Pair.of(new WaitTask(30, 60), 1)))
        ));
    }

    private static void addFightActivities(CreeperEntity creeper, Brain<CreeperEntity> brain) {
        brain.setTaskList(Activity.FIGHT, 10, ImmutableList.of(
            ForgetAttackTargetTask.create(),
            RangedApproachTask.create(1.0f)
        ), MemoryModuleType.ATTACK_TARGET);
    }

    public static void updateActivities(Brain<CreeperEntity> brain) {
		brain.resetPossibleActivities(ImmutableList.of(Activity.FIGHT, Activity.IDLE));
    }

    private static Optional<? extends LivingEntity> getTarget(CreeperEntity creeper) {
        return creeper.getBrain().getOptionalRegisteredMemory(MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER);
    }
}
