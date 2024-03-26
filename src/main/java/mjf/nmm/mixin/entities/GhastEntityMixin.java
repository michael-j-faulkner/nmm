package mjf.nmm.mixin.entities;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.entity.mob.GhastEntity;

@Mixin(GhastEntity.class)
public class GhastEntityMixin {
    /**
     * @author
     * @reason
     */
    @Overwrite
    public int getFireballStrength() {
        return 4;
    }
}
