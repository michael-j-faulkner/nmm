package mjf.nmm.mixin.player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

import mjf.nmm.NightmareMode;
import mjf.nmm.entities.ScalingDifficulty;
import mjf.nmm.tags.Tags;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(at = @At("TAIL"), method = "applyDamage")
    private void permanentDamage(DamageSource source, float amount, CallbackInfo ci, @Local(name = "amount") float finalAmount) {
        if (source.isIn(Tags.PERMANENT_DAMAGE)) {
            double currentModifierAmount = this.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH) - 20.0;
            this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).overwritePersistentModifier(new EntityAttributeModifier(ScalingDifficulty.PERMANENT_DAMAGE_IDENTIFIER, currentModifierAmount - finalAmount, EntityAttributeModifier.Operation.ADD_VALUE));
        }
    }
}
