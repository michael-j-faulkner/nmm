package mjf.nmm.entities.ai.tasks;

import org.apache.commons.lang3.mutable.MutableInt;

import mjf.nmm.entities.ai.sensors.CustomMemoryModuleType;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

public class PlaceBlockTask {


    public static <E extends MobEntity> Task<E> create() {
        return TaskTriggerer.task(context -> {
            MutableInt cooldown = new MutableInt(0);
            return context.group(context.queryMemoryValue(CustomMemoryModuleType.PLACE_BLOCK_LOCATION)).apply(context, (placeBlockLocation) ->
                (world, entity, time) -> {
                    if (cooldown.getAndDecrement() > 0 || !entity.isOnGround()) {
                        return false;
                    }
                    cooldown.setValue(2);
                    entity.swingHand(Hand.MAIN_HAND);
                    BlockPos pos = context.getValue(placeBlockLocation);
                    if (world.getBlockState(pos).canPathfindThrough(NavigationType.LAND)) {
                        // world.setBlockState(pos, Blocks.DIRT.getDefaultState());
                        // world.playSound(entity, pos, SoundEvents.BLOCK_GRAVEL_PLACE, SoundCategory.BLOCKS, 1.0f, 0.8f);
                        world.setBlockState(pos, Blocks.COBBLESTONE.getDefaultState());
                        world.playSound(entity, pos, SoundEvents.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 1.0f, 0.8f);
                        placeBlockLocation.forget();
                        return true;
                    }
                    return false;
                });
        });
    }
}
