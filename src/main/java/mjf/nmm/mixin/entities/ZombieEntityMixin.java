package mjf.nmm.mixin.entities;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.serialization.Dynamic;

import mjf.nmm.entities.ai.ZombieBrain;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

@Mixin(ZombieEntity.class)
public abstract class ZombieEntityMixin extends HostileEntity {
    protected ZombieEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }
    
    @Inject(at = @At("RETURN"), method = "createZombieAttributes", cancellable = true)
	private static void editAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
		cir.setReturnValue(cir.getReturnValue()
			.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25)
            .add(EntityAttributes.GENERIC_ARMOR, 0.0)
            .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.2)
            .add(EntityAttributes.ZOMBIE_SPAWN_REINFORCEMENTS, 0.1)
            .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0));
	}

    /**
	 * Delete normal zombie ai
	 */
	@Overwrite
	public void initGoals() {
	}

    protected Brain.Profile<ZombieEntity> createBrainProfile() {
        return Brain.createProfile(ZombieBrain.MEMORY_MODULES, ZombieBrain.SENSORS);
    }

	@Override
    protected Brain<?> deserializeBrain(Dynamic<?> dynamic) {
        return ZombieBrain.create((ZombieEntity) (Object) this, (Brain<ZombieEntity>) this.createBrainProfile().deserialize(dynamic));
    }

	@SuppressWarnings("unchecked")
	public Brain<ZombieEntity> getBrain() {
        return (Brain<ZombieEntity>) super.getBrain();
    }

    protected void mobTick() {
        this.getBrain().tick((ServerWorld)this.getWorld(), (ZombieEntity) (Object) this);
		ZombieBrain.updateActivities(this.getBrain());
		super.mobTick();
    }
}
