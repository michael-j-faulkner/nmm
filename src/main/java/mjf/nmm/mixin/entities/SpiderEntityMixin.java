package mjf.nmm.mixin.entities;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import mjf.nmm.entities.ScalingDifficulty;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.mob.SpiderEntity.SpiderData;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;

@Mixin(SpiderEntity.class)
public abstract class SpiderEntityMixin extends HostileEntity {
    protected SpiderEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(at = @At("RETURN"), method = "createSpiderAttributes", cancellable = true)
	private static void editAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
		cir.setReturnValue(cir.getReturnValue()
			.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.4)
            .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3.0));
	}
    
    @Override
    public boolean tryAttack(Entity target) {
        if (super.tryAttack(target)) {
            if (this.getWorld().getBlockState(target.getBlockPos()).getHardness(target.getWorld(), target.getBlockPos()) >= 0.0f 
                    && !this.getWorld().getBlockState(this.getBlockPos()).isOf(Blocks.COBWEB)) {
                this.getWorld().breakBlock(target.getBlockPos(), true);
                this.getWorld().setBlockState(target.getBlockPos(), Blocks.COBWEB.getDefaultState());
            }
            return true;
        }
        return false;
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
        entityData = super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
        Random random = world.getRandom();

        SkeletonEntity skeletonEntity;
        if (random.nextInt(100) == 0 && (skeletonEntity = EntityType.SKELETON.create(this.getWorld())) != null) {
            skeletonEntity.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), this.getYaw(), 0.0f);
            skeletonEntity.initialize(world, difficulty, spawnReason, null, null);
            skeletonEntity.startRiding(this);
        }
        double percentDifficulty = ScalingDifficulty.getPercentDifficulty(world, this.getPos());
        if (random.nextFloat() < percentDifficulty) {
            this.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, -1));
        }
        if (random.nextFloat() < 0.5 * percentDifficulty) {
            this.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, -1, Math.round((float)percentDifficulty)));
        }
        if (random.nextFloat() < 0.5 * percentDifficulty) {
            this.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, -1, Math.round(2.0f * (float)percentDifficulty)));
        }
        if (random.nextFloat() < 0.5 * percentDifficulty) {
            this.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, -1));
        }
        return entityData;
    }
}
