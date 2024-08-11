package mjf.nmm.mixin.entities;

import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MagmaCubeEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

@Mixin(MagmaCubeEntity.class)
public abstract class MagmaCubeEntityMixin extends SlimeEntity {
    public MagmaCubeEntityMixin(EntityType<? extends SlimeEntity> entityType, World world) {
        super(entityType, world);
    }

@Override
    public void onPlayerCollision(PlayerEntity player) {
        if (this.canAttack()) {
            this.damage(player);
            if (this.getWorld().getBlockState(player.getBlockPos()).getHardness(this.getWorld(), player.getBlockPos()) >= 0.0f 
                    && !this.getWorld().getBlockState(this.getBlockPos()).isOf(Blocks.LAVA)) {
                this.getWorld().breakBlock(player.getBlockPos(), true);
                this.getWorld().setBlockState(player.getBlockPos(), Blocks.LAVA.getDefaultState());
            }
        }
    }
}
