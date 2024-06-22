package mjf.nmm.mixin.entities;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.WitherSkeletonEntity;
import net.minecraft.entity.projectile.ExplosiveProjectileEntity;
import net.minecraft.entity.projectile.WitherSkullEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

@Mixin(WitherSkullEntity.class)
public abstract class WitherSkullEntityMixin extends ExplosiveProjectileEntity {
    protected WitherSkullEntityMixin(EntityType<? extends ExplosiveProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Shadow
    public abstract boolean isCharged();

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        if (this.getWorld() instanceof ServerWorld) {
            if (this.isCharged()) {
                this.getWorld().createExplosion((Entity)this, this.getX(), this.getY(), this.getZ(), 5.0f, true, World.ExplosionSourceType.MOB);
                WitherSkeletonEntity witherSkeleton = EntityType.WITHER_SKELETON.create(this.getWorld());
                if (witherSkeleton != null) {
                    witherSkeleton.updatePositionAndAngles(this.getX(), this.getY(), this.getZ(), 360 * this.getWorld().getRandom().nextFloat(), 0);
                    witherSkeleton.initialize((ServerWorld)this.getWorld(), this.getWorld().getLocalDifficulty(this.getBlockPos()), SpawnReason.NATURAL, null, null);
                    this.getWorld().spawnEntity(witherSkeleton);
                }
            }
            else {
                this.getWorld().createExplosion((Entity)this, this.getX(), this.getY(), this.getZ(), 2.0f, false, World.ExplosionSourceType.MOB);
            }
            this.discard();
        }
    }
}
