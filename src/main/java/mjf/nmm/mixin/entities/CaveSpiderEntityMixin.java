package mjf.nmm.mixin.entities;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import mjf.nmm.entities.ScalingDifficulty;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.CaveSpiderEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;

@Mixin(CaveSpiderEntity.class)
public abstract class CaveSpiderEntityMixin extends SpiderEntity {
    public CaveSpiderEntityMixin(EntityType<? extends SpiderEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "initialize", at = @At("RETURN"))
    public void initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, CallbackInfoReturnable<CaveSpiderEntity> cir) {
        Random random = world.getRandom();
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
    }
}
