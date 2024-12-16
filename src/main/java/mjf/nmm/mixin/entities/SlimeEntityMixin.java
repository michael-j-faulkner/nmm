package mjf.nmm.mixin.entities;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import mjf.nmm.entities.ScalingDifficulty;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;

@Mixin(SlimeEntity.class)
public abstract class SlimeEntityMixin extends MobEntity {
    protected SlimeEntityMixin(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
    }
    
    // @ModifyArg(method = "setSize", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/attribute/EntityAttributeInstance;setBaseValue(D)V", ordinal = 0))
    // private double setHealth(double health) {
    //     return 3 * health;
    // }

    @ModifyArg(method = "setSize", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/attribute/EntityAttributeInstance;setBaseValue(D)V", ordinal = 1))
    private double setSpeed(double speed) {
        return 1.2 * speed;
    }

    @ModifyArg(method = "setSize", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/attribute/EntityAttributeInstance;setBaseValue(D)V", ordinal = 2))
    private double setDamage(double damage) {
        return 3 * damage;
    }

    @Shadow
	public abstract void setSize(int size, boolean heal);

    @Inject(method = "initialize", at = @At("TAIL"))
    private void initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, CallbackInfoReturnable<EntityData> cir) {
		Random random = world.getRandom();
		int tier = 1 + (int)Math.round(6 * ScalingDifficulty.getPercentDifficulty(world, this.getPos()) + random.nextInt(6));

		this.setSize(tier, true);
        this.getAttributeInstance(EntityAttributes.FOLLOW_RANGE).setBaseValue(64.0);
	}
}
