package mjf.nmm.entities.ai;

import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;

import mjf.nmm.entities.ai.sensors.CustomActivity;
import mjf.nmm.entities.ai.sensors.CustomMemoryModuleType;
import mjf.nmm.entities.ai.sensors.CustomSensorType;
import mjf.nmm.entities.ai.tasks.ForgetTargetOrMineTask;
import mjf.nmm.entities.ai.tasks.MineBlockTask;
import net.fabricmc.fabric.api.entity.event.v1.EntityElytraEvents.Custom;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.LivingTargetCache;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.ai.brain.task.LookAroundTask;
import net.minecraft.entity.ai.brain.task.LookAtMobTask;
import net.minecraft.entity.ai.brain.task.MeleeAttackTask;
import net.minecraft.entity.ai.brain.task.RandomTask;
import net.minecraft.entity.ai.brain.task.RangedApproachTask;
import net.minecraft.entity.ai.brain.task.StrollTask;
import net.minecraft.entity.ai.brain.task.UpdateAttackTargetTask;
import net.minecraft.entity.ai.brain.task.WaitTask;
import net.minecraft.entity.ai.brain.task.WanderAroundTask;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.TurtleEntity;

public class ZombieBrain {
    public static final ImmutableList<SensorType<? extends Sensor<? super ZombieEntity>>> SENSORS = ImmutableList.of(
		CustomSensorType.FOLLOW_RANGE_ENTITIES, CustomSensorType.FOLLOW_RANGE_PLAYERS);
    public static final ImmutableList<MemoryModuleType<?>> MEMORY_MODULES = ImmutableList.of(
        MemoryModuleType.MOBS, MemoryModuleType.VISIBLE_MOBS,
        MemoryModuleType.NEAREST_PLAYERS, CustomMemoryModuleType.NEAREST_TARGETABLE_PLAYERS,
        CustomMemoryModuleType.NEAREST_TARGETABLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_PLAYER, 
        MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER,
        MemoryModuleType.LOOK_TARGET, MemoryModuleType.WALK_TARGET, CustomMemoryModuleType.MINE_BLOCK_LOCATION,
		MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.ATTACK_TARGET,
        MemoryModuleType.ATTACK_COOLING_DOWN);

    public static Brain<ZombieEntity> create(ZombieEntity zombie, Brain<ZombieEntity> brain) {
        ZombieBrain.addCoreActivities(zombie, brain);
        ZombieBrain.addIdleActivities(zombie, brain);
        ZombieBrain.addFightActivities(zombie, brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.resetPossibleActivities();
        return brain;
    }

    private static void addCoreActivities(ZombieEntity zombie, Brain<ZombieEntity> brain) {
        brain.setTaskList(Activity.CORE, 0, ImmutableList.of(
            new LookAroundTask(45, 90),
            new WanderAroundTask()));
    }

    private static void addIdleActivities(ZombieEntity zombie, Brain<ZombieEntity> brain) {
        brain.setTaskList(Activity.IDLE, 10, ImmutableList.of(
            UpdateAttackTargetTask.create(ZombieBrain::getTarget), 
            LookAtMobTask.create((float)zombie.getAttributeValue(EntityAttributes.GENERIC_FOLLOW_RANGE)),
            new RandomTask<ZombieEntity>(ImmutableList.of(
                Pair.of(StrollTask.create(1.0f), 1),
                Pair.of(new WaitTask(30, 60), 1)))
        ));
    }

    private static void addFightActivities(ZombieEntity zombie, Brain<ZombieEntity> brain) {
        brain.setTaskList(Activity.FIGHT, 10, ImmutableList.of(
            ForgetTargetOrMineTask.create(),
            RangedApproachTask.create(1.0f),
            MeleeAttackTask.create(10),
            new MineBlockTask<ZombieEntity>()
        ), MemoryModuleType.ATTACK_TARGET);
    }

    public static void updateActivities(Brain<ZombieEntity> brain) {
		brain.resetPossibleActivities(ImmutableList.of(Activity.FIGHT, Activity.IDLE));
    }

    private static Optional<? extends LivingEntity> getTarget(ZombieEntity zombie) {
        Optional<? extends LivingEntity> target = zombie.getBrain().getOptionalRegisteredMemory(MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER);
        if (target.isPresent())
            return target;
        Optional<? extends LivingTargetCache> visibleMobs = zombie.getBrain().getOptionalRegisteredMemory(MemoryModuleType.VISIBLE_MOBS);
        if (visibleMobs.isPresent()) {
            return visibleMobs.get().findFirst(ZombieBrain::isZombieTarget);
        }
        return Optional.empty();
    }

    private static boolean isZombieTarget(LivingEntity entity) {
        return entity instanceof MerchantEntity || entity instanceof IronGolemEntity || entity instanceof TurtleEntity;
    }
}
