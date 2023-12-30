package mjf.nmm.mixin.entities;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import mjf.nmm.entities.ScalingDifficulty;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.Targeter;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends LivingEntity implements Targeter {
    protected MobEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(at = @At("RETURN"), method = "initialize")
    private void initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt, CallbackInfoReturnable<EntityData> ci) {
        double percentDifficulty = ScalingDifficulty.getPercentDifficulty(world, this.getPos());
        if (this.getAttributes().hasAttribute(EntityAttributes.GENERIC_MAX_HEALTH)) {
            this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).addPersistentModifier(new EntityAttributeModifier("Scaling Difficulty Bonus", 2.0 * percentDifficulty, EntityAttributeModifier.Operation.MULTIPLY_BASE));
            this.setHealth(this.getMaxHealth());
        }
        if (this.getAttributes().hasAttribute(EntityAttributes.GENERIC_ARMOR))
            this.getAttributeInstance(EntityAttributes.GENERIC_ARMOR).addPersistentModifier(new EntityAttributeModifier("Scaling Difficulty Bonus", 10.0 * percentDifficulty, EntityAttributeModifier.Operation.ADDITION));
        if (this.getAttributes().hasAttribute(EntityAttributes.GENERIC_ARMOR_TOUGHNESS))
            this.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS).addPersistentModifier(new EntityAttributeModifier("Scaling Difficulty Bonus", 8.0 * percentDifficulty, EntityAttributeModifier.Operation.ADDITION));
        if (this.getAttributes().hasAttribute(EntityAttributes.GENERIC_ATTACK_DAMAGE))
            this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).addPersistentModifier(new EntityAttributeModifier("Scaling Difficulty Bonus", 3.0 * percentDifficulty, EntityAttributeModifier.Operation.MULTIPLY_BASE));
        if (this.getAttributes().hasAttribute(EntityAttributes.GENERIC_MOVEMENT_SPEED))
            this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).addPersistentModifier(new EntityAttributeModifier("Scaling Difficulty Bonus", 0.5 * percentDifficulty, EntityAttributeModifier.Operation.MULTIPLY_BASE));
        if (this.getAttributes().hasAttribute(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE))
            this.getAttributeInstance(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE).addPersistentModifier(new EntityAttributeModifier("Scaling Difficulty Bonus", 0.5 * percentDifficulty, EntityAttributeModifier.Operation.ADDITION));
    }
}
