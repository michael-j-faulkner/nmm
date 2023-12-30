package mjf.nmm.entities.ai.sensors;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.sensor.NearestLivingEntitiesSensor;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.world.ServerWorld;

public class FollowRangeEntitySensor<T extends LivingEntity>
extends NearestLivingEntitiesSensor<T> {
    private double range = 32.0;

    @Override
    protected void sense(ServerWorld serverWorld, T entity) {
        this.range = entity.getAttributeValue(EntityAttributes.GENERIC_FOLLOW_RANGE);
        super.sense(serverWorld, entity);
    }

    @Override
    protected int getHorizontalExpansion() {
        return (int)this.range;
    }

    @Override
    protected int getHeightExpansion() {
        return (int)this.range;
    }
}
