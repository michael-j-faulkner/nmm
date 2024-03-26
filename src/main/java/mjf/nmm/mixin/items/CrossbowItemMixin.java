package mjf.nmm.mixin.items;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.minecraft.item.CrossbowItem;

@Mixin(CrossbowItem.class)
public class CrossbowItemMixin {
    /**
     * @author
     * @reason
     */
    @Overwrite
    public int getRange() {
        return 24;
    }
}
