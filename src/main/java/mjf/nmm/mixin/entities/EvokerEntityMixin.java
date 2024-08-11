package mjf.nmm.mixin.entities;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import mjf.nmm.entities.ai.goals.EvokerFangsGoal;
import mjf.nmm.entities.interfaces.EvokerEntityAccessor;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.mob.EvokerEntity;
import net.minecraft.entity.mob.SpellcastingIllagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

@Mixin(EvokerEntity.class)
public abstract class EvokerEntityMixin extends SpellcastingIllagerEntity implements EvokerEntityAccessor {
    protected EvokerEntityMixin(EntityType<? extends SpellcastingIllagerEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(at = @At("RETURN"), method = "createEvokerAttributes", cancellable = true)
	private static void editAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
		cir.setReturnValue(cir.getReturnValue()
			.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 64.0)
            .add(EntityAttributes.GENERIC_ARMOR, 20.0)
            .add(EntityAttributes.GENERIC_ARMOR_TOUGHNESS, 10.0)
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.45));
	}

    @Override
    public void setSpellTicks(int spellTicks) {
        this.spellTicks = spellTicks;
    }

    public void setSpellToFangs() {
        this.setSpell(Spell.FANGS);
    }

    @Redirect(method = "initGoals", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ai/goal/GoalSelector;add(ILnet/minecraft/entity/ai/goal/Goal;)V", ordinal = 4))
    private void changeFangsGoal(GoalSelector selector, int priority, Goal originalFangsGoal) {
        selector.add(priority, new EvokerFangsGoal((EvokerEntity)(Object)this));
    }

    @ModifyArg(method = "initGoals", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ai/goal/GoalSelector;add(ILnet/minecraft/entity/ai/goal/Goal;)V", ordinal = 2))
    private Goal modifyFleeGoal(Goal goal) {
        return new FleeEntityGoal<PlayerEntity>(this, PlayerEntity.class, 16.0F, 0.8, 1.0);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource damageSource) {
        return super.isInvulnerableTo(damageSource) || damageSource.isOf(DamageTypes.ARROW);
    }
}
