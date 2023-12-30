package mjf.nmm.entities.ai.sensors;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableSet;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.NearestPlayersSensor;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;

public class FollowRangePlayersSensor extends Sensor<LivingEntity> {
    @Override
    public Set<MemoryModuleType<?>> getOutputMemoryModules() {
        return ImmutableSet.of(MemoryModuleType.NEAREST_PLAYERS, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER);
    }

    @Override
    protected void sense(ServerWorld world, LivingEntity entity) {
        List<PlayerEntity> nearbyPlayers = world.getPlayers().stream().filter(EntityPredicates.EXCEPT_SPECTATOR)
            .filter(player -> entity.isInRange(player, entity.getAttributeValue(EntityAttributes.GENERIC_FOLLOW_RANGE)))
            .sorted(Comparator.comparingDouble(entity::squaredDistanceTo)).collect(Collectors.toList());
        Brain<?> brain = entity.getBrain();
        brain.remember(MemoryModuleType.NEAREST_PLAYERS, nearbyPlayers);
        List<PlayerEntity> visiblePlayers = nearbyPlayers.stream()
            .filter(player -> FollowRangePlayersSensor.testTargetPredicate(entity, player))
            .collect(Collectors.toList());
        brain.remember(MemoryModuleType.NEAREST_VISIBLE_PLAYER, visiblePlayers.isEmpty() ? null : visiblePlayers.get(0));
        Optional<PlayerEntity> nearestTargetablePlayer = visiblePlayers.stream()
            .filter(player -> NearestPlayersSensor.testAttackableTargetPredicate(entity, player)).findFirst();
        brain.remember(MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER, nearestTargetablePlayer);
    }
    
}
