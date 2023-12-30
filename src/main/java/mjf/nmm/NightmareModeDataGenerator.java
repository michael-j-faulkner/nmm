package mjf.nmm;

import java.util.concurrent.CompletableFuture;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class NightmareModeDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
		pack.addProvider(DamageTagGenerator::new);
	}
	
	private static class DamageTagGenerator extends FabricTagProvider<DamageType> {
		public DamageTagGenerator(FabricDataOutput output, CompletableFuture<WrapperLookup> registriesFuture) {
			super(output, RegistryKeys.DAMAGE_TYPE, registriesFuture);
		}

		private static final TagKey<DamageType> PERMANENT_DAMAGE = TagKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(NightmareMode.MODID, "permanent_damage"));

		@Override
		protected void configure(WrapperLookup arg) {
			getOrCreateTagBuilder(PERMANENT_DAMAGE)
				.add(DamageTypes.DRAGON_BREATH)
				.add(DamageTypes.INDIRECT_MAGIC)
				.add(DamageTypes.MAGIC)
				.add(DamageTypes.WITHER)
				.add(DamageTypes.WITHER_SKULL)
				.add(DamageTypes.SONIC_BOOM)
				.add(DamageTypes.STARVE);
		}

	}
}
