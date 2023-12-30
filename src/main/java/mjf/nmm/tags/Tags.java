package mjf.nmm.tags;

import mjf.nmm.NightmareMode;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class Tags {
    public static final TagKey<DamageType> PERMANENT_DAMAGE = TagKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(NightmareMode.MODID, "permanent_damage"));
}
