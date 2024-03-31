package mjf.nmm.mixin.entities;

import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.LivingTargetCache;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.OcelotEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.serialization.Dynamic;

import mjf.nmm.entities.ScalingDifficulty;
import mjf.nmm.entities.ai.CreeperBrain;

@Mixin(CreeperEntity.class)
public abstract class CreeperEntityMixin extends HostileEntity {
	protected CreeperEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(at = @At("RETURN"), method = "createCreeperAttributes", cancellable = true)
	private static void editAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
		cir.setReturnValue(cir.getReturnValue()
			.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.35));
	}

	@Shadow
    private int explosionRadius;
	@Shadow
	private static TrackedData<Boolean> CHARGED;

	public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
		double percentDifficulty = ScalingDifficulty.getPercentDifficulty(world, this.getPos());
		if (random.nextFloat() < 0.1 * percentDifficulty) {
			this.dataTracker.set(CHARGED, true);
		}
        if (random.nextFloat() < 0.1 * percentDifficulty) {
            this.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, -1));
        }
        if (random.nextFloat() < 0.1 * percentDifficulty) {
            this.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, -1));
        }
        if (random.nextFloat() < 0.1 * percentDifficulty) {
            this.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, -1));
        }
        if (random.nextFloat() < 0.1 * percentDifficulty) {
            this.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, -1, 3));
        }
		this.explosionRadius = 3;
		return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
    }

	/**
	 * Delete normal creeper ai
     * @reason 
     * @author
	 */
	@Overwrite
	public void initGoals() {
	}

	protected Brain.Profile<CreeperEntity> createBrainProfile() {
        return Brain.createProfile(CreeperBrain.MEMORY_MODULES, CreeperBrain.SENSORS);
    }

	@Override
    protected Brain<?> deserializeBrain(Dynamic<?> dynamic) {
        return CreeperBrain.create((CreeperEntity) (Object) this, (Brain<CreeperEntity>) this.createBrainProfile().deserialize(dynamic));
    }

	@SuppressWarnings("unchecked")
	public Brain<CreeperEntity> getBrain() {
        return (Brain<CreeperEntity>) super.getBrain();
    }

	protected int lookForCatTimer = 0;

    protected void mobTick() {
		Brain<CreeperEntity> brain = this.getBrain();
        brain.tick((ServerWorld)this.getWorld(), (CreeperEntity) (Object) this);
		CreeperBrain.updateActivities(this.getBrain());
		super.mobTick();

		if (this.lookForCatTimer > 0) {
			--this.lookForCatTimer;
			return;
		}
		Optional<LivingTargetCache> visibleMobs = brain.getOptionalMemory(MemoryModuleType.VISIBLE_MOBS);
		if (visibleMobs.isPresent()) {
			brain.remember(MemoryModuleType.AVOID_TARGET, visibleMobs.get().findFirst(entity -> entity instanceof CatEntity || entity instanceof OcelotEntity));
		}
		this.lookForCatTimer = 20;
	}
}