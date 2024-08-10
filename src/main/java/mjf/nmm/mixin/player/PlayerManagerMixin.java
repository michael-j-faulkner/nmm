package mjf.nmm.mixin.player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import mjf.nmm.NightmareMode;
import mjf.nmm.entities.ScalingDifficulty;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Inject(method = "respawnPlayer", at = @At("RETURN"), cancellable = true)
    public void respawnPlayer(ServerPlayerEntity player, boolean alive, Entity.RemovalReason removalReason, CallbackInfoReturnable<ServerPlayerEntity> cir) {
        ServerPlayerEntity finalPlayerEntity = cir.getReturnValue();
        finalPlayerEntity.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).overwritePersistentModifier(new EntityAttributeModifier(ScalingDifficulty.PERMANENT_DAMAGE_IDENTIFIER, player.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH) - 20.0, EntityAttributeModifier.Operation.ADD_VALUE));
        finalPlayerEntity.setHealth(finalPlayerEntity.getMaxHealth());
        cir.setReturnValue(finalPlayerEntity);
    }
}
