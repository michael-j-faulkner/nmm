package mjf.nmm.mixin.entities;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mjf.nmm.entities.interfaces.FireworkRocketAccessor;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.mob.PillagerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.World.ExplosionSourceType;

@Mixin(FireworkRocketEntity.class)
public abstract class FireworkRocketEntityMixin extends ProjectileEntity implements FireworkRocketAccessor {
    public FireworkRocketEntityMixin(EntityType<? extends ProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Shadow
    private int lifeTime;

    public void setLifeTime(int lifeTime) {
        this.lifeTime = lifeTime;
    }

    private static final float PILLAGER_DAMAGE = 20.0f;

    @ModifyArg(method = "explode", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;F)Z"))
    private float scaleDamage(float damage) {
        if (this.getOwner() instanceof PillagerEntity)
            return PILLAGER_DAMAGE * damage / 7.0f;
        return damage;
    }

    @Inject(method = "explode", at = @At("HEAD"))
    private void explode(ServerWorld world, CallbackInfo ci) {
        if (this.getOwner() instanceof PiglinEntity)
            world.createExplosion(this, this.getX(), this.getY(), this.getZ(), 3, true, ExplosionSourceType.MOB);
    }
}
