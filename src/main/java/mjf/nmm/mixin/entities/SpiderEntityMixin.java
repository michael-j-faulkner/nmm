package mjf.nmm.mixin.entities;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.world.World;

@Mixin(SpiderEntity.class)
public abstract class SpiderEntityMixin extends HostileEntity {
    protected SpiderEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(at = @At("RETURN"), method = "createSpiderAttributes", cancellable = true)
	private static void editAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
		cir.setReturnValue(cir.getReturnValue()
			.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.35));
	}
    
    @Override
    public boolean tryAttack(Entity target) {
        if (super.tryAttack(target)) {
            if (this.getWorld().getBlockState(target.getBlockPos()).getHardness(target.getWorld(), target.getBlockPos()) >= 0.0f) {
                this.getWorld().breakBlock(target.getBlockPos(), true);
                this.getWorld().setBlockState(target.getBlockPos(), Blocks.COBWEB.getDefaultState());
            }
            return true;
        }
        return false;
    }
}
