package mjf.nmm.mixin.entities;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.BreezeEntity;
import net.minecraft.entity.projectile.AbstractWindChargeEntity;
import net.minecraft.entity.projectile.BreezeWindChargeEntity;
import net.minecraft.entity.projectile.WindChargeEntity;
import net.minecraft.world.World;

@Mixin(BreezeWindChargeEntity.class)
public abstract class BreezeWindChargeEntityMixin extends AbstractWindChargeEntity {
    public BreezeWindChargeEntityMixin(EntityType<? extends AbstractWindChargeEntity> entityType, World world) {
        super(entityType, world);
    }

    @ModifyArg(
        method = "createExplosion", 
        at = @At(
            value = "INVOKE", 
            target = 
                "Lnet/minecraft/world/World;createExplosion(" +
                    "Lnet/minecraft/entity/Entity;" + 
                    "Lnet/minecraft/entity/damage/DamageSource;" + 
                    "Lnet/minecraft/world/explosion/ExplosionBehavior;" + 
                    "DDDFZ" + 
                    "Lnet/minecraft/world/World$ExplosionSourceType;" + 
                    "Lnet/minecraft/particle/ParticleEffect;" + 
                    "Lnet/minecraft/particle/ParticleEffect;" + 
                    "Lnet/minecraft/registry/entry/RegistryEntry;)" + 
                    "Lnet/minecraft/world/explosion/Explosion;"
        )
    )
    public float modifyPower(float power) {
        return 10.0f;
    }
}
