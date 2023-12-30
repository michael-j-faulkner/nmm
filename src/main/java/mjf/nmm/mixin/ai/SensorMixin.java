package mjf.nmm.mixin.ai;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.attribute.EntityAttributes;

@Mixin(Sensor.class)
public class SensorMixin {
    private static final double FALLBACK_FOLLOW_RANGE = 32.0;
    @Shadow
    private static final TargetPredicate TARGET_PREDICATE = TargetPredicate.createNonAttackable().setBaseMaxDistance(FALLBACK_FOLLOW_RANGE);
    @Shadow
    private static final TargetPredicate TARGET_PREDICATE_IGNORE_DISTANCE_SCALING = TargetPredicate.createNonAttackable().setBaseMaxDistance(FALLBACK_FOLLOW_RANGE).ignoreDistanceScalingFactor();
    @Shadow
    private static final TargetPredicate ATTACKABLE_TARGET_PREDICATE = TargetPredicate.createAttackable().setBaseMaxDistance(FALLBACK_FOLLOW_RANGE);
    @Shadow
    private static final TargetPredicate ATTACKABLE_TARGET_PREDICATE_IGNORE_DISTANCE_SCALING = TargetPredicate.createAttackable().setBaseMaxDistance(FALLBACK_FOLLOW_RANGE).ignoreDistanceScalingFactor();
    @Shadow
    private static final TargetPredicate ATTACKABLE_TARGET_PREDICATE_IGNORE_VISIBILITY = TargetPredicate.createAttackable().setBaseMaxDistance(FALLBACK_FOLLOW_RANGE).ignoreVisibility();
    @Shadow
    private static final TargetPredicate ATTACKABLE_TARGET_PREDICATE_IGNORE_VISIBILITY_OR_DISTANCE_SCALING = TargetPredicate.createAttackable().setBaseMaxDistance(FALLBACK_FOLLOW_RANGE).ignoreVisibility().ignoreDistanceScalingFactor(); 

    @Inject(at = @At("HEAD"), method = "testTargetPredicate")
    private static void targetPredWithFollowRange(LivingEntity entity, LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
        TARGET_PREDICATE.setBaseMaxDistance(entity.getAttributeValue(EntityAttributes.GENERIC_FOLLOW_RANGE));
        TARGET_PREDICATE_IGNORE_DISTANCE_SCALING.setBaseMaxDistance(entity.getAttributeValue(EntityAttributes.GENERIC_FOLLOW_RANGE));
    }

    @Inject(at = @At("HEAD"), method = "testAttackableTargetPredicate")
    private static void attackablePredWithFollowRange(LivingEntity entity, LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
        ATTACKABLE_TARGET_PREDICATE.setBaseMaxDistance(entity.getAttributeValue(EntityAttributes.GENERIC_FOLLOW_RANGE));
        ATTACKABLE_TARGET_PREDICATE_IGNORE_DISTANCE_SCALING.setBaseMaxDistance(entity.getAttributeValue(EntityAttributes.GENERIC_FOLLOW_RANGE));
    }

    @Inject(at = @At("HEAD"), method = "testAttackableTargetPredicateIgnoreVisibility")
    private static void attackableIgnoreVisibilityPredWithFollowRange(LivingEntity entity, LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
        ATTACKABLE_TARGET_PREDICATE_IGNORE_VISIBILITY.setBaseMaxDistance(entity.getAttributeValue(EntityAttributes.GENERIC_FOLLOW_RANGE));
        ATTACKABLE_TARGET_PREDICATE_IGNORE_VISIBILITY_OR_DISTANCE_SCALING.setBaseMaxDistance(entity.getAttributeValue(EntityAttributes.GENERIC_FOLLOW_RANGE));
    }
}
