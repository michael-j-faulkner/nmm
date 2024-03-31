package mjf.nmm.mixin.entities;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.serialization.Dynamic;

import mjf.nmm.entities.ScalingDifficulty;
import mjf.nmm.entities.ai.ZombieBrain;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;

@Mixin(ZombieEntity.class)
public abstract class ZombieEntityMixin extends HostileEntity {
    protected ZombieEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }
    
    @Inject(at = @At("RETURN"), method = "createZombieAttributes", cancellable = true)
	private static void editAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
		cir.setReturnValue(cir.getReturnValue()
			.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
			.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4.0)
            .add(EntityAttributes.GENERIC_ARMOR, 10.0)
            .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.5)
            .add(EntityAttributes.ZOMBIE_SPAWN_REINFORCEMENTS, 0.1)
            .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0));
	}

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void initEquipment(Random random, LocalDifficulty localDifficulty) {
        super.initEquipment(random, localDifficulty);
        double percentDifficulty = ScalingDifficulty.getPercentDifficulty((ServerWorld)this.getWorld(), this.getPos());
        if (random.nextFloat() < 0.5 * percentDifficulty) {
            switch (random.nextInt(12)) {
            case 0:
                this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_PICKAXE));
                break;
            case 1:
            case 2:
                this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_PICKAXE));
                break;
            case 3:
                this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_PICKAXE));
                break;
            case 4:
                this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SHOVEL));
                break;
            case 5:
            case 6:
                this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SHOVEL));
                break;
            case 7:
                this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SHOVEL));
                break;
            case 8:
                this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_AXE));
                break;
            case 9:
            case 10:
                this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_AXE));
                break;
            case 11:
                this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_AXE));
                break;
            }
        }
    }

    /**
	 * Delete normal zombie ai
     * @reason 
     * @author
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
