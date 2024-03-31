package mjf.nmm.mixin.entities;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.WitchEntity;

@Mixin(WitchEntity.class)
public class WitchEntityMixin {
    @Inject(at = @At("RETURN"), method = "createWitchAttributes", cancellable = true)
	private static void editAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
		cir.setReturnValue(cir.getReturnValue()
            .add(EntityAttributes.GENERIC_ARMOR, 20.0)
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.35));
	}
}
