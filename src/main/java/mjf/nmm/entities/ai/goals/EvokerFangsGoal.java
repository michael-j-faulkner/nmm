package mjf.nmm.entities.ai.goals;

import mjf.nmm.entities.interfaces.EvokerEntityAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.EvokerEntity;
import net.minecraft.entity.mob.EvokerFangsEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.event.GameEvent;

// Slightly modified Evoker Fangs Goal
public class EvokerFangsGoal extends Goal {
    protected int spellCooldown;
    protected int startTime;
    protected EvokerEntity evoker;
    
    public EvokerFangsGoal(EvokerEntity evoker) {
        this.evoker = evoker;
    }

    @Override
    public boolean canStart() {
        LivingEntity livingEntity = this.evoker.getTarget();
        if (livingEntity == null || !livingEntity.isAlive()) {
            return false;
        }
        if (this.evoker.isSpellcasting()) {
            return false;
        }
        return this.evoker.age >= this.startTime;
    }

    @Override
    public boolean shouldContinue() {
        LivingEntity livingEntity = this.evoker.getTarget();
        return livingEntity != null && livingEntity.isAlive() && this.spellCooldown > 0;
    }

    @Override
    public void start() {
        this.spellCooldown = this.getTickCount(this.getInitialCooldown());
        ((EvokerEntityAccessor)this.evoker).setSpellTicks(this.getSpellTicks());
        this.startTime = this.evoker.age + this.startTimeDelay();
        SoundEvent soundEvent = this.getSoundPrepare();
        if (soundEvent != null) {
            this.evoker.playSound(soundEvent, 1.0f, 1.0f);
        }
        ((EvokerEntityAccessor)this.evoker).setSpellToFangs();
    }

    @Override
    public void tick() {
        --this.spellCooldown;
        if (this.spellCooldown == 0) {
            this.castSpell();
            this.evoker.playSound(SoundEvents.ENTITY_EVOKER_CAST_SPELL, 1.0f, 1.0f);
        }
    }

    protected int getInitialCooldown() {
        return 5;
    }

    protected int getSpellTicks() {
        return 5;
    }

    protected int startTimeDelay() {
        return 30;
    }

    protected void conjureCicle(LivingEntity target, float theta, double minY, double maxY) {
        for (int i = 0; i < 20; ++i) {
            float r = this.evoker.getRandom().nextFloat() * 5;
            float phi = theta + i * (float)Math.PI * 2.0f / 8.0f;
            this.conjureFangs(target.getX() + r * MathHelper.cos(phi), target.getZ() + r * MathHelper.sin(phi), minY, maxY, phi, this.evoker.getRandom().nextInt(20));
        }
    }


    protected void castSpell() {
        LivingEntity target = this.evoker.getTarget();
        double minY = Math.min(target.getY(), this.evoker.getY()) - 1.0;
        double maxY = Math.max(target.getY(), this.evoker.getY()) + 1.0;
        float theta = (float)MathHelper.atan2(target.getZ() - this.evoker.getZ(), target.getX() - this.evoker.getX());
        if (this.evoker.squaredDistanceTo(target) < 25.0) {
            for (int i = 0; i < 5; ++i) {
                float phi = theta + i * (float)Math.PI * 0.4f;
                this.conjureFangs(this.evoker.getX() + MathHelper.cos(phi) * 1.5, this.evoker.getZ() + MathHelper.sin(phi) * 1.5, minY, maxY, phi, 1);
            }
            for (int i = 0; i < 8; ++i) {
                float phi = theta + i * (float)Math.PI * 2.0f / 8.0f + 1.2566371f;
                this.conjureFangs(this.evoker.getX() + MathHelper.cos(phi) * 2.5, this.evoker.getZ() + MathHelper.sin(phi) * 2.5, minY, maxY, phi, 2);
            }
            for (int i = 0; i < 13; ++i) {
                float phi = theta + i * (float)Math.PI * 0.153f;
                this.conjureFangs(this.evoker.getX() + MathHelper.cos(phi) * 3.5, this.evoker.getZ() + MathHelper.sin(phi) * 3.5, minY, maxY, phi, 3);
            }
        } else if (this.evoker.squaredDistanceTo(target) < 1024.0) {
            switch (this.evoker.getRandom().nextInt(2)) {
            case 0:
                for (int i = 0; i < 32; ++i) {
                    double h = 1.25 * (double)(i + 1);
                    int j = 1 * i;
                    this.conjureFangs(this.evoker.getX() + (double)MathHelper.cos(theta) * h, this.evoker.getZ() + (double)MathHelper.sin(theta) * h, minY, maxY, theta, j);
                }
                break;
            case 1:
                this.conjureCicle(target, theta, minY, maxY);
            }
        } else {
            this.conjureCicle(target, theta, minY, maxY);
        }
    }

    private void conjureFangs(double x, double z, double minY, double maxY, float yaw, int warmup) {
        BlockPos blockPos = BlockPos.ofFloored(x, maxY, z);
        boolean shouldSpawn = false;
        double yOffset = 0.0;
        do {
            BlockPos floorPos = blockPos.down();
            BlockState floorBlockState = this.evoker.getWorld().getBlockState(floorPos);
            if (!floorBlockState.isSideSolidFullSquare(this.evoker.getWorld(), floorPos, Direction.UP)) 
                continue;

            VoxelShape voxelShape = this.evoker.getWorld().getBlockState(blockPos).getCollisionShape(this.evoker.getWorld(), blockPos);
            if (!this.evoker.getWorld().isAir(blockPos) && !voxelShape.isEmpty()) {
                yOffset = voxelShape.getMax(Direction.Axis.Y);
                shouldSpawn = true;
                break;
            }
        } while ((blockPos = blockPos.down()).getY() >= MathHelper.floor(minY) - 1);
        if (shouldSpawn) {
            this.evoker.getWorld().spawnEntity(new EvokerFangsEntity(this.evoker.getWorld(), x, blockPos.getY() + yOffset, z, yaw, warmup, this.evoker));
            this.evoker.getWorld().emitGameEvent(GameEvent.ENTITY_PLACE, new Vec3d(x, blockPos.getY() + yOffset, z), GameEvent.Emitter.of(this.evoker));
        }
    }

    // Was originally suppossed to come from EvokerEntity.getSoundPrepare()
    protected SoundEvent getSoundPrepare() {
        return SoundEvents.ENTITY_EVOKER_PREPARE_ATTACK;
    }
}
