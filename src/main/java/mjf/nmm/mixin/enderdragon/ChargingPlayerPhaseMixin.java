package mjf.nmm.mixin.enderdragon;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.phase.AbstractPhase;
import net.minecraft.entity.boss.dragon.phase.ChargingPlayerPhase;
import net.minecraft.entity.boss.dragon.phase.PhaseType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.feature.EndPortalFeature;

@Mixin(ChargingPlayerPhase.class)
public abstract class ChargingPlayerPhaseMixin extends AbstractPhase {
    public ChargingPlayerPhaseMixin(EnderDragonEntity dragon) {
        super(dragon);
    }
    @Shadow
    private Vec3d pathTarget;
    @Shadow
    private int chargingTicks;

    /**
     * @author
     * @reason
     */
    @Overwrite
    public float getMaxYAcceleration() {
        return 40.0f;
    }

    @Override
    public float getYawAcceleration() {
        return 2.0f * super.getMaxYAcceleration();
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void serverTick(ServerWorld world) {
        BlockPos origin = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, new BlockPos(EndPortalFeature.offsetOrigin(this.dragon.getFightOrigin())));
        PlayerEntity nearestPlayer = world.getClosestPlayer((double)origin.getX(), (double)origin.getY(), (double)origin.getZ(), 256.0, (entity) -> true);
		this.pathTarget = nearestPlayer != null ? nearestPlayer.getPos() : null;
        if (this.pathTarget == null) {
            this.dragon.getPhaseManager().setPhase(PhaseType.HOLDING_PATTERN);
            return;
        }
        if (this.chargingTicks > 0 && this.chargingTicks >= 5) {
            this.dragon.getPhaseManager().setPhase(PhaseType.HOLDING_PATTERN);
            return;
        }
        double d = this.pathTarget.squaredDistanceTo(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
        if (d < 100.0 || d > 22500.0) {
            ++this.chargingTicks;
        }
    }
}
