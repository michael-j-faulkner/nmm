package mjf.nmm.mixin.entities;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import mjf.nmm.entities.ScalingDifficulty;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.BowAttackGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;

@Mixin(AbstractSkeletonEntity.class)
public abstract class AbstractSkeletonEntityMixin extends HostileEntity {
    protected AbstractSkeletonEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(at = @At("RETURN"), method = "createAbstractSkeletonAttributes", cancellable = true)
    private static void editAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
		cir.setReturnValue(cir.getReturnValue()
			.add(EntityAttributes.MOVEMENT_SPEED, 0.33));
	}

    @Inject(method = "initEquipment", at = @At("TAIL"))
    protected void initEquipment(Random random, LocalDifficulty localDifficulty, CallbackInfo ci) {
        double percentDifficulty = ScalingDifficulty.getPercentDifficulty(this.getServer().getWorld(this.getWorld().getRegistryKey()), this.getPos());
        if (random.nextFloat() < percentDifficulty) {
            ItemStack arrow = Items.TIPPED_ARROW.getDefaultStack();
            switch (random.nextInt(1 + (int)Math.round(6 * percentDifficulty))) {
            case 0: default:
                arrow.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Optional.empty(), Optional.empty(), 
                List.of(new StatusEffectInstance(StatusEffects.NAUSEA, 160, 4)), Optional.empty()));
                break;
            case 1:
                arrow.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Optional.empty(), Optional.empty(), 
                List.of(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 160, 4)), Optional.empty()));
                break;
            case 2:
                arrow.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Optional.empty(), Optional.empty(), 
                List.of(new StatusEffectInstance(StatusEffects.SLOWNESS, 160, 1)), Optional.empty()));
                break;
            case 3:
                arrow.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Optional.empty(), Optional.empty(), 
                List.of(new StatusEffectInstance(StatusEffects.WEAKNESS, 160, 1)), Optional.empty()));
                break;
            case 4:
                arrow.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Optional.empty(), Optional.empty(), 
                List.of(new StatusEffectInstance(StatusEffects.BLINDNESS, 160, 0)), Optional.empty()));
                break;
            case 5:
                arrow.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Optional.empty(), Optional.empty(), 
                List.of(new StatusEffectInstance(StatusEffects.POISON, 160, 1)), Optional.empty()));
                break;
            case 6:
                arrow.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Optional.empty(), Optional.empty(), 
                List.of(new StatusEffectInstance(StatusEffects.INSTANT_DAMAGE, 1, 1)), Optional.empty()));
                break;
            }
            this.equipStack(EquipmentSlot.OFFHAND, arrow);
        } 
    }

    @Shadow
    private final BowAttackGoal<AbstractSkeletonEntity> bowAttackGoal = new BowAttackGoal<AbstractSkeletonEntity>((AbstractSkeletonEntity)(Object)this, 1.0, 20, 30.0f);

    @Shadow
    protected abstract PersistentProjectileEntity createArrowProjectile(ItemStack arrow, float damageModifier, @Nullable ItemStack shotFrom);

    // Adjustable Parameters
    private static final float ARROW_SPEED = 2.5f;
    private static final float ARROW_DEVIATION = 0.1f;

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

        double xVel = C3 * deltaX; // + this.getRandom().nextTriangular(0.0, ARROW_DEVIATION);
        double zVel = C3 * deltaZ; // + this.getRandom().nextTriangular(0.0, ARROW_DEVIATION);
        double yVel = C3 * (deltaY + C4); // + this.getRandom().nextTriangular(0.0, ARROW_DEVIATION);
        
        persistentProjectileEntity.setVelocity(xVel, yVel, zVel, ARROW_SPEED, ARROW_DEVIATION);
        persistentProjectileEntity.setDamage(2.0);
        this.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
        this.getWorld().spawnEntity(persistentProjectileEntity);
    }
}
