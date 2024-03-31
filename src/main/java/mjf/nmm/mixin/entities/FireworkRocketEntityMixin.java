package mjf.nmm.mixin.entities;

import org.apache.logging.log4j.core.jmx.Server;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import mjf.nmm.entities.ScalingDifficulty;
import mjf.nmm.entities.interfaces.FireworkRocketAccessor;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.PillagerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

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

    @ModifyArg(method = "explode", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
    private float scaleDamage(float damage) {
        if (this.getWorld() instanceof ServerWorld && this.getOwner() instanceof PillagerEntity)
            return 2.0f * damage;
        return damage;
    }
}
