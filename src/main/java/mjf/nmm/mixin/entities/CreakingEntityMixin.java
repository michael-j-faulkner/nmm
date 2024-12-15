package mjf.nmm.mixin.entities;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.CreakingEntity;

@Mixin(CreakingEntity.class)
public abstract class CreakingEntityMixin {
	@Inject(at = @At("RETURN"), method = "createCreakingAttributes", cancellable = true)
	private static void editAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
		cir.setReturnValue(cir.getReturnValue()
			.add(EntityAttributes.MOVEMENT_SPEED, 0.6)
            .add(EntityAttributes.ATTACK_DAMAGE, 256.0));
	}

    @ModifyConstant(method = "shouldBeUnrooted", constant = @Constant(doubleValue = 144.0))
    private static double changeDetectDistance(double d) {
        return 1024.0;
    }
}
