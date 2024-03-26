package mjf.nmm.entities.ai.sensors;

import java.util.function.Supplier;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class CustomSensorType<U extends Sensor<?>> {
    public static final SensorType<FollowRangePlayersSensor> FOLLOW_RANGE_PLAYERS = register("follow_range_players", FollowRangePlayersSensor::new);
    public static final SensorType<FollowRangeEntitySensor<LivingEntity>> FOLLOW_RANGE_ENTITIES = register("follow_range_entities", FollowRangeEntitySensor::new);
    private final Supplier<U> factory;

    public CustomSensorType(Supplier<U> factory) {
        this.factory = factory;
    }

    public U create() {
        return this.factory.get();
    }

    private static <U extends Sensor<?>> SensorType<U> register(String id, Supplier<U> factory) {
        return Registry.register(Registries.SENSOR_TYPE, new Identifier(id), new SensorType<U>(factory));
    }
}
