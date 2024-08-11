package mjf.nmm.mixin.entities;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.BlazeEntity;
import net.minecraft.entity.projectile.AbstractFireballEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.world.World;

@Mixin(SmallFireballEntity.class)
public abstract class SmallFireballEntityMixin extends AbstractFireballEntity {
    public SmallFireballEntityMixin(EntityType<? extends AbstractFireballEntity> entityType, World world) {
        super(entityType, world);
    }

    @ModifyArg(method = "onEntityHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
    public float modifyDamage(float damage) {
        return 15.0f;
    }

    @Inject(method = "onEntityHit", at = @At("TAIL"))
    public void addExplosion(CallbackInfo ci) {
        if (!this.getWorld().isClient() && this.getOwner() instanceof BlazeEntity) {
            this.getWorld().createExplosion(this, this.getX(), this.getY(), this.getZ(), 2.0f, true, World.ExplosionSourceType.MOB);
		}
    }

    @Inject(method = "onBlockHit", at = @At("TAIL"))
    public void addBlockExplosion(CallbackInfo ci) {
        if (!this.getWorld().isClient() && this.getOwner() instanceof BlazeEntity) {
            this.getWorld().createExplosion(this, this.getX(), this.getY(), this.getZ(), 2.0f, true, World.ExplosionSourceType.MOB);
		}
    }
}
