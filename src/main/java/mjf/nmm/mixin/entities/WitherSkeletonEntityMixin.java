package mjf.nmm.mixin.entities;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import mjf.nmm.entities.ScalingDifficulty;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.WitherSkeletonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;

@Mixin(WitherSkeletonEntity.class)
public abstract class WitherSkeletonEntityMixin extends AbstractSkeletonEntity {
    protected WitherSkeletonEntityMixin(EntityType<? extends AbstractSkeletonEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public boolean canTarget(EntityType<?> type) {
        return super.canTarget(type) && type != EntityType.WITHER;
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        EntityData resultingEntityData = super.initialize(world, difficulty, spawnReason, entityData);
        this.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE).setBaseValue(20.0);
        this.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED).setBaseValue(0.35);
        this.updateAttackType();
        return resultingEntityData;
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void initEquipment(Random random, LocalDifficulty localDifficulty) {
        super.initEquipment(random, localDifficulty);
        this.equipStack(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        double percentDifficulty = ScalingDifficulty.getPercentDifficulty((ServerWorld)this.getWorld(), this.getPos());
        switch (random.nextInt(2) + Math.round(2.0f * (float)percentDifficulty)) {
        case 0: default:
            this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));
            break;
        case 1:
            this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
            break;
        case 2:
            this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SWORD));
            break;
        case 3:
            this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.NETHERITE_SWORD));
            break;
        }
    }

    @Override
	public boolean damage(ServerWorld world, DamageSource source, float amount) {
        if (super.damage(world, source, amount)) {
            for (int x = -1; x <= 1; ++x)
                for (int z = -1; z <= 1; ++z)
                    for (int y = 0; y < 4; ++y)
                        if (world.getBlockState(this.getBlockPos().add(x, y, z)).getHardness(world, this.getBlockPos()) >= 0.0)
                            world.breakBlock(this.getBlockPos().add(x, y, z), false);

            if (source.getAttacker() instanceof PlayerEntity playerEntity) {
                playerEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 100));
            }
            
            for (int i = 0; i < 16; ++i) {
                double x = this.getX() + (this.getRandom().nextDouble() - 0.5) * 32.0;
                double y = this.getY() + (this.getRandom().nextDouble() - 0.5) * 32.0;
                double z = this.getZ() + (this.getRandom().nextDouble() - 0.5) * 32.0;

                world.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_WITHER_AMBIENT, SoundCategory.HOSTILE, 1.0f, 1.8f);
                if (this.teleport(x, y, z, false)) {
                    world.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_WITHER_AMBIENT, SoundCategory.HOSTILE, 1.0f, 1.8f);
                    break;
                }
            }
            return true;
        }
        return false;
    }

    private static boolean isEmpty(BlockView world, BlockPos pos) {
        return world.getBlockState(pos).canPathfindThrough(NavigationType.LAND);
    }

    @Override
    public void tick() {
        if (!this.getWorld().isClient) {
            ServerWorld  world = this.getServer().getWorld(this.getWorld().getRegistryKey());
            for (BlockPos direction : List.of(this.getBlockPos().north(), this.getBlockPos().east(), this.getBlockPos().south(), this.getBlockPos().west())) {
                for (int i = 0; i < 3; ++i)
                    if (isEmpty(world, direction.up(i))) {
                        world.breakBlock(direction.up(i), true);
                    }
            }
        }
        super.tick();
    }
}
