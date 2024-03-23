package mjf.nmm.mixin.entities;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import mjf.nmm.NightmareMode;
import mjf.nmm.entities.ScalingDifficulty;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.BowAttackGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;

@Mixin(AbstractSkeletonEntity.class)
public abstract class AbstractSkeletonEntityMixin extends HostileEntity {
    protected AbstractSkeletonEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }
    
    private double arrowDamage = 2.0;

    @Inject(at = @At("RETURN"), method = "createAbstractSkeletonAttributes", cancellable = true)
    private static void editAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
		cir.setReturnValue(cir.getReturnValue()
			.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3));
	}

    @Inject(at = @At("RETURN"), method = "initialize")
    private void initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt, CallbackInfoReturnable<EntityData> ci) {
        double percentDifficulty = ScalingDifficulty.getPercentDifficulty(world, this.getPos());
        this.updateArrowSpeed((float)(percentDifficulty + 1.5));
        this.arrowDamage = 2.0 * (1.0 + 3 * percentDifficulty);
    }

    private void updateArrowSpeed(float arrowSpeed) {
        this.ARROW_SPEED = arrowSpeed;
        this.SPEED_SQUARED = ARROW_SPEED * ARROW_SPEED;
        this.C1 = SPEED_SQUARED / GRAV;
        this.C2 = 2.0 * GRAV / SPEED_SQUARED;
        this.C3 = (GRAV * GRAV) / (SPEED_SQUARED * SPEED_SQUARED);
    }

    @Shadow
    private final BowAttackGoal<AbstractSkeletonEntity> bowAttackGoal = new BowAttackGoal<AbstractSkeletonEntity>((AbstractSkeletonEntity)(Object)this, 1.0, 20, 30.0f);

    @Shadow
    protected abstract PersistentProjectileEntity createArrowProjectile(ItemStack arrow, float damageModifier);

    private float ARROW_SPEED = 1.0f;
    private static final double GRAV = 0.068; // Acutal is 0.08, but roughly accounting for drag 0.0784;
    // Constants for trajectory equation
    private double SPEED_SQUARED = ARROW_SPEED * ARROW_SPEED;
    private double C1 = SPEED_SQUARED / GRAV;
    private double C2 = 2.0 * GRAV / SPEED_SQUARED;
    private double C3 = (GRAV * GRAV) / (SPEED_SQUARED * SPEED_SQUARED);

    private double calcYVel(double deltaY, double horDistSqrd) {
        return this.C1 * (1 - Math.sqrt(Math.max(0, 1 - this.C2 * deltaY - this.C3 * horDistSqrd)));
    }

    /**
     * Use better math for shooting arrows
     * @author
     * @reason
     */
    @Overwrite 
    public void shootAt(LivingEntity target, float pullProgress) {
        ItemStack itemStack = this.getProjectileType(this.getStackInHand(ProjectileUtil.getHandPossiblyHolding(this, Items.BOW)));
        PersistentProjectileEntity persistentProjectileEntity = this.createArrowProjectile(itemStack, pullProgress);

        // Use better math
        double deltaX = target.getX() - this.getX();
        double deltaZ = target.getZ() - this.getZ();
        double horDistSqrd = deltaX * deltaX + deltaZ * deltaZ;

        double deltaY = target.getBodyY(0.3333333333333333) - persistentProjectileEntity.getY();
        double yVel = this.calcYVel(deltaY, horDistSqrd);

        persistentProjectileEntity.setVelocity(deltaX, yVel, deltaZ, this.ARROW_SPEED, 0.0f);
        persistentProjectileEntity.setDamage(this.arrowDamage);
        this.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
        this.getWorld().spawnEntity(persistentProjectileEntity);
    }
}
