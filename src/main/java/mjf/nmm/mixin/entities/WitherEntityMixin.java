package mjf.nmm.mixin.entities;

import java.util.List;
import java.util.function.Predicate;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.FuzzyPositions;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.WitherSkeletonEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.ModifiableWorld;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;

@Mixin(WitherEntity.class)
public abstract class WitherEntityMixin extends HostileEntity {
    protected WitherEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Shadow
    public abstract int getInvulnerableTimer();
    @Shadow
    public abstract void setInvulTimer(int ticks);
    @Shadow
    public abstract int getTrackedEntityId(int headIndex);
    @Shadow
    public abstract void setTrackedEntityId(int headIndex, int id);
    @Shadow
    protected abstract void shootSkullAt(int headIndex, double targetX, double targetY, double targetZ, boolean charged);
    @Shadow
    protected abstract void shootSkullAt(int headIndex, LivingEntity target);

    @Shadow
    private ServerBossBar bossBar;
    @Shadow
    private final int[] skullCooldowns = new int[2];
    @Shadow
    private final int[] chargedSkullCooldowns = new int[2];
    @Shadow
    private int blockBreakingCooldown;
    @Shadow
    private static final Predicate<LivingEntity> CAN_ATTACK_PREDICATE = entity -> entity.getGroup() != EntityGroup.UNDEAD && entity.isMobOrPlayer();
    @Shadow
    private static final TargetPredicate HEAD_TARGET_PREDICATE = TargetPredicate.createAttackable().setBaseMaxDistance(32.0).setPredicate(CAN_ATTACK_PREDICATE).ignoreVisibility();

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void mobTick() {
        // Prespawn logic
        if (this.getInvulnerableTimer() > 0) {
            int timeRemaining = this.getInvulnerableTimer() - 1;
            this.bossBar.setPercent(1.0f - (float)timeRemaining / 220.0f);
            if (timeRemaining <= 0) {
                this.getWorld().createExplosion((Entity)this, this.getX(), this.getEyeY(), this.getZ(), 7.0f, true, World.ExplosionSourceType.MOB);
                if (!this.isSilent()) {
                    this.getWorld().syncGlobalEvent(WorldEvents.WITHER_SPAWNS, this.getBlockPos(), 0);
                }
                for (int i = 0; i < 4; ++i) {
                    WitherSkeletonEntity witherSkeleton = EntityType.WITHER_SKELETON.create(this.getWorld());
                    if (witherSkeleton != null && this.getWorld() instanceof ServerWorld) {
                        witherSkeleton.updatePositionAndAngles(this.getX(), this.getY(), this.getZ(), 360 * this.getRandom().nextFloat(), 0);
                        witherSkeleton.initialize((ServerWorld)this.getWorld(), this.getWorld().getLocalDifficulty(this.getBlockPos()), SpawnReason.NATURAL, null, null);
                        this.getWorld().spawnEntity(witherSkeleton);
                    }
                }
            }
            this.setInvulTimer(timeRemaining);
            if (this.age % 10 == 0) {
                this.heal(10.0f);
            }
            return;
        }

        // Postspawn logic
        super.mobTick();
        for (int i = 0; i < 2; ++i) {
            if (this.age < this.skullCooldowns[i]) 
                continue;
            this.skullCooldowns[i] = this.age + 10 + this.random.nextInt(10);

            // Charged Skull Logic
            this.chargedSkullCooldowns[i] = this.chargedSkullCooldowns[i] + 1;
            if (this.chargedSkullCooldowns[i] > 15) {
                double x = MathHelper.nextDouble(this.random, this.getX() - 32.0, this.getX() + 32.0);
                double y = MathHelper.nextDouble(this.random, this.getY() - 16.0, this.getY() + 16.0);
                double z = MathHelper.nextDouble(this.random, this.getZ() - 32.0, this.getZ() + 32.0);
                this.shootSkullAt(i + 1, x, y, z, true);
                this.chargedSkullCooldowns[i] = 0;
            }

            // Regular Skull Logic
            int skullTargetId;
            if ((skullTargetId = this.getTrackedEntityId(i + 1)) > 0) {
                LivingEntity target = (LivingEntity)this.getWorld().getEntityById(skullTargetId);
                if (target == null || !this.canTarget(target) || this.squaredDistanceTo(target) > 1024.0 || !this.canSee(target)) {
                    this.setTrackedEntityId(i, 0);
                    continue;
                }
                this.shootSkullAt(i + 1, target);
                this.skullCooldowns[i] = this.age + 40 + this.random.nextInt(20);
                // this.chargedSkullCooldowns[i] = 0;
                continue;
            }

            // Update Targets
            List<LivingEntity> list = this.getWorld().getTargets(LivingEntity.class, HEAD_TARGET_PREDICATE, this, this.getBoundingBox().expand(32.0, 16.0, 32.0));
            if (list.isEmpty()) continue;
            LivingEntity newTarget = list.get(this.random.nextInt(list.size()));
            this.setTrackedEntityId(i, newTarget.getId());
        }

        // Update main head's target
        if (this.getTarget() != null) {
            this.setTrackedEntityId(0, this.getTarget().getId());
        } else {
            this.setTrackedEntityId(0, 0);
        }

        // Break Blocks
        if (this.blockBreakingCooldown > 0) {
            --this.blockBreakingCooldown;
            if (this.blockBreakingCooldown == 0 && this.getWorld().getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
                int x = MathHelper.floor(this.getX());
                int y = MathHelper.floor(this.getY());
                int z = MathHelper.floor(this.getZ());
                boolean successfullyBrokeBlock = false;
                for (int i = -1; i <= 1; ++i) {
                    for (int j = -1; j <= 3; ++j) {
                        for (int k = -1; k <= 1; ++k) {
                            BlockPos blockPos = new BlockPos(x + i, y + j, z + k);
                            if (!WitherEntity.canDestroy(this.getWorld().getBlockState(blockPos))) 
                                continue;
                            if (this.getWorld().breakBlock(blockPos, true, this))
                                successfullyBrokeBlock = true;
                        }
                    }
                }
                if (successfullyBrokeBlock) {
                    this.getWorld().syncWorldEvent(null, WorldEvents.WITHER_BREAKS_BLOCK, this.getBlockPos(), 0);
                }
            }
        }

        // Update Health
        if (this.age % 20 == 0) {
            this.heal(1.0f);
        }
        this.bossBar.setPercent(this.getHealth() / this.getMaxHealth());
    }
}
