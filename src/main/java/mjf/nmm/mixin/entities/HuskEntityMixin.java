package mjf.nmm.mixin.entities;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HuskEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.world.World;

@Mixin(HuskEntity.class)
public abstract class HuskEntityMixin extends ZombieEntity {
    public HuskEntityMixin(EntityType<? extends ZombieEntity> entityType, World world) {
        super(entityType, world);
    }

    /**
     * Remove local difficulty and apply weakness instead of hunger
     * @author
     * @reason
     */
    @Overwrite
    public boolean tryAttack(Entity target) {
        boolean attackSucceeded = super.tryAttack(target);
        if (attackSucceeded && target instanceof LivingEntity) {
            ((LivingEntity)target).addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 140), this);
        }
        return attackSucceeded;
    }
}
