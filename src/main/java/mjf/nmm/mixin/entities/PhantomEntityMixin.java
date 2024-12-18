package mjf.nmm.mixin.entities;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import mjf.nmm.entities.ScalingDifficulty;
import mjf.nmm.entities.ai.goals.PhantomFindTargetGoal;
import mjf.nmm.entities.ai.goals.PhantomStartSwoopGoal;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.FlyingEntity;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionTypes;

@Mixin(PhantomEntity.class)
public abstract class PhantomEntityMixin extends FlyingEntity {
    protected PhantomEntityMixin(EntityType<? extends FlyingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Shadow
    BlockPos circlingCenter;
    
    @Inject(method = "initialize", at = @At("TAIL"))
	public void initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, CallbackInfoReturnable<EntityData> cir) {
        double percentDifficulty = ScalingDifficulty.getPercentDifficulty(world, this.getPos());
        if (world.getDimension().equals(world.getRegistryManager().getOrThrow(RegistryKeys.DIMENSION_TYPE).get(DimensionTypes.THE_END))) {
            this.setPhantomSize(5 + (int) (percentDifficulty * (5 + world.getRandom().nextInt(6))));
        } else {
            this.setPhantomSize((int) (percentDifficulty * (3 + world.getRandom().nextInt(3))));
        }
        this.circlingCenter = this.getBlockPos().up(48);
    }

    @Shadow
    public abstract void setPhantomSize(int size);
    @Shadow
    public abstract int getPhantomSize();

    @Inject(method = "onSizeChanged", at = @At("TAIL"))
    public void increaseDamage(CallbackInfo ci) {
        this.getAttributeInstance(EntityAttributes.ATTACK_KNOCKBACK).setBaseValue(5.0);
        this.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE).setBaseValue(6.0 + 2.0 * this.getPhantomSize());
        this.getAttributeInstance(EntityAttributes.MAX_HEALTH).setBaseValue(20.0 + 3.0 * this.getPhantomSize());
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void startNoClip(CallbackInfo ci) {
        this.noClip = true;
    }


    @Inject(method = "tick", at = @At("TAIL"))
    public void endNoClip(CallbackInfo ci) {
        this.noClip = false;
    }

    @ModifyArg(method = "initGoals", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ai/goal/GoalSelector;add(ILnet/minecraft/entity/ai/goal/Goal;)V", ordinal = 0))
    private Goal seeThroughWalls1(Goal goal) {
        return new PhantomStartSwoopGoal((PhantomEntity) (Object) this);
    }

    @ModifyArg(method = "initGoals", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ai/goal/GoalSelector;add(ILnet/minecraft/entity/ai/goal/Goal;)V", ordinal = 3))
    private Goal seeThroughWalls2(Goal goal) {
        return new PhantomFindTargetGoal((PhantomEntity) (Object) this);
    }
}
