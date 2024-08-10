package mjf.nmm.mixin.entities;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import mjf.nmm.NightmareMode;
import mjf.nmm.entities.ScalingDifficulty;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow
    public abstract double getAttributeValue(RegistryEntry<EntityAttribute> attribute);


    private static double HEALTH_PER_APPLE = 4.0;
    private static double MAX_HEALTH = 60.0;
    
    @Inject(method = "eatFood", at = @At("HEAD"))
    public void eatFood(World world, ItemStack stack, FoodComponent foodComponent, CallbackInfoReturnable<ItemStack> cir) {
        LivingEntity entity = (LivingEntity)(Object)this;
        double currentModifierAmount = this.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH) - 20.0;
        if (stack.isOf(Items.GOLDEN_APPLE) && currentModifierAmount < 0.0) {
            entity.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).overwritePersistentModifier(new EntityAttributeModifier(ScalingDifficulty.PERMANENT_DAMAGE_IDENTIFIER, Math.min(currentModifierAmount + HEALTH_PER_APPLE, 0.0), EntityAttributeModifier.Operation.ADD_VALUE));
        }
        if (stack.isOf(Items.ENCHANTED_GOLDEN_APPLE)) {
            entity.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).overwritePersistentModifier(new EntityAttributeModifier(ScalingDifficulty.PERMANENT_DAMAGE_IDENTIFIER, Math.min(currentModifierAmount + HEALTH_PER_APPLE, MAX_HEALTH - 20.0), EntityAttributeModifier.Operation.ADD_VALUE));
        }
    } 

    
    @Shadow
    public abstract boolean clearStatusEffects();
    @Shadow
    public abstract ItemStack getStackInHand(Hand hand);
    @Shadow
    public abstract void setHealth(float health);
    @Shadow
    public abstract boolean addStatusEffect(StatusEffectInstance effect);

    /**
     * @author
     * @reason
     */
    @Overwrite
    private boolean tryUseTotem(DamageSource source) {
		if (source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
			return false;
		} else {
			ItemStack itemStack = null;

			for (Hand hand : Hand.values()) {
				ItemStack itemStack2 = this.getStackInHand(hand);
				if (itemStack2.isOf(Items.TOTEM_OF_UNDYING)) {
					itemStack = itemStack2.copy();
					itemStack2.decrement(1);
					break;
				}
			}

            if ((Object) this instanceof PlayerEntity player && itemStack == null) {
                int slot = player.getInventory().getSlotWithStack(Items.TOTEM_OF_UNDYING.getDefaultStack());
                if (slot != -1) {
                    itemStack = player.getInventory().getStack(slot);
                    player.getInventory().removeOne(itemStack);
                }
            }

            if (itemStack != null) {
                if ((Object) this instanceof ServerPlayerEntity serverPlayer) {
                    serverPlayer.incrementStat(Stats.USED.getOrCreateStat(Items.TOTEM_OF_UNDYING));
                    Criteria.USED_TOTEM.trigger(serverPlayer, itemStack);
                    this.emitGameEvent(GameEvent.ITEM_INTERACT_FINISH);
                }
                
                this.setHealth(1.0F);
                this.clearStatusEffects();
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 1200, 1));
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 1200, 4));
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 1200, 0));
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 1200, 1));
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 1200, 1));
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 1200, 1));
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 1200, 1));
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 1200, 1));
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 1200, 1));
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 1200, 0));
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.WATER_BREATHING, 1200, 0));
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION, 20, 0));
                this.getWorld().sendEntityStatus(this, EntityStatuses.USE_TOTEM_OF_UNDYING);
            }

			return itemStack != null;
		}
	}
}
