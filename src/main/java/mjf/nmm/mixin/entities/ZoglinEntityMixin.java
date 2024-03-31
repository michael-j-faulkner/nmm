package mjf.nmm.mixin.entities;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.common.collect.ImmutableList;

import mjf.nmm.entities.ai.sensors.CustomMemoryModuleType;
import mjf.nmm.entities.ai.sensors.CustomSensorType;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.ZoglinEntity;

@Mixin(ZoglinEntity.class)
public class ZoglinEntityMixin {
    @Shadow
    protected static final ImmutableList<? extends SensorType<? extends Sensor<? super ZoglinEntity>>> USED_SENSORS = ImmutableList.of(
        CustomSensorType.FOLLOW_RANGE_ENTITIES, CustomSensorType.FOLLOW_RANGE_PLAYERS);
    @Shadow
    protected static final ImmutableList<? extends MemoryModuleType<?>> USED_MEMORY_MODULES = ImmutableList.of(
        MemoryModuleType.MOBS, MemoryModuleType.VISIBLE_MOBS, MemoryModuleType.NEAREST_VISIBLE_PLAYER, 
        MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER, MemoryModuleType.LOOK_TARGET, MemoryModuleType.WALK_TARGET, 
        MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.ATTACK_TARGET, MemoryModuleType.ATTACK_COOLING_DOWN,
        CustomMemoryModuleType.NEAREST_TARGETABLE_PLAYER, CustomMemoryModuleType.NEAREST_TARGETABLE_PLAYERS);
    
    @Inject(at = @At("RETURN"), method = "createZoglinAttributes", cancellable = true)
	private static void editAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
		cir.setReturnValue(cir.getReturnValue()
			.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 12.0)
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.35));
	}
}
