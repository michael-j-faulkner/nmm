package mjf.nmm.mixin.items;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import mjf.nmm.entities.ScalingDifficulty;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;

@Mixin(Item.class)
public abstract class ItemMixin {
    private static double HEALTH_PER_APPLE = 4.0;
    private static double MAX_HEALTH = 60.0;

    @Inject(method = "finishUsing", at = @At("HEAD"))
    public void finishUsing(ItemStack stack, World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        double currentModifierAmount = user.getAttributeValue(EntityAttributes.MAX_HEALTH) - 20.0;
        if (stack.isOf(Items.GOLDEN_APPLE) && currentModifierAmount < 0.0) {
            user.getAttributeInstance(EntityAttributes.MAX_HEALTH).overwritePersistentModifier(new EntityAttributeModifier(ScalingDifficulty.PERMANENT_DAMAGE_IDENTIFIER, Math.min(currentModifierAmount + HEALTH_PER_APPLE, 0.0), EntityAttributeModifier.Operation.ADD_VALUE));
        }
        if (stack.isOf(Items.ENCHANTED_GOLDEN_APPLE)) {
            user.getAttributeInstance(EntityAttributes.MAX_HEALTH).overwritePersistentModifier(new EntityAttributeModifier(ScalingDifficulty.PERMANENT_DAMAGE_IDENTIFIER, Math.min(currentModifierAmount + HEALTH_PER_APPLE, MAX_HEALTH - 20.0), EntityAttributeModifier.Operation.ADD_VALUE));
        }
    }
}
