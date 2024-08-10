package mjf.nmm.mixin.entities;

import java.util.Arrays;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import mjf.nmm.NightmareMode;
import mjf.nmm.entities.ScalingDifficulty;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.enchantment.provider.EnchantmentProviders;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Targeter;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends LivingEntity implements Targeter {
    protected MobEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    private static final double[] ARMOR_CHANCES_EARLY_GAME = {0.9, 0.05, 0.015, 0.015, 0.019, 0.001};
    private static final double[] ARMOR_CHANCES_LATE_GAME = {0.05, 0.1, 0.125, 0.125, 0.4, 0.2};

    private static final int NUM_ARMOR_LEVELS = 6;
    private static final double[] LERP_CONSTANTS_EARLY_GAME = new double[NUM_ARMOR_LEVELS];
    private static final double[] LERP_CONSTANTS_LATE_GAME = new double[NUM_ARMOR_LEVELS];
    static {
        assert Arrays.stream(ARMOR_CHANCES_EARLY_GAME).sum() - 1.0 < 1.0e-7;
        assert Arrays.stream(ARMOR_CHANCES_LATE_GAME).sum() - 1.0 < 1.0e-7;

        // See Desmos file for calculation: https://www.desmos.com/calculator/jisu1gdvzx
        LERP_CONSTANTS_EARLY_GAME[0] = ARMOR_CHANCES_EARLY_GAME[0];
        LERP_CONSTANTS_LATE_GAME[0] = ARMOR_CHANCES_LATE_GAME[0];
        for (int i = 1; i < NUM_ARMOR_LEVELS; ++i) {
            LERP_CONSTANTS_EARLY_GAME[i] = ARMOR_CHANCES_EARLY_GAME[i] * LERP_CONSTANTS_EARLY_GAME[i-1] / (ARMOR_CHANCES_EARLY_GAME[i-1] * (1 - LERP_CONSTANTS_EARLY_GAME[i-1]));
            LERP_CONSTANTS_LATE_GAME[i] = ARMOR_CHANCES_LATE_GAME[i] * LERP_CONSTANTS_LATE_GAME[i-1] / (ARMOR_CHANCES_LATE_GAME[i-1] * (1 - LERP_CONSTANTS_LATE_GAME[i-1]));
        }
    }

    /**
     * @author 
     * @reason 
     */
    @Overwrite
    public void initEquipment(Random random, LocalDifficulty localDifficulty) {
        double percentDifficulty = ScalingDifficulty.getPercentDifficulty((ServerWorld)this.getWorld(), this.getPos());
        int armorLevel = 0;
        for (int i = 0; i < NUM_ARMOR_LEVELS; ++i) {
            armorLevel = i;
            double probStopping = MathHelper.lerp(percentDifficulty, LERP_CONSTANTS_EARLY_GAME[i], LERP_CONSTANTS_LATE_GAME[i]);
            if (random.nextFloat() < probStopping) {
                break;
            }
        }
        if (armorLevel == 0)
            return;
        --armorLevel;

        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            if (equipmentSlot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) 
                continue;
            
            // Add armor boots to head, with slight chance to stop partway
            if (random.nextFloat() < 0.5 + 0.45 * percentDifficulty) {
                // Check if there's already an item set
                ItemStack itemStack = this.getEquippedStack(equipmentSlot);
                if (itemStack.isEmpty()) {
                    // Item to put in if not
                    Item item = MobEntity.getEquipmentForSlot(equipmentSlot, armorLevel);
                    if (item != null)
                        this.equipStack(equipmentSlot, new ItemStack(item));
                }
            } else {
                break;
            }
        }
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void updateEnchantments(ServerWorldAccess world, Random random, LocalDifficulty localDifficulty) {
        double percentDifficulty = ScalingDifficulty.getPercentDifficulty(world, this.getPos());
        if (random.nextFloat() < percentDifficulty) {
            for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
                ItemStack itemStack = this.getEquippedStack(equipmentSlot);
                if (!itemStack.isEmpty()) {
                    EnchantmentHelper.enchant(random, itemStack, (int)(5.0 + percentDifficulty * (15.0 + random.nextInt(15))), 
                        world.getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntryList(EnchantmentTags.ON_MOB_SPAWN_EQUIPMENT)
                        .orElseThrow().stream());
                    this.equipStack(equipmentSlot, itemStack);
                }
            }
        }
    }
}
