package mjf.nmm.mixin.enderdragon;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.entity.boss.dragon.EnderDragonEntity;

@Mixin(EnderDragonEntity.class)
public class EnderDragonEntityMixin {
    @ModifyArg(method = "damageLivingEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
    public float setDragonDamageAmount(float f) {
        return 30.0f;
    }
}
