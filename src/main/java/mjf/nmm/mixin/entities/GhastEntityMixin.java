package mjf.nmm.mixin.entities;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.FlyingEntity;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

@Mixin(GhastEntity.class)
public abstract class GhastEntityMixin extends FlyingEntity implements Monster {
    protected GhastEntityMixin(EntityType<? extends FlyingEntity> entityType, World world) {
        super(entityType, world);
    }
    
    /**
     * @author
     * @reason
     */
    @Overwrite
    public int getFireballStrength() {
        return 6;
    }

    @Inject(at = @At("RETURN"), method = "createGhastAttributes", cancellable = true)
	private static void editAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
		cir.setReturnValue(cir.getReturnValue()
			.add(EntityAttributes.ARMOR, 20.0)
			.add(EntityAttributes.ARMOR_TOUGHNESS, 8.0)
			.add(EntityAttributes.MAX_HEALTH, 20.0)
            .add(EntityAttributes.FOLLOW_RANGE, 128.0));
	}

    @ModifyArg(method = "initGoals", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ai/goal/GoalSelector;add(ILnet/minecraft/entity/ai/goal/Goal;)V", ordinal = 3))
    private Goal betterGhastDetection(Goal originalGoal) {
        return new ActiveTargetGoal<>(this, PlayerEntity.class, 10, true, false, null);
    }
}
