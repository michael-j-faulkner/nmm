package mjf.nmm.mixin.ai;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.DrownedEntity;
import net.minecraft.util.math.Vec3d;

@Mixin(targets = "net.minecraft.entity.mob.DrownedEntity$DrownedMoveControl")
public class DrownedMoveControlMixin {
    private static final double SPEED_MULTIPLIER = 5.0; 

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Vec3d;add(DDD)Lnet/minecraft/util/math/Vec3d;", ordinal = 1))
    private Vec3d increaseSwimSpeed(Vec3d vel, double x, double y, double z) {
        return vel.add(SPEED_MULTIPLIER * x, SPEED_MULTIPLIER * y, SPEED_MULTIPLIER * z);
    }
}
