package mjf.nmm;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;

import mjf.nmm.tags.Tags;
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
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.EnchantedCountIncreaseLootFunction;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;

public class NightmareModeDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
		pack.addProvider(DamageTagGenerator::new);
		pack.addProvider(LootTableGenerator::new);
	}

	private static class LootTableGenerator extends SimpleFabricLootTableProvider {
		private CompletableFuture<WrapperLookup> regLookup;

		public LootTableGenerator(FabricDataOutput output, CompletableFuture<WrapperLookup> registryLookup) {
			super(output, registryLookup, LootContextTypes.ENTITY);
			this.regLookup = registryLookup;
		}

		@Override
		public void accept(BiConsumer<RegistryKey<LootTable>, Builder> lootTableBiConsumer) {
			lootTableBiConsumer.accept(EntityType.IRON_GOLEM.getLootTableId(), LootTable.builder()
				.pool(LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f))
					.with(ItemEntry.builder(Items.IRON_NUGGET).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(3, 5))))
					.with(ItemEntry.builder(Items.POPPY).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(0, 2))))
				)
			);

			try {
				lootTableBiConsumer.accept(EntityType.SPIDER.getLootTableId(), LootTable.builder()
					.pool(LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f))
						.with(ItemEntry.builder(Items.STRING).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(0, 2)))
							.apply(EnchantedCountIncreaseLootFunction.builder(this.regLookup.get(), UniformLootNumberProvider.create(0, 1))))
					)
				);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}

		
	}
	
	private static class DamageTagGenerator extends FabricTagProvider<DamageType> {
		public DamageTagGenerator(FabricDataOutput output, CompletableFuture<WrapperLookup> registriesFuture) {
			super(output, RegistryKeys.DAMAGE_TYPE, registriesFuture);
		}

		@Override
		protected void configure(WrapperLookup arg) {
			getOrCreateTagBuilder(Tags.PERMANENT_DAMAGE)
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
