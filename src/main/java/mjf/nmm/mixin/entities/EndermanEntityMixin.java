package mjf.nmm.mixin.entities;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
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
			.add(EntityAttributes.ATTACK_DAMAGE, 30.0));
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

    @Inject(method = "damage", at = @At("RETURN"))
    public void damage(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() && source.getAttacker() instanceof PlayerEntity player) {
            for (int i = 0; i < 16; ++i) {
                double x = player.getX() + (player.getRandom().nextDouble() - 0.5) * 32.0;
                double y = player.getY() + (player.getRandom().nextDouble() - 0.5) * 32.0;
                double z = player.getZ() + (player.getRandom().nextDouble() - 0.5) * 32.0;

                if (player.teleport(x, y, z, true)) {
                    world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, SoundCategory.HOSTILE, 1.0f, 0.25f);
                    break;
                }
            }
        }
    }

    @Shadow
    abstract boolean teleportTo(double x, double y, double z);

    @Inject(method = "teleportRandomly", at = @At("HEAD"), cancellable = true)
    protected void teleportRandomly(CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity entity = this.getWorld().getClosestPlayer(this, 128);
        if (entity != null) {

            this.teleportTo(
                entity.getX() + this.getRandom().nextTriangular(0.0, 0.5) * 128.0, 
                entity.getY() + this.getRandom().nextTriangular(0.0, 0.5) * 128.0, 
                entity.getZ() + this.getRandom().nextTriangular(0.0, 0.5) * 128.0);
            cir.cancel();
        }
	}
}
