package mjf.nmm.mixin.entities;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.StreamSupport;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.util.Util;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.TradedItem;
import net.minecraft.village.TradeOffers.Factory;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.World;

@Mixin(VillagerEntity.class)
public abstract class VillagerEntityMixin extends MerchantEntity {
    public VillagerEntityMixin(EntityType<? extends MerchantEntity> entityType, World world) {
        super(entityType, world);
    }

    public static class TradeFactory implements Factory {
        private Item buyItem1;
        private Item buyItem2 = null;
        private ItemStack sellItem;
		private BiFunction<Entity, Random, ItemStack> sellItemGenerator = null;
        private int maxUses = 1;
        private int experience = 1;
        private float multiplier = 0.0f;
		private int buyItem1Min = 1;
		private int buyItem1Max = 1;
		private int buyItem2Min = 1;
		private int buyItem2Max = 1;
		private int sellItemMin = 1;
		private int sellItemMax = 1;

		public TradeFactory buyItem1(Item item) {
			this.buyItem1 = item;
			return this;
		}

		public TradeFactory buyItem2(Item item) {
			this.buyItem2 = item;
			return this;
		}

		public TradeFactory sellItem(Item item) {
			this.sellItem = new ItemStack(item);
			return this;
		}
		
		public TradeFactory sellItem(ItemStack itemStack) {
			this.sellItem = itemStack.copy();
			return this;
		}

		public TradeFactory buyItem1Min(int count) {
			this.buyItem1Min = count;
			return this;
		}

		public TradeFactory buyItem1Max(int count) {
			this.buyItem1Max = count;
			return this;
		}

		public TradeFactory buyItem1Count(int count) {
			this.buyItem1Min = count;
			this.buyItem1Max = count;
			return this;
		}

		public TradeFactory buyItem2Min(int count) {
			this.buyItem2Min = count;
			return this;
		}

		public TradeFactory buyItem2Max(int count) {
			this.buyItem2Max = count;
			return this;
		}

		public TradeFactory buyItem2Count(int count) {
			this.buyItem2Min = count;
			this.buyItem2Max = count;
			return this;
		}

		public TradeFactory sellItemMin(int count) {
			this.sellItemMin = count;
			return this;
		}

		public TradeFactory sellItemMax(int count) {
			this.sellItemMax = count;
			return this;
		}

		public TradeFactory sellItemCount(int count) {
			this.sellItemMin = count;
			this.sellItemMax = count;
			return this;
		}

		public TradeFactory maxUses(int uses) {
			this.maxUses = uses;
			return this;
		}

		public TradeFactory experience(int experience) {
			this.experience = experience;
			return this;
		}

		public TradeFactory multiplier(float multiplier) {
			this.multiplier = multiplier;
			return this;
		}

		public TradeFactory sellItemGenerator(BiFunction<Entity, Random, ItemStack> generator) {
			this.sellItemGenerator = generator;
			return this;
		}

        @Override
        public TradeOffer create(Entity entity, Random random) {
			ItemStack sellItem;
			if (this.sellItemGenerator != null)
				sellItem = this.sellItemGenerator.apply(entity, random);
			else {
				sellItem = this.sellItem.copy();
			}
			sellItem.setCount(random.nextBetween(this.sellItemMin, this.sellItemMax));
            return new TradeOffer(
				new TradedItem(this.buyItem1, random.nextBetween(this.buyItem1Min, this.buyItem1Max)), 
				this.buyItem2 != null ? Optional.of(new TradedItem(this.buyItem2, random.nextBetween(this.buyItem2Min, this.buyItem2Max))) : Optional.empty(), 
				sellItem, 
				this.maxUses, 
				this.experience, 
				this.multiplier);
        }
    }
    
    @Shadow
    public abstract VillagerData getVillagerData();

    @ModifyVariable(method = "fillRecipes", at = @At("STORE"), ordinal = 0)
    private Int2ObjectMap<TradeOffers.Factory[]> replaceVillagerTrades(Int2ObjectMap<TradeOffers.Factory[]> map) {
        return CUSTOM_TRADES.get(this.getVillagerData().getProfession());
    }

	private static ItemStack enchantWithLevel(ItemStack itemStack, int level, Random random, Entity entity) {
		Registry<Enchantment> enchantmentRegistry = entity.getRegistryManager().getOrThrow(Enchantments.AQUA_AFFINITY.getRegistryRef());
		return EnchantmentHelper.enchant(random, itemStack, level, StreamSupport.stream(enchantmentRegistry.iterateEntries(EnchantmentTags.NON_TREASURE).spliterator(), false));
	}

	private static final Map<VillagerProfession, Int2ObjectMap<TradeOffers.Factory[]>> CUSTOM_TRADES = Util.make(() -> {
		Map<VillagerProfession, Int2ObjectMap<TradeOffers.Factory[]>> trades = new HashMap<>();
		
		trades.put(VillagerProfession.ARMORER, 
			new Int2ObjectOpenHashMap<>(Map.of(
				1, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.IRON_INGOT).buyItem1Min(8).buyItem1Max(16)
						.sellItemGenerator((entity, random) -> enchantWithLevel(Items.IRON_HELMET.getDefaultStack(), 10, random, entity))
						.experience(20).maxUses(2).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.IRON_INGOT).buyItem1Min(6).buyItem1Max(12)
						.sellItemGenerator((entity, random) -> enchantWithLevel(Items.IRON_BOOTS.getDefaultStack(), 10, random, entity))
						.experience(20).maxUses(2).multiplier(0.01f),
				},
				2, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.IRON_INGOT).buyItem1Min(12).buyItem1Max(24)
						.sellItemGenerator((entity, random) -> enchantWithLevel(Items.IRON_CHESTPLATE.getDefaultStack(), 10, random, entity))
						.experience(25).maxUses(2).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.IRON_INGOT).buyItem1Min(11).buyItem1Max(22)
						.sellItemGenerator((entity, random) -> enchantWithLevel(Items.IRON_LEGGINGS.getDefaultStack(), 10, random, entity))
						.experience(25).maxUses(2).multiplier(0.01f),
				}
			))
		);

		trades.put(VillagerProfession.BUTCHER, 
			new Int2ObjectOpenHashMap<>(Map.of(
				1, new TradeOffers.Factory[] {

				}
			))
		);

		trades.put(VillagerProfession.CARTOGRAPHER, 
			new Int2ObjectOpenHashMap<>(Map.of(
				1, new TradeOffers.Factory[] {

				}
			))
		);

		trades.put(VillagerProfession.CLERIC, 
			new Int2ObjectOpenHashMap<>(Map.of(
				1, new TradeOffers.Factory[] {

				}
			))
		);

		trades.put(VillagerProfession.FARMER, 
			new Int2ObjectOpenHashMap<>(Map.of(
				1, new TradeOffers.Factory[] {

				}
			))
		);

		trades.put(VillagerProfession.FISHERMAN, 
			new Int2ObjectOpenHashMap<>(Map.of(
				1, new TradeOffers.Factory[] {

				}
			))
		);

		trades.put(VillagerProfession.FLETCHER, 
			new Int2ObjectOpenHashMap<>(Map.of(
				1, new TradeOffers.Factory[] {

				}
			))
		);

		trades.put(VillagerProfession.LEATHERWORKER, 
			new Int2ObjectOpenHashMap<>(Map.of(
				1, new TradeOffers.Factory[] {

				}
			))
		);

		trades.put(VillagerProfession.LIBRARIAN, 
			new Int2ObjectOpenHashMap<>(Map.of(
				1, new TradeOffers.Factory[] {

				}
			))
		);

		trades.put(VillagerProfession.MASON, 
			new Int2ObjectOpenHashMap<>(Map.of(
				1, new TradeOffers.Factory[] {

				}
			))
		);

		trades.put(VillagerProfession.SHEPHERD, 
			new Int2ObjectOpenHashMap<>(Map.of(
				1, new TradeOffers.Factory[] {

				}
			))
		);

		trades.put(VillagerProfession.TOOLSMITH, 
			new Int2ObjectOpenHashMap<>(Map.of(
				1, new TradeOffers.Factory[] {

				}
			))
		);

		trades.put(VillagerProfession.WEAPONSMITH, 
			new Int2ObjectOpenHashMap<>(Map.of(
				1, new TradeOffers.Factory[] {

				}
			))
		);

		return trades;
	});
}
