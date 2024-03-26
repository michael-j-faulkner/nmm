package mjf.nmm.mixin.player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Inject(method = "respawnPlayer", at = @At("RETURN"), cancellable = true)
    public void respawnPlayer(ServerPlayerEntity player, boolean alive, CallbackInfoReturnable<ServerPlayerEntity> cir) {
        ServerPlayerEntity finalPlayerEntity = cir.getReturnValue();
        finalPlayerEntity.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).addPersistentModifier(new EntityAttributeModifier("Previous Life", player.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH) - 20.0, EntityAttributeModifier.Operation.ADDITION));
        finalPlayerEntity.setHealth(finalPlayerEntity.getMaxHealth());
        cir.setReturnValue(finalPlayerEntity);
    }
}
