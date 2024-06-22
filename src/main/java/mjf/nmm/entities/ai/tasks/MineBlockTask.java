package mjf.nmm.entities.ai.tasks;

import com.google.common.collect.ImmutableMap;

import mjf.nmm.entities.ai.sensors.CustomMemoryModuleType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

public class MineBlockTask<E extends LivingEntity> extends MultiTickTask<E> {
    protected BlockPos targetPos;
    protected long breakTime;
    protected long elapsedTime;
    protected long timeToBreak;
    public MineBlockTask() {
        super(ImmutableMap.of(CustomMemoryModuleType.MINE_BLOCK_LOCATION, MemoryModuleState.VALUE_PRESENT), 200);
    }

    @Override
    protected void run(ServerWorld world, E entity, long time) {
        this.elapsedTime = 0;
        this.targetPos = entity.getBrain().getOptionalMemory(CustomMemoryModuleType.MINE_BLOCK_LOCATION).get();

        float miningSpeed = entity.getMainHandStack().getItem().getMiningSpeedMultiplier(entity.getMainHandStack(), world.getBlockState(this.targetPos));
        float hardness = world.getBlockState(this.targetPos).getHardness(world, this.targetPos);
        if (hardness > 0) {
            this.timeToBreak = Math.round(30 * hardness / miningSpeed);
            this.breakTime = time + this.timeToBreak;
        } else {
            this.stop(world, entity, time);
        }
    }

    @Override
    protected void keepRunning(ServerWorld world, E entity, long time) {
        if (elapsedTime % 2 == 0)
            entity.swingHand(Hand.MAIN_HAND);
        if (this.elapsedTime % 4 == 0) {
            BlockSoundGroup soundGroup = world.getBlockState(this.targetPos).getSoundGroup();
            world.playSound(entity, this.targetPos, soundGroup.getHitSound(), SoundCategory.BLOCKS, (soundGroup.getVolume() + 1.0f) / 8.0f, soundGroup.getPitch() * 0.5f);
        }
        if (breakTime == time)
            world.breakBlock(this.targetPos, true, entity);
        ++this.elapsedTime;
        world.setBlockBreakingInfo(entity.getId(), this.targetPos, Math.round(10 * ((float)this.elapsedTime / this.timeToBreak)));
    }

    @Override
    protected boolean shouldKeepRunning(ServerWorld world, E entity, long time) {
        return this.breakTime >= time
            && entity.squaredDistanceTo(this.targetPos.getX(), this.targetPos.getY(), this.targetPos.getZ()) < 25.0
            && entity.hurtTime == 0;
    }

    @Override
    protected void finishRunning(ServerWorld world, E entity, long time) {
        world.setBlockBreakingInfo(entity.getId(), this.targetPos, -1);
        entity.getBrain().forget(CustomMemoryModuleType.MINE_BLOCK_LOCATION);
    }
}
