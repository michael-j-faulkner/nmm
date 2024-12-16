package mjf.nmm.mixin.entities;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.RavagerEntity;

@Mixin(RavagerEntity.class)
public class RavagerEntityMixin {
    @Inject(at = @At("RETURN"), method = "createRavagerAttributes", cancellable = true)
	private static void editAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
		cir.setReturnValue(cir.getReturnValue()
			.add(EntityAttributes.MOVEMENT_SPEED, 0.45)
            .add(EntityAttributes.ATTACK_DAMAGE, 20.0)
			.add(EntityAttributes.ATTACK_KNOCKBACK, 4.0));
	}
}
