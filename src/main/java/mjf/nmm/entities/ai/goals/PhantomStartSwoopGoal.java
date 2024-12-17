package mjf.nmm.entities.ai.goals;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;

public class PhantomStartSwoopGoal extends Goal {
    private int cooldown;
    private PhantomEntity phantomEntity;
    // Basically never lose interest once they're initially spotted
    private final TargetPredicate TARGET_PREDICATE = TargetPredicate.createAttackable().ignoreVisibility().setBaseMaxDistance(128.0);

    public PhantomStartSwoopGoal(PhantomEntity phantomEntity) {
        this.phantomEntity = phantomEntity;
    }

    @Override
    public boolean canStart() {
        LivingEntity livingEntity = this.phantomEntity.getTarget();
        return livingEntity != null
            ? TARGET_PREDICATE.test(castToServerWorld(this.phantomEntity.getWorld()), this.phantomEntity, livingEntity)
            : false;
    }

    @Override
    public void start() {
        this.cooldown = this.getTickCount(10);
        this.phantomEntity.movementType = PhantomEntity.PhantomMovementType.CIRCLE;
        this.startSwoop();
    }

    @Override
    public void stop() {
        this.phantomEntity.circlingCenter = this.phantomEntity.getWorld()
            .getTopPosition(Heightmap.Type.MOTION_BLOCKING, this.phantomEntity.circlingCenter)
            .up(10 + this.phantomEntity.getRandom().nextInt(20));
    }

    @Override
    public void tick() {
        if (this.phantomEntity.movementType == PhantomEntity.PhantomMovementType.CIRCLE) {
            this.cooldown--;
            if (this.cooldown <= 0) {
                this.phantomEntity.movementType = PhantomEntity.PhantomMovementType.SWOOP;
                this.startSwoop();
                this.cooldown = this.getTickCount((8 + this.phantomEntity.getRandom().nextInt(4)) * 20);
                this.phantomEntity.playSound(SoundEvents.ENTITY_PHANTOM_SWOOP, 10.0F, 0.95F + this.phantomEntity.getRandom().nextFloat() * 0.1F);
            }
        }
    }

    private void startSwoop() {
        this.phantomEntity.circlingCenter = this.phantomEntity.getTarget().getBlockPos().up(20 + this.phantomEntity.getRandom().nextInt(20));
        if (this.phantomEntity.circlingCenter.getY() < this.phantomEntity.getWorld().getSeaLevel()) {
            this.phantomEntity.circlingCenter = new BlockPos(
                this.phantomEntity.circlingCenter.getX(), this.phantomEntity.getWorld().getSeaLevel() + 1, this.phantomEntity.circlingCenter.getZ()
            );
        }
    }
}
