package mjf.nmm.mixin.entities;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FireworkExplosionComponent;
import net.minecraft.component.type.FireworksComponent;
import net.minecraft.entity.CrossbowUser;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.IllagerEntity;
import net.minecraft.entity.mob.PillagerEntity;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;

@Mixin(PillagerEntity.class)
public abstract class PillagerEntityMixin extends IllagerEntity implements CrossbowUser {
    protected PillagerEntityMixin(EntityType<? extends IllagerEntity> entityType, World world) {
        super(entityType, world);
    }

    @ModifyArg(method = "initGoals", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ai/goal/CrossbowAttackGoal;<init>(Lnet/minecraft/entity/mob/HostileEntity;DF)V"))
    protected float modifyRange(float range) {
        return 30.0f;
    }
    
    @ModifyArg(method = "initGoals", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/raid/RaiderEntity$PatrolApproachGoal;<init>(Lnet/minecraft/entity/raid/RaiderEntity;Lnet/minecraft/entity/mob/IllagerEntity;F)V"))
    private float patrolAggroRange(float f) {
        return 64.0f;
    }

    @Inject(at = @At("RETURN"), method = "createPillagerAttributes", cancellable = true)
	private static void editAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
		cir.setReturnValue(cir.getReturnValue()
			.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.45)
            .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 64.0));
	}

    @Override
    public ItemStack getProjectileType(ItemStack stack) {
        ItemStack firework = Items.FIREWORK_ROCKET.getDefaultStack();
        FireworksComponent fireworksData = new FireworksComponent(
            FireworkRocketItem.FLIGHT_VALUES[0], 
            List.of(
                new FireworkExplosionComponent(
                    FireworkExplosionComponent.Type.BURST,
                    IntList.of(
                        (this.getRandom().nextInt(256) << 16) + 
                        (this.getRandom().nextInt(256) << 8) +
                        (this.getRandom().nextInt(256))), 
                    IntList.of(), 
                    true, 
                    true)
            )
        );
        firework.set(DataComponentTypes.FIREWORKS, fireworksData);
        return firework;
    }

}
