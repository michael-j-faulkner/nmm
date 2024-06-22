package mjf.nmm.mixin.enderdragon;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.phase.AbstractPhase;
import net.minecraft.entity.boss.dragon.phase.ChargingPlayerPhase;
import net.minecraft.entity.boss.dragon.phase.PhaseType;
import net.minecraft.util.math.Vec3d;

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
        return 20.0f;
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void serverTick() {
        if (this.pathTarget == null) {
            this.dragon.getPhaseManager().setPhase(PhaseType.HOLDING_PATTERN);
            return;
        }
        if (this.chargingTicks > 0 && this.chargingTicks++ >= 20) {
            this.dragon.getPhaseManager().setPhase(PhaseType.HOLDING_PATTERN);
            return;
        }
        double d = this.pathTarget.squaredDistanceTo(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
        if (d < 4.0 || d > 22500.0) {
            ++this.chargingTicks;
        }
    }
}
