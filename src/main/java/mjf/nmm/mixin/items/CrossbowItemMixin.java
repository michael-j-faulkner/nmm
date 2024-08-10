package mjf.nmm.mixin.items;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mjf.nmm.entities.interfaces.FireworkRocketAccessor;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.util.math.Vec3d;

@Mixin(CrossbowItem.class)
public class CrossbowItemMixin {
    /**
     * @author
     * @reason
     */
    @Overwrite
    public int getRange() {
        return 24;
    }

    // Adjustable Parameters
    private static final double ARROW_SPEED = 2.5;
    private static final double ARROW_DEVIATION = 0.1;
    private static final float FIREWORK_ROCKET_SPEED = 1.6f;
    private static final float FIREWORK_DEVIATION = 0.1f;

    // Based on minecraft's values
    private static final double DRAG_COEFFICIENT = 0.99;
    private static final double G = 0.05;
    private static final double C1 = 1 - DRAG_COEFFICIENT;


    @Inject(method = "shoot", at = @At("TAIL"))
    protected void shoot(LivingEntity shooter, ProjectileEntity projectile, int index, float speed, float divergence, float yaw, @Nullable LivingEntity target, CallbackInfo ci) {
        if (target != null) {
            // Use better math
            double deltaX = target.getX() - projectile.getX();
            double deltaY = target.getBodyY(0.8) - projectile.getY();
            double deltaZ = target.getZ() - projectile.getZ();
            double dist = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);

            if (projectile instanceof FireworkRocketEntity) {
                Vec3d vel = new Vec3d(deltaX, deltaY, deltaZ).normalize().multiply(FIREWORK_ROCKET_SPEED)
                    .add(shooter.getRandom().nextTriangular(0.0, FIREWORK_DEVIATION),
                        shooter.getRandom().nextTriangular(0.0, FIREWORK_DEVIATION),
                        shooter.getRandom().nextTriangular(0.0, FIREWORK_DEVIATION));
                
                projectile.setVelocity(vel);
                // Make the firwork explode just before reaching the user
                ((FireworkRocketAccessor) projectile).setLifeTime((int) ((dist - 3.0) / FIREWORK_ROCKET_SPEED));
            } else {
                double ticksToReachTarget = dist / ARROW_SPEED;
                double C2 = 1 - Math.pow(DRAG_COEFFICIENT, ticksToReachTarget);
                double C3 = C1 / C2;
                double C4 = G / C1 * (ticksToReachTarget - C2 / C1);
    
                double xVel = C3 * deltaX + shooter.getRandom().nextTriangular(0.0, ARROW_DEVIATION);
                double zVel = C3 * deltaZ + shooter.getRandom().nextTriangular(0.0, ARROW_DEVIATION);
                double yVel = C3 * (deltaY + C4) + shooter.getRandom().nextTriangular(0.0, ARROW_DEVIATION);
                
                projectile.setVelocity(xVel, yVel, zVel);
            }
        }
    }
}
