package mjf.nmm;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.SimpleFabricLootTableProvider;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTable.Builder;
import net.minecraft.loot.context.LootContextType;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class NightmareModeDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
		pack.addProvider(DamageTagGenerator::new);
		pack.addProvider(LootTableGenerator::new);
	}

	private static class LootTableGenerator extends SimpleFabricLootTableProvider {

		public LootTableGenerator(FabricDataOutput dataGenerator) {
			super(dataGenerator, LootContextTypes.ENTITY);
		}

		@Override
		public void accept(BiConsumer<Identifier, LootTable.Builder> biConsumer) {
			biConsumer.accept(Registries.ENTITY_TYPE.getId(EntityType.IRON_GOLEM).withPrefixedPath("entities/"), LootTable.builder()
				.pool(LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f))
				.with(ItemEntry.builder(Items.IRON_NUGGET).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(3, 5))))
				.with(ItemEntry.builder(Items.POPPY).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(0, 2))))));
		}
		
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
				.add(DamageTypes.STARVE)
				.add(DamageTypes.THORNS);
		}

	}
}
