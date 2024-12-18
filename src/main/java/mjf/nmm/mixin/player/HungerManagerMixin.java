package mjf.nmm.mixin.player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.entity.player.HungerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;

@Mixin(HungerManager.class)
public abstract class HungerManagerMixin {
    @Shadow
    private int foodLevel = 20;
    @Shadow
    private float saturationLevel = 5.0f;
    @Shadow
    private float exhaustion;
    @Shadow
    private int foodTickTimer;

    @Shadow
    public abstract void addExhaustion(float exhaustion);

    /**
     * Remove fast regeneration from food
     * @reason 
     * @author
     */
    @Overwrite
    public void update(ServerPlayerEntity player) {
        Difficulty difficulty = player.getWorld().getDifficulty();
        ServerWorld serverWorld = player.getServer().getWorld(player.getWorld().getRegistryKey());
        if (this.exhaustion > 4.0f) {
            this.exhaustion -= 4.0f;
            if (this.saturationLevel > 0.0f) {
                this.saturationLevel = Math.max(this.saturationLevel - 1.0f, 0.0f);
            } else if (difficulty != Difficulty.PEACEFUL) {
                this.foodLevel = Math.max(this.foodLevel - 1, 0);
            }
        }
        if (serverWorld.getGameRules().getBoolean(GameRules.NATURAL_REGENERATION) && this.foodLevel >= 18 && player.canFoodHeal()) {
            ++this.foodTickTimer;
            if (this.foodTickTimer >= 100) {
                player.heal(1.0f);
                this.addExhaustion(6.0f);
                this.foodTickTimer = 0;
            }
        } else if (this.foodLevel <= 0) {
            ++this.foodTickTimer;
            if (this.foodTickTimer >= 80) {
                player.damage(serverWorld, player.getDamageSources().starve(), 1.0f);
                this.foodTickTimer = 0;
            }
        } else {
            this.foodTickTimer = 0;
        }
    }
}
