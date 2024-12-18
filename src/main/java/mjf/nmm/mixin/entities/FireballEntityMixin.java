package mjf.nmm.mixin.entities;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.AbstractFireballEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

@Mixin(FireballEntity.class)
public abstract class FireballEntityMixin extends AbstractFireballEntity {
    public FireballEntityMixin(EntityType<? extends AbstractFireballEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "onCollision", at = @At("TAIL"))
    private void placeLava(HitResult hitResult, CallbackInfo ci) {
        if (this.getWorld().getBlockState(this.getBlockPos()).getHardness(this.getWorld(), this.getBlockPos()) >= 0.0f 
                && !this.getWorld().getBlockState(this.getBlockPos()).isOf(Blocks.LAVA)) {
            this.getWorld().breakBlock(this.getBlockPos(), true);
            this.getWorld().setBlockState(this.getBlockPos(), Blocks.LAVA.getDefaultState());
        }
    }
}
