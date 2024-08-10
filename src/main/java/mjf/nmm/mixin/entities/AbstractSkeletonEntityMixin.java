package mjf.nmm.mixin.entities;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.BowAttackGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

@Mixin(AbstractSkeletonEntity.class)
public abstract class AbstractSkeletonEntityMixin extends HostileEntity {
    protected AbstractSkeletonEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(at = @At("RETURN"), method = "createAbstractSkeletonAttributes", cancellable = true)
    private static void editAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
		cir.setReturnValue(cir.getReturnValue()
			.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.33));
	}

    @Shadow
    private final BowAttackGoal<AbstractSkeletonEntity> bowAttackGoal = new BowAttackGoal<AbstractSkeletonEntity>((AbstractSkeletonEntity)(Object)this, 1.0, 20, 30.0f);

    @Shadow
    protected abstract PersistentProjectileEntity createArrowProjectile(ItemStack arrow, float damageModifier, @Nullable ItemStack shotFrom);

    // Adjustable Parameters
    private static final double ARROW_SPEED = 2.5;
    private static final double ARROW_DEVIATION = 0.1;

    // Based on minecraft's values
    private static final double DRAG_COEFFICIENT = 0.99;
    private static final double G = 0.05;
    private static final double C1 = 1 - DRAG_COEFFICIENT;

    /**
     * Use better math for shooting arrows
     * @author
     * @reason
     */
    @Overwrite 
    public void shootAt(LivingEntity target, float pullProgress) {
        ItemStack bow = this.getStackInHand(ProjectileUtil.getHandPossiblyHolding(this, Items.BOW));
		ItemStack arrow = this.getProjectileType(bow);
		PersistentProjectileEntity persistentProjectileEntity = this.createArrowProjectile(arrow, pullProgress, bow);

        // Use better math
        double deltaX = target.getX() - this.getX();
        double deltaZ = target.getZ() - this.getZ();
        double deltaY = target.getBodyY(0.8) - persistentProjectileEntity.getY();

        double ticksToReachTarget = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ) / ARROW_SPEED;
        double C2 = 1 - Math.pow(DRAG_COEFFICIENT, ticksToReachTarget);
        double C3 = C1 / C2;
        double C4 = G / C1 * (ticksToReachTarget - C2 / C1);

        double xVel = C3 * deltaX + this.getRandom().nextTriangular(0.0, ARROW_DEVIATION);
        double zVel = C3 * deltaZ + this.getRandom().nextTriangular(0.0, ARROW_DEVIATION);
        double yVel = C3 * (deltaY + C4) + this.getRandom().nextTriangular(0.0, ARROW_DEVIATION);
        
        persistentProjectileEntity.setVelocity(xVel, yVel, zVel);
        persistentProjectileEntity.setDamage(2.0);
        this.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
        this.getWorld().spawnEntity(persistentProjectileEntity);
    }
}
