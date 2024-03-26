package mjf.nmm.mixin.entities;

import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.common.collect.ImmutableList;

import mjf.nmm.entities.ScalingDifficulty;
import mjf.nmm.entities.ai.sensors.CustomMemoryModuleType;
import mjf.nmm.entities.ai.sensors.CustomSensorType;
import mjf.nmm.entities.interfaces.FireworkRocketAccessor;
import net.minecraft.entity.CrossbowUser;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.AbstractPiglinEntity;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@Mixin(PiglinEntity.class)
public abstract class PiglinEntityMixin extends AbstractPiglinEntity implements CrossbowUser {
    public PiglinEntityMixin(EntityType<? extends AbstractPiglinEntity> entityType, World world) {
        super(entityType, world);
    }

    // Switch to use the follow range sensors
    @Shadow
    protected static final ImmutableList<SensorType<? extends Sensor<? super PiglinEntity>>> SENSOR_TYPES = ImmutableList.of(
        CustomSensorType.FOLLOW_RANGE_ENTITIES, CustomSensorType.FOLLOW_RANGE_PLAYERS, 
        SensorType.NEAREST_ITEMS, SensorType.HURT_BY, SensorType.PIGLIN_SPECIFIC_SENSOR);
    @Shadow
    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_MODULE_TYPES = ImmutableList.of(
        MemoryModuleType.LOOK_TARGET, MemoryModuleType.DOORS_TO_CLOSE, MemoryModuleType.MOBS,
        MemoryModuleType.VISIBLE_MOBS, MemoryModuleType.NEAREST_VISIBLE_PLAYER,
        MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS,
        MemoryModuleType.NEARBY_ADULT_PIGLINS, MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, 
        MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS, MemoryModuleType.HURT_BY, MemoryModuleType.HURT_BY_ENTITY,
        MemoryModuleType.WALK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
        MemoryModuleType.ATTACK_TARGET, MemoryModuleType.ATTACK_COOLING_DOWN, 
        MemoryModuleType.INTERACTION_TARGET, MemoryModuleType.PATH, 
        MemoryModuleType.ANGRY_AT, MemoryModuleType.UNIVERSAL_ANGER, 
        MemoryModuleType.AVOID_TARGET, MemoryModuleType.ADMIRING_ITEM, 
        MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM, 
        MemoryModuleType.ADMIRING_DISABLED, MemoryModuleType.DISABLE_WALK_TO_ADMIRE_ITEM, 
        MemoryModuleType.CELEBRATE_LOCATION, MemoryModuleType.DANCING, 
        MemoryModuleType.HUNTED_RECENTLY, MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN, 
        MemoryModuleType.NEAREST_VISIBLE_NEMESIS, MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, 
        MemoryModuleType.RIDE_TARGET, MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, 
        MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT, MemoryModuleType.NEAREST_VISIBLE_HUNTABLE_HOGLIN, 
        MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD, 
        MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM, MemoryModuleType.ATE_RECENTLY, 
        MemoryModuleType.NEAREST_REPELLENT,
        CustomMemoryModuleType.NEAREST_TARGETABLE_PLAYER, CustomMemoryModuleType.NEAREST_TARGETABLE_PLAYERS);

    @Inject(at = @At("RETURN"), method = "createPiglinAttributes", cancellable = true)
	private static void editAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
		cir.setReturnValue(cir.getReturnValue()
			.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3));
	}

    private static final float ARROW_SPEED = 1.6f;
    private static final double GRAV = 0.06; // Acutal is 0.08, but roughly accounting for drag 0.0784;
    // Constants for trajectory equation
    private static final double SPEED_SQUARED = ARROW_SPEED * ARROW_SPEED;
    private static final double C1 = SPEED_SQUARED / GRAV;
    private static final double C2 = 2.0 * GRAV / SPEED_SQUARED;
    private static final double C3 = (GRAV * GRAV) / (SPEED_SQUARED * SPEED_SQUARED);

    private double calcYVel(double deltaY, double horDistSqrd) {
        return C1 * (1 - Math.sqrt(Math.max(0, 1 - C2 * deltaY - C3 * horDistSqrd)));
    }

    @Override
    public void shoot(LivingEntity target, ItemStack crossbow, ProjectileEntity projectile, float multiShotSpray) {
        double deltaX = target.getX() - projectile.getX();
        double deltaY = target.getBodyY(0.3333333333333333) - projectile.getY();
        double deltaZ = target.getZ() - projectile.getZ();
        double horDistSqrd = deltaX * deltaX + deltaZ * deltaZ;
        Vector3f vector3f = this.getProjectileLaunchVelocity(this, new Vec3d(deltaX, this.calcYVel(deltaY, horDistSqrd), deltaZ), multiShotSpray);
        projectile.setVelocity(vector3f.x(), vector3f.y(), vector3f.z(), ARROW_SPEED, 1.0f);
        this.playSound(SoundEvents.ITEM_CROSSBOW_SHOOT, 1.0f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));

        if (this.getWorld() instanceof ServerWorld && projectile instanceof PersistentProjectileEntity) {
            double percentDifficulty = ScalingDifficulty.getPercentDifficulty((ServerWorld)this.getWorld(), this.getPos());
            ((PersistentProjectileEntity)projectile).setDamage(2.0 * (1.0 + 4 * percentDifficulty));
        }
    }
}
