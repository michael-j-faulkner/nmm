package mjf.nmm.entities.ai.goals;

import java.util.Comparator;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class PhantomFindTargetGoal extends Goal {
    private final TargetPredicate TARGET_PREDICATE = TargetPredicate.createAttackable().setBaseMaxDistance(64.0); // .ignoreVisibility()
    private int delay = toGoalTicks(20);

    private PhantomEntity phantomEntity;

    public PhantomFindTargetGoal(PhantomEntity phantomEntity) {
        this.phantomEntity = phantomEntity;
    }

    @Override
    public boolean canStart() {
        if (this.delay > 0) {
            this.delay--;
            return false;
        } else {
            this.delay = toGoalTicks(60);
            ServerWorld serverWorld = castToServerWorld(this.phantomEntity.getWorld());
            List<PlayerEntity> list = serverWorld.getPlayers(TARGET_PREDICATE, this.phantomEntity, this.phantomEntity.getBoundingBox().expand(32, 64, 32));
            if (!list.isEmpty()) {
                list.sort(Comparator.comparing(Entity::getY).reversed());
                this.phantomEntity.setTarget(list.getFirst());
                return true;
            }

            return false;
        }
    }

    @Override
    public boolean shouldContinue() {
        LivingEntity livingEntity = this.phantomEntity.getTarget();
        return livingEntity != null
            ? TARGET_PREDICATE.test(this.phantomEntity.getServer().getWorld(this.phantomEntity.getWorld().getRegistryKey()), this.phantomEntity, livingEntity)
            : false;
    }
}
