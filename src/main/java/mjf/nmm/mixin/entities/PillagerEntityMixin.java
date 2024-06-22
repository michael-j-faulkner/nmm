package mjf.nmm.mixin.entities;

import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import mjf.nmm.entities.interfaces.FireworkRocketAccessor;
import net.minecraft.entity.CrossbowUser;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.IllagerEntity;
import net.minecraft.entity.mob.PillagerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@Mixin(PillagerEntity.class)
public abstract class PillagerEntityMixin extends IllagerEntity implements CrossbowUser {
    protected PillagerEntityMixin(EntityType<? extends IllagerEntity> entityType, World world) {
        super(entityType, world);
    }

    @ModifyArg(method = "initGoals", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ai/goal/CrossbowAttackGoal;<init>(Lnet/minecraft/entity/mob/HostileEntity;DF)V"))
    protected float modifyRange(float range) {
        return 30.0f;
    }

    @Inject(at = @At("RETURN"), method = "createPillagerAttributes", cancellable = true)
	private static void editAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
		cir.setReturnValue(cir.getReturnValue()
			.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.45));
	}

    @Override
    public ItemStack getProjectileType(ItemStack stack) {
        NbtCompound fireworkNbt = new NbtCompound();
        fireworkNbt.putByte(FireworkRocketItem.FLIGHT_KEY, FireworkRocketItem.FLIGHT_VALUES[0]);
        NbtList explosions = new NbtList();
        NbtCompound explosion = new NbtCompound();
        explosion.putIntArray(FireworkRocketItem.COLORS_KEY, new int[]{(255 << 16) + (255 << 8) + (255)});
        explosion.putByte(FireworkRocketItem.TYPE_KEY, (byte)4);
        explosions.add(explosion);
        fireworkNbt.put(FireworkRocketItem.EXPLOSIONS_KEY, explosions);
        
        ItemStack firework = Items.FIREWORK_ROCKET.getDefaultStack();
        firework.getOrCreateNbt().put(FireworkRocketItem.FIREWORKS_KEY, fireworkNbt);
        return firework;
    }

    /**
     * Shoot fireworks flat
     * @author 
     * @reason 
     */
    @Overwrite
    public void shoot(LivingEntity target, ItemStack crossbow, ProjectileEntity projectile, float multiShotSpray) {
        double deltaX = target.getX() - this.getX();
        double deltaY = target.getBodyY(0.3333333333333333) - this.getY();
        double deltaZ = target.getZ() - this.getZ();
        double dist = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
        Vector3f vector3f = this.getProjectileLaunchVelocity(this, new Vec3d(deltaX, deltaY, deltaZ), multiShotSpray);
        projectile.setVelocity(vector3f.x(), vector3f.y(), vector3f.z(), 1.6f, 1.0f);
        this.playSound(SoundEvents.ITEM_CROSSBOW_SHOOT, 1.0f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
        ((FireworkRocketAccessor)projectile).setLifeTime((int) (0.6 * dist));
    }
}
