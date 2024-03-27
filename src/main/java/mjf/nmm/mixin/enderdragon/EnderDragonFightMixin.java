package mjf.nmm.mixin.enderdragon;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonFight;

@Mixin(EnderDragonFight.class)
public class EnderDragonFightMixin {
    @Shadow
    private boolean previouslyKilled;

    @Inject(method = "dragonKilled", at = @At("TAIL"))
    private void respawnEgg(EnderDragonEntity dragon, CallbackInfo ci) {
        this.previouslyKilled = false;
    }
}
