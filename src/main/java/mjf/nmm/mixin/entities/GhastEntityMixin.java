package mjf.nmm.mixin.entities;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.GhastEntity;

@Mixin(GhastEntity.class)
public class GhastEntityMixin {
    /**
     * @author
     * @reason
     */
    @Overwrite
    public int getFireballStrength() {
        return 4;
    }

    @Inject(at = @At("RETURN"), method = "createGhastAttributes", cancellable = true)
	private static void editAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
		cir.setReturnValue(cir.getReturnValue()
			.add(EntityAttributes.GENERIC_ARMOR, 20.0)
			.add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0));
	}
}
