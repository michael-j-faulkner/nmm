package mjf.nmm.mixin.enderdragon;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.phase.AbstractPhase;
import net.minecraft.entity.boss.dragon.phase.HoldingPatternPhase;
import net.minecraft.entity.boss.dragon.phase.PhaseType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.feature.EndPortalFeature;

@Mixin(HoldingPatternPhase.class)
public abstract class HoldingPatternPhaseMixin extends AbstractPhase {
    public HoldingPatternPhaseMixin(EnderDragonEntity dragon) {
        super(dragon);
    }

    @Shadow
    private static TargetPredicate PLAYERS_IN_RANGE_PREDICATE;
    @Shadow
    private Path path;
    @Shadow
    private Vec3d pathTarget;

    private boolean patrolClockwise = false;

    @Shadow
    protected abstract void strafePlayer(PlayerEntity player);
    @Shadow
    protected abstract void followPath();

    // Note also in Charging Phase
    @Override
    public float getMaxYAcceleration() {
        return 40.0f;
    }

    // Note also in Charging Phase
    @Override
    public float getYawAcceleration() {
        return 2.0f * super.getMaxYAcceleration();
    }

    @Redirect(method = "serverTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/boss/dragon/phase/HoldingPatternPhase;tickInRange(Lnet/minecraft/server/world/ServerWorld;)V"))
    private void tick(HoldingPatternPhase phase, ServerWorld world) {
        // Check if we should change phases
        if (this.path != null && this.path.isFinished()) {
            BlockPos origin = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, new BlockPos(EndPortalFeature.offsetOrigin(this.dragon.getFightOrigin())));
            int remainingCrystals = this.dragon.getFight() == null ? 0 : this.dragon.getFight().getAliveEndCrystals();
            PlayerEntity nearestPlayer = world.getClosestPlayer((double)origin.getX(), (double)origin.getY(), (double)origin.getZ(), 256.0, target -> PLAYERS_IN_RANGE_PREDICATE.test(world, this.dragon, (PlayerEntity)target));
            if (nearestPlayer != null && this.dragon.getRandom().nextInt(11 - remainingCrystals) == 0) {
                switch (this.dragon.getRandom().nextInt(2 + remainingCrystals)) {
                    case 0: 
                        this.strafePlayer(nearestPlayer);
                        return;
                    default:
                        if (this.dragon.getRandom().nextInt(2) > 0) {
                            this.dragon.getPhaseManager().setPhase(PhaseType.CHARGING_PLAYER);
                            this.dragon.getPhaseManager().create(PhaseType.CHARGING_PLAYER).setPathTarget(nearestPlayer.getPos());
                            return;
                        }
                        break;
                }
            }
        }

        // Continue Phase
        if (this.path == null || this.path.isFinished()) {
            int currentPathNode = this.dragon.getNearestPathNodeIndex();
            int targetPathNode = currentPathNode;

            // Occassionally mix things up
            if (this.dragon.getRandom().nextInt(8) == 0) {
                this.patrolClockwise = !this.patrolClockwise;
            } else if (this.dragon.getRandom().nextInt(8) == 0) {
                targetPathNode += 6;
            }

            // Continue circling
            if (this.patrolClockwise)
                ++targetPathNode;
            else
                --targetPathNode;
            if (targetPathNode < 0)
                targetPathNode += 12; // Wrap around

            targetPathNode %= 24;
            this.path = this.dragon.findPath(currentPathNode, targetPathNode, null);
            if (this.path != null) {
                this.path.next();
            }
        }
        this.followPath();
    }
}
