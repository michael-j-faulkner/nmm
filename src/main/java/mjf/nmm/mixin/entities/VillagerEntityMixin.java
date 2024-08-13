package mjf.nmm.mixin.entities;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.SuspiciousStewEffectsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapDecorationTypes;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.registry.tag.StructureTags;
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

		public TradeFactory buyItem2Min(int count) {
			this.buyItem2Min = count;
			return this;
		}

		public TradeFactory buyItem2Max(int count) {
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
				sellItem.setCount(random.nextBetween(this.sellItemMin, this.sellItemMax));
			}
            return new TradeOffer(
				new TradedItem(this.buyItem1, random.nextBetween(this.buyItem1Min, this.buyItem1Max)), 
				this.buyItem2 != null ? Optional.of(new TradedItem(this.buyItem2, random.nextBetween(this.buyItem2Min, this.buyItem2Max))) : Optional.empty(), 
				sellItem, 
				this.maxUses, 
				this.experience, 
				this.multiplier);
        }
    }

    private static Int2ObjectMap<TradeOffers.Factory[]> copyToFastUtilMap(ImmutableMap<Integer, TradeOffers.Factory[]> map) {
		return new Int2ObjectOpenHashMap<>(map);
	}

    private static final Map<VillagerProfession, Int2ObjectMap<Factory[]>> CUSTOM_TRADES = Util.make(
		Maps.<VillagerProfession, Int2ObjectMap<TradeOffers.Factory[]>>newHashMap(),
		map -> {
			map.put(
				VillagerProfession.FARMER,
				copyToFastUtilMap(
					ImmutableMap.of(
						1,
						new TradeOffers.Factory[]{
							new TradeFactory()
								.buyItem1(Items.COPPER_INGOT).buyItem1Min(4).buyItem1Max(8)
								.sellItem(Items.BREAD).sellItemMax(8)
								.maxUses(8).experience(1).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.COPPER_INGOT).buyItem1Min(4).buyItem1Max(8)
								.sellItem(Items.POTATO).sellItemMax(8)
								.maxUses(8).experience(1).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.COPPER_INGOT).buyItem1Min(4).buyItem1Max(8)
								.sellItem(Items.CARROT).sellItemMax(8)
								.maxUses(8).experience(1).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.COPPER_INGOT).buyItem1Min(4).buyItem1Max(8)
								.sellItem(Items.BEETROOT).sellItemMax(8)
								.maxUses(8).experience(1).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.COPPER_INGOT).buyItem1Min(4).buyItem1Max(8)
								.sellItem(Items.APPLE).sellItemMax(8)
								.maxUses(8).experience(1).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.BONE_MEAL).buyItem1Min(16).buyItem1Max(32)
								.sellItem(Items.COPPER_INGOT)
								.maxUses(8).experience(2).multiplier(0.05f),
						},
						2,
						new TradeOffers.Factory[]{
							new TradeFactory()
								.buyItem1(Items.BONE_MEAL).buyItem1Min(32).buyItem1Max(64)
								.sellItem(Items.IRON_INGOT)
								.maxUses(8).experience(5).multiplier(0.01f),
							new TradeFactory()
								.buyItem1(Items.IRON_INGOT).buyItem1Max(4)
								.sellItem(Items.BEETROOT_SOUP)
								.maxUses(8).experience(5).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.IRON_INGOT).buyItem1Max(4)
								.sellItem(Items.MUSHROOM_STEW)
								.maxUses(8).experience(5).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.IRON_INGOT).buyItem1Max(4)
								.sellItemGenerator((entity, random) -> {
									List<SuspiciousStewEffectsComponent.StewEffect> potentialEffects = Arrays.asList(
										new SuspiciousStewEffectsComponent.StewEffect(StatusEffects.ABSORPTION, random.nextBetween(100, 600)),
										new SuspiciousStewEffectsComponent.StewEffect(StatusEffects.BAD_OMEN, random.nextBetween(100, 600)),
										new SuspiciousStewEffectsComponent.StewEffect(StatusEffects.BLINDNESS, random.nextBetween(100, 600)),
										new SuspiciousStewEffectsComponent.StewEffect(StatusEffects.FIRE_RESISTANCE, random.nextBetween(100, 600)),
										new SuspiciousStewEffectsComponent.StewEffect(StatusEffects.GLOWING, random.nextBetween(100, 600)),
										new SuspiciousStewEffectsComponent.StewEffect(StatusEffects.HASTE, random.nextBetween(100, 600)),
										new SuspiciousStewEffectsComponent.StewEffect(StatusEffects.HEALTH_BOOST, random.nextBetween(100, 600)),
										new SuspiciousStewEffectsComponent.StewEffect(StatusEffects.INVISIBILITY, random.nextBetween(100, 600)),
										new SuspiciousStewEffectsComponent.StewEffect(StatusEffects.JUMP_BOOST, random.nextBetween(100, 600)),
										new SuspiciousStewEffectsComponent.StewEffect(StatusEffects.LEVITATION, random.nextBetween(100, 600)),
										new SuspiciousStewEffectsComponent.StewEffect(StatusEffects.MINING_FATIGUE, random.nextBetween(100, 600)),
										new SuspiciousStewEffectsComponent.StewEffect(StatusEffects.NAUSEA, random.nextBetween(100, 600)),
										new SuspiciousStewEffectsComponent.StewEffect(StatusEffects.NIGHT_VISION, random.nextBetween(100, 600)),
										new SuspiciousStewEffectsComponent.StewEffect(StatusEffects.POISON, random.nextBetween(100, 600)),
										new SuspiciousStewEffectsComponent.StewEffect(StatusEffects.REGENERATION, random.nextBetween(100, 600)),
										new SuspiciousStewEffectsComponent.StewEffect(StatusEffects.RESISTANCE, random.nextBetween(100, 600)),
										new SuspiciousStewEffectsComponent.StewEffect(StatusEffects.SLOWNESS, random.nextBetween(100, 600)),
										new SuspiciousStewEffectsComponent.StewEffect(StatusEffects.SPEED, random.nextBetween(100, 600)),
										new SuspiciousStewEffectsComponent.StewEffect(StatusEffects.STRENGTH, random.nextBetween(100, 600)),
										new SuspiciousStewEffectsComponent.StewEffect(StatusEffects.WEAKNESS, random.nextBetween(100, 600)),
										new SuspiciousStewEffectsComponent.StewEffect(StatusEffects.WITHER, random.nextBetween(100, 600)));
									Collections.shuffle(potentialEffects);
									ItemStack stew = new ItemStack(Items.SUSPICIOUS_STEW);
									stew.set(DataComponentTypes.SUSPICIOUS_STEW_EFFECTS, 
										new SuspiciousStewEffectsComponent(potentialEffects.subList(0, 1 + random.nextInt(3))));
									return stew;
								})
								.maxUses(8).experience(5).multiplier(0.05f)
						},
						3,
						new TradeOffers.Factory[]{
							new TradeFactory()
								.buyItem1(Items.LAPIS_LAZULI).buyItem1Min(16).buyItem1Max(32)
								.sellItem(Items.SWEET_BERRIES).sellItemMax(16)
								.maxUses(16).experience(10).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.LAPIS_LAZULI).buyItem1Min(16).buyItem1Max(32)
								.sellItem(Items.MELON_SLICE).sellItemMax(16)
								.maxUses(16).experience(10).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.LAPIS_LAZULI).buyItem1Min(16).buyItem1Max(32)
								.sellItem(Items.CAKE)
								.maxUses(16).experience(10).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.LAPIS_LAZULI).buyItem1Min(16).buyItem1Max(32)
								.sellItem(Items.COOKIE).sellItemMax(16)
								.maxUses(16).experience(10).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.LAPIS_LAZULI).buyItem1Min(16).buyItem1Max(32)
								.sellItem(Items.PUMPKIN_PIE).sellItemMax(8)
								.maxUses(16).experience(10).multiplier(0.05f),
						},
						4,
						new TradeOffers.Factory[]{
							new TradeFactory()
								.buyItem1(Items.DIAMOND)
								.sellItem(Items.GOLDEN_APPLE).sellItemMax(4)
								.maxUses(4).experience(20).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.DIAMOND)
								.sellItem(Items.GOLDEN_CARROT).sellItemMax(8)
								.maxUses(4).experience(20).multiplier(0.05f),
						},
						5,
						new TradeOffers.Factory[]{
							new TradeFactory()
								.buyItem1(Items.EMERALD).buyItem1Min(8).buyItem1Max(16)
								.sellItem(Items.ENCHANTED_GOLDEN_APPLE)
								.maxUses(2),
							new TradeFactory()
								.buyItem1(Items.BONE_BLOCK).buyItem1Max(8)
								.sellItem(Items.LAPIS_LAZULI).sellItemMax(8)
								.maxUses(8).multiplier(0.01f),
						}
					)
				)
			);
			map.put(
				VillagerProfession.FISHERMAN,
				copyToFastUtilMap(
					ImmutableMap.of(
						1,
						new TradeOffers.Factory[]{
							new TradeFactory()
								.buyItem1(Items.COPPER_INGOT).buyItem1Max(4)
								.sellItem(Items.WATER_BUCKET)
								.maxUses(8).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.COPPER_INGOT).buyItem1Max(4)
								.sellItem(Items.FISHING_ROD)
								.maxUses(8).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.STRING).buyItem1Min(4).buyItem1Max(8)
								.sellItem(Items.COPPER_INGOT)
								.maxUses(8).multiplier(0.05f),
						},
						2,
						new TradeOffers.Factory[]{
							new TradeFactory()
								.buyItem1(Items.STRING).buyItem1Min(8).buyItem1Max(16)
								.sellItem(Items.IRON_INGOT)
								.maxUses(8).experience(5).multiplier(0.01f),
							new TradeFactory()
								.buyItem1(Items.IRON_INGOT).buyItem1Max(4)
								.sellItem(Items.COOKED_COD).sellItemMax(4)
								.maxUses(8).experience(5).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.IRON_INGOT).buyItem1Max(4)
								.sellItem(Items.COOKED_SALMON).sellItemMax(4)
								.maxUses(8).experience(5).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.IRON_INGOT).buyItem1Max(4)
								.sellItem(Items.TROPICAL_FISH).sellItemMax(4)
								.maxUses(8).experience(5).multiplier(0.05f),
						},
						3,
						new TradeOffers.Factory[]{
							new TradeFactory()
								.buyItem1(Items.LAPIS_LAZULI).buyItem1Min(16).buyItem1Max(32)
								.sellItem(Items.COD_BUCKET)
								.maxUses(4).experience(10).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.LAPIS_LAZULI).buyItem1Min(16).buyItem1Max(32)
								.sellItem(Items.SALMON_BUCKET)
								.maxUses(4).experience(10).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.LAPIS_LAZULI).buyItem1Min(16).buyItem1Max(32)
								.sellItem(Items.TROPICAL_FISH_BUCKET)
								.maxUses(4).experience(10).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.LAPIS_LAZULI).buyItem1Min(16).buyItem1Max(32)
								.sellItem(Items.PUFFERFISH_BUCKET)
								.maxUses(4).experience(10).multiplier(0.05f),
						},
						4,
						new TradeOffers.Factory[]{
							new TradeFactory()
								.buyItem1(Items.DIAMOND).buyItem1Min(16).buyItem1Max(48)
								.sellItemGenerator((entity, random) -> {
									Optional<RegistryEntry.Reference<Enchantment>> lure = entity.getWorld().getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(Enchantments.LURE);
									return lure.isPresent() ? EnchantedBookItem.forEnchantment(new EnchantmentLevelEntry(lure.get(), 3)) : Items.BOOK.getDefaultStack();
								})
								.maxUses(2).experience(40).multiplier(0.01f),
							new TradeFactory()
								.buyItem1(Items.DIAMOND).buyItem1Min(16).buyItem1Max(48)
								.sellItemGenerator((entity, random) -> {
									Optional<RegistryEntry.Reference<Enchantment>> luckOfTheSea = entity.getWorld().getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(Enchantments.LUCK_OF_THE_SEA);
									return luckOfTheSea.isPresent() ? EnchantedBookItem.forEnchantment(new EnchantmentLevelEntry(luckOfTheSea.get(), 3)) : Items.BOOK.getDefaultStack();
								})
								.maxUses(2).experience(40).multiplier(0.01f),
						},
						5,
						new TradeOffers.Factory[]{
							new TradeFactory()
								.buyItem1(Items.EMERALD).buyItem1Max(2)
								.sellItem(Items.COBWEB).sellItemMin(32).sellItemMax(64)
								.maxUses(2),
							new TradeFactory()
								.buyItem1(Items.STRING).buyItem1Min(16).buyItem1Max(32)
								.sellItem(Items.LAPIS_LAZULI).sellItemMax(8)
								.maxUses(8),
						}
					)
				)
			);
			map.put(
				VillagerProfession.SHEPHERD,
				copyToFastUtilMap(
					ImmutableMap.of(
						1,
						new TradeOffers.Factory[]{
							new TradeFactory()
								.buyItem1(Items.WHITE_DYE).buyItem1Min(4).buyItem1Max(8)
								.sellItem(Items.COPPER_INGOT)
								.maxUses(16).experience(2).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.ORANGE_DYE).buyItem1Min(4).buyItem1Max(8)
								.sellItem(Items.COPPER_INGOT)
								.maxUses(16).experience(2).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.MAGENTA_DYE).buyItem1Min(4).buyItem1Max(8)
								.sellItem(Items.COPPER_INGOT)
								.maxUses(16).experience(2).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.LIGHT_BLUE_DYE).buyItem1Min(4).buyItem1Max(8)
								.sellItem(Items.COPPER_INGOT)
								.maxUses(16).experience(2).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.YELLOW_DYE).buyItem1Min(4).buyItem1Max(8)
								.sellItem(Items.COPPER_INGOT)
								.maxUses(16).experience(2).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.LIME_DYE).buyItem1Min(4).buyItem1Max(8)
								.sellItem(Items.COPPER_INGOT)
								.maxUses(16).experience(2).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.PINK_DYE).buyItem1Min(4).buyItem1Max(8)
								.sellItem(Items.COPPER_INGOT)
								.maxUses(16).experience(2).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.GRAY_DYE).buyItem1Min(4).buyItem1Max(8)
								.sellItem(Items.COPPER_INGOT)
								.maxUses(16).experience(2).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.LIGHT_GRAY_DYE).buyItem1Min(4).buyItem1Max(8)
								.sellItem(Items.COPPER_INGOT)
								.maxUses(16).experience(2).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.CYAN_DYE).buyItem1Min(4).buyItem1Max(8)
								.sellItem(Items.COPPER_INGOT)
								.maxUses(16).experience(2).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.PURPLE_DYE).buyItem1Min(4).buyItem1Max(8)
								.sellItem(Items.COPPER_INGOT)
								.maxUses(16).experience(2).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.BLUE_DYE).buyItem1Min(4).buyItem1Max(8)
								.sellItem(Items.COPPER_INGOT)
								.maxUses(16).experience(2).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.BROWN_DYE).buyItem1Min(4).buyItem1Max(8)
								.sellItem(Items.COPPER_INGOT)
								.maxUses(16).experience(2).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.GREEN_DYE).buyItem1Min(4).buyItem1Max(8)
								.sellItem(Items.COPPER_INGOT)
								.maxUses(16).experience(2).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.RED_DYE).buyItem1Min(4).buyItem1Max(8)
								.sellItem(Items.COPPER_INGOT)
								.maxUses(16).experience(2).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.BLACK_DYE).buyItem1Min(4).buyItem1Max(8)
								.sellItem(Items.COPPER_INGOT)
								.maxUses(16).experience(2).multiplier(0.05f),
						},
						2,
						new TradeOffers.Factory[]{
							new TradeFactory()
								.buyItem1(Items.IRON_INGOT).buyItem1Max(4)
								.sellItem(Items.WHITE_WOOL).sellItemMin(4).sellItemMax(8)
								.maxUses(8).experience(5).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.IRON_INGOT).buyItem1Max(4)
								.sellItem(Items.ORANGE_WOOL).sellItemMin(4).sellItemMax(8)
								.maxUses(8).experience(5).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.IRON_INGOT).buyItem1Max(4)
								.sellItem(Items.MAGENTA_WOOL).sellItemMin(4).sellItemMax(8)
								.maxUses(8).experience(5).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.IRON_INGOT).buyItem1Max(4)
								.sellItem(Items.LIGHT_BLUE_WOOL).sellItemMin(4).sellItemMax(8)
								.maxUses(8).experience(5).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.IRON_INGOT).buyItem1Max(4)
								.sellItem(Items.YELLOW_WOOL).sellItemMin(4).sellItemMax(8)
								.maxUses(8).experience(5).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.IRON_INGOT).buyItem1Max(4)
								.sellItem(Items.LIME_WOOL).sellItemMin(4).sellItemMax(8)
								.maxUses(8).experience(5).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.IRON_INGOT).buyItem1Max(4)
								.sellItem(Items.PINK_WOOL).sellItemMin(4).sellItemMax(8)
								.maxUses(8).experience(5).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.IRON_INGOT).buyItem1Max(4)
								.sellItem(Items.GRAY_WOOL).sellItemMin(4).sellItemMax(8)
								.maxUses(8).experience(5).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.IRON_INGOT).buyItem1Max(4)
								.sellItem(Items.LIGHT_GRAY_WOOL).sellItemMin(4).sellItemMax(8)
								.maxUses(8).experience(5).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.IRON_INGOT).buyItem1Max(4)
								.sellItem(Items.CYAN_WOOL).sellItemMin(4).sellItemMax(8)
								.maxUses(8).experience(5).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.IRON_INGOT).buyItem1Max(4)
								.sellItem(Items.PURPLE_WOOL).sellItemMin(4).sellItemMax(8)
								.maxUses(8).experience(5).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.IRON_INGOT).buyItem1Max(4)
								.sellItem(Items.BLUE_WOOL).sellItemMin(4).sellItemMax(8)
								.maxUses(8).experience(5).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.IRON_INGOT).buyItem1Max(4)
								.sellItem(Items.BROWN_WOOL).sellItemMin(4).sellItemMax(8)
								.maxUses(8).experience(5).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.IRON_INGOT).buyItem1Max(4)
								.sellItem(Items.GREEN_WOOL).sellItemMin(4).sellItemMax(8)
								.maxUses(8).experience(5).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.IRON_INGOT).buyItem1Max(4)
								.sellItem(Items.RED_WOOL).sellItemMin(4).sellItemMax(8)
								.maxUses(8).experience(5).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.IRON_INGOT).buyItem1Max(4)
								.sellItem(Items.BLACK_WOOL).sellItemMin(4).sellItemMax(8)
								.maxUses(8).experience(5).multiplier(0.05f),
						},
						3,
						new TradeOffers.Factory[]{
							new TradeFactory()
								.buyItem1(Items.LAPIS_LAZULI).buyItem1Min(16).buyItem1Max(32)
								.sellItem(Items.WHITE_DYE).sellItemMin(4).sellItemMax(8)
								.maxUses(8).experience(10).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.LAPIS_LAZULI).buyItem1Min(16).buyItem1Max(32)
								.sellItem(Items.ORANGE_DYE).sellItemMin(4).sellItemMax(8)
								.maxUses(8).experience(10).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.LAPIS_LAZULI).buyItem1Min(16).buyItem1Max(32)
								.sellItem(Items.MAGENTA_DYE).sellItemMin(4).sellItemMax(8)
								.maxUses(8).experience(10).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.LAPIS_LAZULI).buyItem1Min(16).buyItem1Max(32)
								.sellItem(Items.LIGHT_BLUE_DYE).sellItemMin(4).sellItemMax(8)
								.maxUses(8).experience(10).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.LAPIS_LAZULI).buyItem1Min(16).buyItem1Max(32)
								.sellItem(Items.YELLOW_DYE).sellItemMin(4).sellItemMax(8)
								.maxUses(8).experience(10).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.LAPIS_LAZULI).buyItem1Min(16).buyItem1Max(32)
								.sellItem(Items.LIME_DYE).sellItemMin(4).sellItemMax(8)
								.maxUses(8).experience(10).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.LAPIS_LAZULI).buyItem1Min(16).buyItem1Max(32)
								.sellItem(Items.PINK_DYE).sellItemMin(4).sellItemMax(8)
								.maxUses(8).experience(10).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.LAPIS_LAZULI).buyItem1Min(16).buyItem1Max(32)
								.sellItem(Items.GRAY_DYE).sellItemMin(4).sellItemMax(8)
								.maxUses(8).experience(10).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.LAPIS_LAZULI).buyItem1Min(16).buyItem1Max(32)
								.sellItem(Items.LIGHT_GRAY_DYE).sellItemMin(4).sellItemMax(8)
								.maxUses(8).experience(10).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.LAPIS_LAZULI).buyItem1Min(16).buyItem1Max(32)
								.sellItem(Items.CYAN_DYE).sellItemMin(4).sellItemMax(8)
								.maxUses(8).experience(10).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.LAPIS_LAZULI).buyItem1Min(16).buyItem1Max(32)
								.sellItem(Items.PURPLE_DYE).sellItemMin(4).sellItemMax(8)
								.maxUses(8).experience(10).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.LAPIS_LAZULI).buyItem1Min(16).buyItem1Max(32)
								.sellItem(Items.BLUE_DYE).sellItemMin(4).sellItemMax(8)
								.maxUses(8).experience(10).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.LAPIS_LAZULI).buyItem1Min(16).buyItem1Max(32)
								.sellItem(Items.BROWN_DYE).sellItemMin(4).sellItemMax(8)
								.maxUses(8).experience(10).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.LAPIS_LAZULI).buyItem1Min(16).buyItem1Max(32)
								.sellItem(Items.GREEN_DYE).sellItemMin(4).sellItemMax(8)
								.maxUses(8).experience(10).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.LAPIS_LAZULI).buyItem1Min(16).buyItem1Max(32)
								.sellItem(Items.RED_DYE).sellItemMin(4).sellItemMax(8)
								.maxUses(8).experience(10).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.LAPIS_LAZULI).buyItem1Min(16).buyItem1Max(32)
								.sellItem(Items.BLACK_DYE).sellItemMin(4).sellItemMax(8)
								.maxUses(8).experience(10).multiplier(0.05f),
						},
						4,
						new TradeOffers.Factory[]{
							new TradeFactory()
								.buyItem1(Items.DIAMOND).buyItem1Min(16).buyItem1Max(48)
								.sellItemGenerator((entity, random) -> {
									Optional<RegistryEntry.Reference<Enchantment>> luckOfTheSea = entity.getWorld().getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(Enchantments.LUCK_OF_THE_SEA);
									return luckOfTheSea.isPresent() ? EnchantedBookItem.forEnchantment(new EnchantmentLevelEntry(luckOfTheSea.get(), 3)) : Items.BOOK.getDefaultStack();
								})
								.maxUses(2).experience(40).multiplier(0.01f),
						},
						5,
						new TradeOffers.Factory[]{
							new TradeFactory()
								.buyItem1(Items.EMERALD).buyItem1Max(8)
								.sellItem(Items.BELL)
								.maxUses(2).multiplier(0.01f),
						}
					)
				)
			);
			map.put(
				VillagerProfession.FLETCHER,
				copyToFastUtilMap(
					ImmutableMap.of(
						1,
						new TradeOffers.Factory[]{
							new TradeFactory()
								.buyItem1(Items.COPPER_INGOT).buyItem1Max(4)
								.sellItem(Items.ARROW).sellItemMin(4).sellItemMax(8)
								.maxUses(8).experience(1).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.FEATHER).buyItem1Min(4).buyItem1Max(8)
								.sellItem(Items.COPPER_INGOT)
								.maxUses(8).experience(1).multiplier(0.05f),
						},
						2,
						new TradeOffers.Factory[]{new TradeOffers.BuyItemFactory(Items.FLINT, 26, 12, 10), new TradeOffers.SellItemFactory(Items.BOW, 2, 1, 5)},
						3,
						new TradeOffers.Factory[]{new TradeOffers.BuyItemFactory(Items.STRING, 14, 16, 20), new TradeOffers.SellItemFactory(Items.CROSSBOW, 3, 1, 10)},
						4,
						new TradeOffers.Factory[]{new TradeOffers.BuyItemFactory(Items.FEATHER, 24, 16, 30), new TradeOffers.SellEnchantedToolFactory(Items.BOW, 2, 3, 15)},
						5,
						new TradeOffers.Factory[]{
							new TradeOffers.BuyItemFactory(Items.TRIPWIRE_HOOK, 8, 12, 30),
							new TradeOffers.SellEnchantedToolFactory(Items.CROSSBOW, 3, 3, 15),
							new TradeOffers.SellPotionHoldingItemFactory(Items.ARROW, 5, Items.TIPPED_ARROW, 5, 2, 12, 30)
						}
					)
				)
			);
			map.put(
				VillagerProfession.LIBRARIAN,
				copyToFastUtilMap(
					ImmutableMap.<Integer, TradeOffers.Factory[]>builder()
						.put(
							1,
							new TradeOffers.Factory[]{
								new TradeOffers.BuyItemFactory(Items.PAPER, 24, 16, 2),
								new TradeOffers.EnchantBookFactory(1, EnchantmentTags.TRADEABLE),
								new TradeOffers.SellItemFactory(Blocks.BOOKSHELF, 9, 1, 12, 1)
							}
						)
						.put(
							2,
							new TradeOffers.Factory[]{
								new TradeOffers.BuyItemFactory(Items.BOOK, 4, 12, 10),
								new TradeOffers.EnchantBookFactory(5, EnchantmentTags.TRADEABLE),
								new TradeOffers.SellItemFactory(Items.LANTERN, 1, 1, 5)
							}
						)
						.put(
							3,
							new TradeOffers.Factory[]{
								new TradeOffers.BuyItemFactory(Items.INK_SAC, 5, 12, 20),
								new TradeOffers.EnchantBookFactory(10, EnchantmentTags.TRADEABLE),
								new TradeOffers.SellItemFactory(Items.GLASS, 1, 4, 10)
							}
						)
						.put(
							4,
							new TradeOffers.Factory[]{
								new TradeOffers.BuyItemFactory(Items.WRITABLE_BOOK, 2, 12, 30),
								new TradeOffers.EnchantBookFactory(15, EnchantmentTags.TRADEABLE),
								new TradeOffers.SellItemFactory(Items.CLOCK, 5, 1, 15),
								new TradeOffers.SellItemFactory(Items.COMPASS, 4, 1, 15)
							}
						)
						.put(5, new TradeOffers.Factory[]{new TradeOffers.SellItemFactory(Items.NAME_TAG, 20, 1, 30)})
						.build()
				)
			);
			map.put(
				VillagerProfession.CARTOGRAPHER,
				copyToFastUtilMap(
					ImmutableMap.of(
						1,
						new TradeOffers.Factory[]{new TradeOffers.BuyItemFactory(Items.PAPER, 24, 16, 2), new TradeOffers.SellItemFactory(Items.MAP, 7, 1, 1)},
						2,
						new TradeOffers.Factory[]{
							new TradeOffers.BuyItemFactory(Items.GLASS_PANE, 11, 16, 10),
							new TradeOffers.SellMapFactory(13, StructureTags.ON_OCEAN_EXPLORER_MAPS, "filled_map.monument", MapDecorationTypes.MONUMENT, 12, 5)
						},
						3,
						new TradeOffers.Factory[]{
							new TradeOffers.BuyItemFactory(Items.COMPASS, 1, 12, 20),
							new TradeOffers.SellMapFactory(14, StructureTags.ON_WOODLAND_EXPLORER_MAPS, "filled_map.mansion", MapDecorationTypes.MANSION, 12, 10),
							new TradeOffers.SellMapFactory(12, StructureTags.ON_TRIAL_CHAMBERS_MAPS, "filled_map.trial_chambers", MapDecorationTypes.TRIAL_CHAMBERS, 12, 10)
						},
						4,
						new TradeOffers.Factory[]{
							new TradeOffers.SellItemFactory(Items.ITEM_FRAME, 7, 1, 15),
							new TradeOffers.SellItemFactory(Items.WHITE_BANNER, 3, 1, 15),
							new TradeOffers.SellItemFactory(Items.BLUE_BANNER, 3, 1, 15),
							new TradeOffers.SellItemFactory(Items.LIGHT_BLUE_BANNER, 3, 1, 15),
							new TradeOffers.SellItemFactory(Items.RED_BANNER, 3, 1, 15),
							new TradeOffers.SellItemFactory(Items.PINK_BANNER, 3, 1, 15),
							new TradeOffers.SellItemFactory(Items.GREEN_BANNER, 3, 1, 15),
							new TradeOffers.SellItemFactory(Items.LIME_BANNER, 3, 1, 15),
							new TradeOffers.SellItemFactory(Items.GRAY_BANNER, 3, 1, 15),
							new TradeOffers.SellItemFactory(Items.BLACK_BANNER, 3, 1, 15),
							new TradeOffers.SellItemFactory(Items.PURPLE_BANNER, 3, 1, 15),
							new TradeOffers.SellItemFactory(Items.MAGENTA_BANNER, 3, 1, 15),
							new TradeOffers.SellItemFactory(Items.CYAN_BANNER, 3, 1, 15),
							new TradeOffers.SellItemFactory(Items.BROWN_BANNER, 3, 1, 15),
							new TradeOffers.SellItemFactory(Items.YELLOW_BANNER, 3, 1, 15),
							new TradeOffers.SellItemFactory(Items.ORANGE_BANNER, 3, 1, 15),
							new TradeOffers.SellItemFactory(Items.LIGHT_GRAY_BANNER, 3, 1, 15)
						},
						5,
						new TradeOffers.Factory[]{new TradeOffers.SellItemFactory(Items.GLOBE_BANNER_PATTERN, 8, 1, 30)}
					)
				)
			);
			map.put(
				VillagerProfession.CLERIC,
				copyToFastUtilMap(
					ImmutableMap.of(
						1,
						new TradeOffers.Factory[]{new TradeOffers.BuyItemFactory(Items.ROTTEN_FLESH, 32, 16, 2), new TradeOffers.SellItemFactory(Items.REDSTONE, 1, 2, 1)},
						2,
						new TradeOffers.Factory[]{new TradeOffers.BuyItemFactory(Items.GOLD_INGOT, 3, 12, 10), new TradeOffers.SellItemFactory(Items.LAPIS_LAZULI, 1, 1, 5)},
						3,
						new TradeOffers.Factory[]{new TradeOffers.BuyItemFactory(Items.RABBIT_FOOT, 2, 12, 20), new TradeOffers.SellItemFactory(Blocks.GLOWSTONE, 4, 1, 12, 10)},
						4,
						new TradeOffers.Factory[]{
							new TradeOffers.BuyItemFactory(Items.TURTLE_SCUTE, 4, 12, 30),
							new TradeOffers.BuyItemFactory(Items.GLASS_BOTTLE, 9, 12, 30),
							new TradeOffers.SellItemFactory(Items.ENDER_PEARL, 5, 1, 15)
						},
						5,
						new TradeOffers.Factory[]{
							new TradeOffers.BuyItemFactory(Items.NETHER_WART, 22, 12, 30), new TradeOffers.SellItemFactory(Items.EXPERIENCE_BOTTLE, 3, 1, 30)
						}
					)
				)
			);
			map.put(
				VillagerProfession.ARMORER,
				copyToFastUtilMap(
					ImmutableMap.of(
						1,
						new TradeOffers.Factory[]{
							new TradeOffers.BuyItemFactory(Items.COAL, 15, 16, 2),
							new TradeOffers.SellItemFactory(new ItemStack(Items.IRON_LEGGINGS), 7, 1, 12, 1, 0.2F),
							new TradeOffers.SellItemFactory(new ItemStack(Items.IRON_BOOTS), 4, 1, 12, 1, 0.2F),
							new TradeOffers.SellItemFactory(new ItemStack(Items.IRON_HELMET), 5, 1, 12, 1, 0.2F),
							new TradeOffers.SellItemFactory(new ItemStack(Items.IRON_CHESTPLATE), 9, 1, 12, 1, 0.2F)
						},
						2,
						new TradeOffers.Factory[]{
							new TradeOffers.BuyItemFactory(Items.IRON_INGOT, 4, 12, 10),
							new TradeOffers.SellItemFactory(new ItemStack(Items.BELL), 36, 1, 12, 5, 0.2F),
							new TradeOffers.SellItemFactory(new ItemStack(Items.CHAINMAIL_BOOTS), 1, 1, 12, 5, 0.2F),
							new TradeOffers.SellItemFactory(new ItemStack(Items.CHAINMAIL_LEGGINGS), 3, 1, 12, 5, 0.2F)
						},
						3,
						new TradeOffers.Factory[]{
							new TradeOffers.BuyItemFactory(Items.LAVA_BUCKET, 1, 12, 20),
							new TradeOffers.BuyItemFactory(Items.DIAMOND, 1, 12, 20),
							new TradeOffers.SellItemFactory(new ItemStack(Items.CHAINMAIL_HELMET), 1, 1, 12, 10, 0.2F),
							new TradeOffers.SellItemFactory(new ItemStack(Items.CHAINMAIL_CHESTPLATE), 4, 1, 12, 10, 0.2F),
							new TradeOffers.SellItemFactory(new ItemStack(Items.SHIELD), 5, 1, 12, 10, 0.2F)
						},
						4,
						new TradeOffers.Factory[]{
							new TradeOffers.SellEnchantedToolFactory(Items.DIAMOND_LEGGINGS, 14, 3, 15, 0.2F),
							new TradeOffers.SellEnchantedToolFactory(Items.DIAMOND_BOOTS, 8, 3, 15, 0.2F)
						},
						5,
						new TradeOffers.Factory[]{
							new TradeOffers.SellEnchantedToolFactory(Items.DIAMOND_HELMET, 8, 3, 30, 0.2F),
							new TradeOffers.SellEnchantedToolFactory(Items.DIAMOND_CHESTPLATE, 16, 3, 30, 0.2F)
						}
					)
				)
			);
			map.put(
				VillagerProfession.WEAPONSMITH,
				copyToFastUtilMap(
					ImmutableMap.of(
						1,
						new TradeOffers.Factory[]{
							new TradeOffers.BuyItemFactory(Items.COAL, 15, 16, 2),
							new TradeOffers.SellItemFactory(new ItemStack(Items.IRON_AXE), 3, 1, 12, 1, 0.2F),
							new TradeOffers.SellEnchantedToolFactory(Items.IRON_SWORD, 2, 3, 1)
						},
						2,
						new TradeOffers.Factory[]{
							new TradeOffers.BuyItemFactory(Items.IRON_INGOT, 4, 12, 10), new TradeOffers.SellItemFactory(new ItemStack(Items.BELL), 36, 1, 12, 5, 0.2F)
						},
						3,
						new TradeOffers.Factory[]{new TradeOffers.BuyItemFactory(Items.FLINT, 24, 12, 20)},
						4,
						new TradeOffers.Factory[]{
							new TradeOffers.BuyItemFactory(Items.DIAMOND, 1, 12, 30), new TradeOffers.SellEnchantedToolFactory(Items.DIAMOND_AXE, 12, 3, 15, 0.2F)
						},
						5,
						new TradeOffers.Factory[]{new TradeOffers.SellEnchantedToolFactory(Items.DIAMOND_SWORD, 8, 3, 30, 0.2F)}
					)
				)
			);
			map.put(
				VillagerProfession.TOOLSMITH,
				copyToFastUtilMap(
					ImmutableMap.of(
						1,
						new TradeOffers.Factory[]{
							new TradeOffers.BuyItemFactory(Items.COAL, 15, 16, 2),
							new TradeOffers.SellItemFactory(new ItemStack(Items.STONE_AXE), 1, 1, 12, 1, 0.2F),
							new TradeOffers.SellItemFactory(new ItemStack(Items.STONE_SHOVEL), 1, 1, 12, 1, 0.2F),
							new TradeOffers.SellItemFactory(new ItemStack(Items.STONE_PICKAXE), 1, 1, 12, 1, 0.2F),
							new TradeOffers.SellItemFactory(new ItemStack(Items.STONE_HOE), 1, 1, 12, 1, 0.2F)
						},
						2,
						new TradeOffers.Factory[]{
							new TradeOffers.BuyItemFactory(Items.IRON_INGOT, 4, 12, 10), new TradeOffers.SellItemFactory(new ItemStack(Items.BELL), 36, 1, 12, 5, 0.2F)
						},
						3,
						new TradeOffers.Factory[]{
							new TradeOffers.BuyItemFactory(Items.FLINT, 30, 12, 20),
							new TradeOffers.SellEnchantedToolFactory(Items.IRON_AXE, 1, 3, 10, 0.2F),
							new TradeOffers.SellEnchantedToolFactory(Items.IRON_SHOVEL, 2, 3, 10, 0.2F),
							new TradeOffers.SellEnchantedToolFactory(Items.IRON_PICKAXE, 3, 3, 10, 0.2F),
							new TradeOffers.SellItemFactory(new ItemStack(Items.DIAMOND_HOE), 4, 1, 3, 10, 0.2F)
						},
						4,
						new TradeOffers.Factory[]{
							new TradeOffers.BuyItemFactory(Items.DIAMOND, 1, 12, 30),
							new TradeOffers.SellEnchantedToolFactory(Items.DIAMOND_AXE, 12, 3, 15, 0.2F),
							new TradeOffers.SellEnchantedToolFactory(Items.DIAMOND_SHOVEL, 5, 3, 15, 0.2F)
						},
						5,
						new TradeOffers.Factory[]{new TradeOffers.SellEnchantedToolFactory(Items.DIAMOND_PICKAXE, 13, 3, 30, 0.2F)}
					)
				)
			);
			map.put(
				VillagerProfession.BUTCHER,
				copyToFastUtilMap(
					ImmutableMap.of(
						1,
						new TradeOffers.Factory[]{
							new TradeOffers.BuyItemFactory(Items.CHICKEN, 14, 16, 2),
							new TradeOffers.BuyItemFactory(Items.PORKCHOP, 7, 16, 2),
							new TradeOffers.BuyItemFactory(Items.RABBIT, 4, 16, 2),
							new TradeOffers.SellItemFactory(Items.RABBIT_STEW, 1, 1, 1)
						},
						2,
						new TradeOffers.Factory[]{
							new TradeOffers.BuyItemFactory(Items.COAL, 15, 16, 2),
							new TradeOffers.SellItemFactory(Items.COOKED_PORKCHOP, 1, 5, 16, 5),
							new TradeOffers.SellItemFactory(Items.COOKED_CHICKEN, 1, 8, 16, 5)
						},
						3,
						new TradeOffers.Factory[]{new TradeOffers.BuyItemFactory(Items.MUTTON, 7, 16, 20), new TradeOffers.BuyItemFactory(Items.BEEF, 10, 16, 20)},
						4,
						new TradeOffers.Factory[]{new TradeOffers.BuyItemFactory(Items.DRIED_KELP_BLOCK, 10, 12, 30)},
						5,
						new TradeOffers.Factory[]{new TradeOffers.BuyItemFactory(Items.SWEET_BERRIES, 10, 12, 30)}
					)
				)
			);
			map.put(
				VillagerProfession.LEATHERWORKER,
				copyToFastUtilMap(
					ImmutableMap.of(
						1,
						new TradeOffers.Factory[]{
							new TradeOffers.BuyItemFactory(Items.LEATHER, 6, 16, 2),
							new TradeOffers.SellDyedArmorFactory(Items.LEATHER_LEGGINGS, 3),
							new TradeOffers.SellDyedArmorFactory(Items.LEATHER_CHESTPLATE, 7)
						},
						2,
						new TradeOffers.Factory[]{
							new TradeOffers.BuyItemFactory(Items.FLINT, 26, 12, 10),
							new TradeOffers.SellDyedArmorFactory(Items.LEATHER_HELMET, 5, 12, 5),
							new TradeOffers.SellDyedArmorFactory(Items.LEATHER_BOOTS, 4, 12, 5)
						},
						3,
						new TradeOffers.Factory[]{
							new TradeOffers.BuyItemFactory(Items.RABBIT_HIDE, 9, 12, 20), new TradeOffers.SellDyedArmorFactory(Items.LEATHER_CHESTPLATE, 7)
						},
						4,
						new TradeOffers.Factory[]{
							new TradeOffers.BuyItemFactory(Items.TURTLE_SCUTE, 4, 12, 30), new TradeOffers.SellDyedArmorFactory(Items.LEATHER_HORSE_ARMOR, 6, 12, 15)
						},
						5,
						new TradeOffers.Factory[]{
							new TradeOffers.SellItemFactory(new ItemStack(Items.SADDLE), 6, 1, 12, 30, 0.2F), new TradeOffers.SellDyedArmorFactory(Items.LEATHER_HELMET, 5, 12, 30)
						}
					)
				)
			);
			map.put(
				VillagerProfession.MASON,
				copyToFastUtilMap(
					ImmutableMap.of(
						1,
						new TradeOffers.Factory[]{new TradeOffers.BuyItemFactory(Items.CLAY_BALL, 10, 16, 2), new TradeOffers.SellItemFactory(Items.BRICK, 1, 10, 16, 1)},
						2,
						new TradeOffers.Factory[]{
							new TradeOffers.BuyItemFactory(Blocks.STONE, 20, 16, 10), new TradeOffers.SellItemFactory(Blocks.CHISELED_STONE_BRICKS, 1, 4, 16, 5)
						},
						3,
						new TradeOffers.Factory[]{
							new TradeOffers.BuyItemFactory(Blocks.GRANITE, 16, 16, 20),
							new TradeOffers.BuyItemFactory(Blocks.ANDESITE, 16, 16, 20),
							new TradeOffers.BuyItemFactory(Blocks.DIORITE, 16, 16, 20),
							new TradeOffers.SellItemFactory(Blocks.DRIPSTONE_BLOCK, 1, 4, 16, 10),
							new TradeOffers.SellItemFactory(Blocks.POLISHED_ANDESITE, 1, 4, 16, 10),
							new TradeOffers.SellItemFactory(Blocks.POLISHED_DIORITE, 1, 4, 16, 10),
							new TradeOffers.SellItemFactory(Blocks.POLISHED_GRANITE, 1, 4, 16, 10)
						},
						4,
						new TradeOffers.Factory[]{
							new TradeOffers.BuyItemFactory(Items.QUARTZ, 12, 12, 30),
							new TradeOffers.SellItemFactory(Blocks.ORANGE_TERRACOTTA, 1, 1, 12, 15),
							new TradeOffers.SellItemFactory(Blocks.WHITE_TERRACOTTA, 1, 1, 12, 15),
							new TradeOffers.SellItemFactory(Blocks.BLUE_TERRACOTTA, 1, 1, 12, 15),
							new TradeOffers.SellItemFactory(Blocks.LIGHT_BLUE_TERRACOTTA, 1, 1, 12, 15),
							new TradeOffers.SellItemFactory(Blocks.GRAY_TERRACOTTA, 1, 1, 12, 15),
							new TradeOffers.SellItemFactory(Blocks.LIGHT_GRAY_TERRACOTTA, 1, 1, 12, 15),
							new TradeOffers.SellItemFactory(Blocks.BLACK_TERRACOTTA, 1, 1, 12, 15),
							new TradeOffers.SellItemFactory(Blocks.RED_TERRACOTTA, 1, 1, 12, 15),
							new TradeOffers.SellItemFactory(Blocks.PINK_TERRACOTTA, 1, 1, 12, 15),
							new TradeOffers.SellItemFactory(Blocks.MAGENTA_TERRACOTTA, 1, 1, 12, 15),
							new TradeOffers.SellItemFactory(Blocks.LIME_TERRACOTTA, 1, 1, 12, 15),
							new TradeOffers.SellItemFactory(Blocks.GREEN_TERRACOTTA, 1, 1, 12, 15),
							new TradeOffers.SellItemFactory(Blocks.CYAN_TERRACOTTA, 1, 1, 12, 15),
							new TradeOffers.SellItemFactory(Blocks.PURPLE_TERRACOTTA, 1, 1, 12, 15),
							new TradeOffers.SellItemFactory(Blocks.YELLOW_TERRACOTTA, 1, 1, 12, 15),
							new TradeOffers.SellItemFactory(Blocks.BROWN_TERRACOTTA, 1, 1, 12, 15),
							new TradeOffers.SellItemFactory(Blocks.ORANGE_GLAZED_TERRACOTTA, 1, 1, 12, 15),
							new TradeOffers.SellItemFactory(Blocks.WHITE_GLAZED_TERRACOTTA, 1, 1, 12, 15),
							new TradeOffers.SellItemFactory(Blocks.BLUE_GLAZED_TERRACOTTA, 1, 1, 12, 15),
							new TradeOffers.SellItemFactory(Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA, 1, 1, 12, 15),
							new TradeOffers.SellItemFactory(Blocks.GRAY_GLAZED_TERRACOTTA, 1, 1, 12, 15),
							new TradeOffers.SellItemFactory(Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA, 1, 1, 12, 15),
							new TradeOffers.SellItemFactory(Blocks.BLACK_GLAZED_TERRACOTTA, 1, 1, 12, 15),
							new TradeOffers.SellItemFactory(Blocks.RED_GLAZED_TERRACOTTA, 1, 1, 12, 15),
							new TradeOffers.SellItemFactory(Blocks.PINK_GLAZED_TERRACOTTA, 1, 1, 12, 15),
							new TradeOffers.SellItemFactory(Blocks.MAGENTA_GLAZED_TERRACOTTA, 1, 1, 12, 15),
							new TradeOffers.SellItemFactory(Blocks.LIME_GLAZED_TERRACOTTA, 1, 1, 12, 15),
							new TradeOffers.SellItemFactory(Blocks.GREEN_GLAZED_TERRACOTTA, 1, 1, 12, 15),
							new TradeOffers.SellItemFactory(Blocks.CYAN_GLAZED_TERRACOTTA, 1, 1, 12, 15),
							new TradeOffers.SellItemFactory(Blocks.PURPLE_GLAZED_TERRACOTTA, 1, 1, 12, 15),
							new TradeOffers.SellItemFactory(Blocks.YELLOW_GLAZED_TERRACOTTA, 1, 1, 12, 15),
							new TradeOffers.SellItemFactory(Blocks.BROWN_GLAZED_TERRACOTTA, 1, 1, 12, 15)
						},
						5,
						new TradeOffers.Factory[]{
							new TradeOffers.SellItemFactory(Blocks.QUARTZ_PILLAR, 1, 1, 12, 30), new TradeOffers.SellItemFactory(Blocks.QUARTZ_BLOCK, 1, 1, 12, 30)
						}
					)
				)
			);
		}
	);
    
    @Shadow
    public abstract VillagerData getVillagerData();

    @ModifyVariable(method = "fillRecipes", at = @At("STORE"), ordinal = 0)
    private Int2ObjectMap<TradeOffers.Factory[]> replaceVillagerTrades(Int2ObjectMap<TradeOffers.Factory[]> map) {
        return CUSTOM_TRADES.get(this.getVillagerData().getProfession());
    }
}
