package mjf.nmm.mixin.entities;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.chunk.light.ChunkLightingView;

@Mixin(EndermanEntity.class)
public abstract class EndermanEntityMixin extends HostileEntity implements Angerable {
    protected EndermanEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(at = @At("RETURN"), method = "createEndermanAttributes", cancellable = true)
	private static void editAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
		cir.setReturnValue(cir.getReturnValue()
			.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 40.0));
	}

    private int destroyLightCooldown = 0;
    
    @Inject(method = "mobTick", at = @At("TAIL"))
    public void destroyLights(CallbackInfo ci) {
        int lightLevel = this.getWorld().getLightingProvider().get(LightType.BLOCK).getLightLevel(this.getBlockPos());
        if (lightLevel > 0 && destroyLightCooldown == 0) {
            destroyLightSource(this.getWorld(), this.getBlockPos());
            this.destroyLightCooldown = 20;
        }
        if (this.destroyLightCooldown > 0)
            --this.destroyLightCooldown;
        if (this.getAngerTime() > 0) {
            this.dismountVehicle();
        }
    }

    private static void destroyLightSource(World world, BlockPos pos) {
        if (world.getLuminance(pos) > 0) {
            if (world.getBlockState(pos).getHardness(world, pos) >= 0.0f)
                world.breakBlock(pos, true);
            return;
        }
        ChunkLightingView blockLightView = world.getLightingProvider().get(LightType.BLOCK);
        int lightLevel = blockLightView.getLightLevel(pos);
        if (blockLightView.getLightLevel(pos.up()) > lightLevel) {
            destroyLightSource(world, pos.up());
            return;
        }
        if (blockLightView.getLightLevel(pos.down()) > lightLevel) {
            destroyLightSource(world, pos.down());
            return;
        }
        if (blockLightView.getLightLevel(pos.north()) > lightLevel) {
            destroyLightSource(world, pos.north());
            return;
        }
        if (blockLightView.getLightLevel(pos.east()) > lightLevel) {
            destroyLightSource(world, pos.east());
            return;
        }
        if (blockLightView.getLightLevel(pos.south()) > lightLevel) {
            destroyLightSource(world, pos.south());
            return;
        }
        if (blockLightView.getLightLevel(pos.west()) > lightLevel) {
            destroyLightSource(world, pos.west());
            return;
        }
    }
}
