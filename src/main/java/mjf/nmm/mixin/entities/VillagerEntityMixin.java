package mjf.nmm.mixin.entities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FireworkExplosionComponent;
import net.minecraft.component.type.FireworksComponent;
import net.minecraft.component.type.PotionContentsComponent;
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
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapDecorationTypes;
import net.minecraft.item.map.MapState;
import net.minecraft.potion.Potion;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.StructureTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
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
								.buyItem1(Items.DIAMOND).buyItem1Min(32).buyItem1Max(64)
								.sellItemGenerator((entity, random) -> {
									Optional<RegistryEntry.Reference<Enchantment>> enchantment = 
										entity.getWorld().getRegistryManager().get(RegistryKeys.ENCHANTMENT)
										.getEntry(Enchantments.LURE);
									return enchantment.isPresent() ? EnchantedBookItem.forEnchantment(
										new EnchantmentLevelEntry(enchantment.get(), 3)) : Items.BOOK.getDefaultStack();
								})
								.maxUses(2).experience(40).multiplier(0.01f),
								new TradeFactory()
									.buyItem1(Items.DIAMOND).buyItem1Min(32).buyItem1Max(64)
									.sellItemGenerator((entity, random) -> {
										Optional<RegistryEntry.Reference<Enchantment>> enchantment = 
											entity.getWorld().getRegistryManager().get(RegistryKeys.ENCHANTMENT)
											.getEntry(Enchantments.LUCK_OF_THE_SEA);
										return enchantment.isPresent() ? EnchantedBookItem.forEnchantment(
											new EnchantmentLevelEntry(enchantment.get(), 3)) : Items.BOOK.getDefaultStack();
									})
									.maxUses(2).experience(40).multiplier(0.01f),
								new TradeFactory()
									.buyItem1(Items.DIAMOND).buyItem1Min(32).buyItem1Max(64)
									.sellItemGenerator((entity, random) -> {
										Optional<RegistryEntry.Reference<Enchantment>> enchantment = 
											entity.getWorld().getRegistryManager().get(RegistryKeys.ENCHANTMENT)
											.getEntry(Enchantments.LUCK_OF_THE_SEA);
										return enchantment.isPresent() ? EnchantedBookItem.forEnchantment(
											new EnchantmentLevelEntry(enchantment.get(), 3)) : Items.BOOK.getDefaultStack();
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
								.buyItem1(Items.STRING).buyItem1Min(4).buyItem1Max(8)
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
									Optional<RegistryEntry.Reference<Enchantment>> enchantment = 
										entity.getWorld().getRegistryManager().get(RegistryKeys.ENCHANTMENT)
										.getEntry(Enchantments.SILK_TOUCH);
									return enchantment.isPresent() ? EnchantedBookItem.forEnchantment(
										new EnchantmentLevelEntry(enchantment.get(), 1)) : Items.BOOK.getDefaultStack();
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
						new TradeOffers.Factory[]{
							new TradeFactory()
								.buyItem1(Items.FLINT).buyItem1Min(4).buyItem1Max(8)
								.sellItem(Items.IRON_INGOT)
								.maxUses(16).experience(5).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.IRON_INGOT).buyItem1Min(4).buyItem1Max(8)
								.sellItem(Items.ARROW).sellItemMin(8).sellItemMax(16)
								.maxUses(8).experience(5).multiplier(0.02f),
						},
						3,
						new TradeOffers.Factory[]{
							new TradeFactory()
								.buyItem1(Items.GUNPOWDER).buyItem1Min(4).buyItem1Max(8)
								.sellItem(Items.LAPIS_LAZULI).sellItemMin(8).sellItemMax(16)
								.maxUses(8).experience(10).multiplier(0.02f),
						},
						4,
						new TradeOffers.Factory[]{
							new TradeFactory()
								.buyItem1(Items.DIAMOND).buyItem1Min(16).buyItem1Max(48)
								.sellItemGenerator((entity, random) -> {
									Optional<RegistryEntry.Reference<Enchantment>> enchantment = 
										entity.getWorld().getRegistryManager().get(RegistryKeys.ENCHANTMENT)
										.getEntry(Enchantments.POWER);
									return enchantment.isPresent() ? EnchantedBookItem.forEnchantment(
										new EnchantmentLevelEntry(enchantment.get(), 5)) : Items.BOOK.getDefaultStack();
								})
								.maxUses(2).experience(40).multiplier(0.01f),
							new TradeFactory()
								.buyItem1(Items.DIAMOND).buyItem1Min(16).buyItem1Max(48)
								.sellItemGenerator((entity, random) -> {
									Optional<RegistryEntry.Reference<Enchantment>> enchantment = 
										entity.getWorld().getRegistryManager().get(RegistryKeys.ENCHANTMENT)
										.getEntry(Enchantments.PUNCH);
									return enchantment.isPresent() ? EnchantedBookItem.forEnchantment(
										new EnchantmentLevelEntry(enchantment.get(), 2)) : Items.BOOK.getDefaultStack();
								})
								.maxUses(2).experience(40).multiplier(0.01f),
							new TradeFactory()
								.buyItem1(Items.DIAMOND).buyItem1Min(16).buyItem1Max(48)
								.sellItemGenerator((entity, random) -> {
									Optional<RegistryEntry.Reference<Enchantment>> enchantment = 
										entity.getWorld().getRegistryManager().get(RegistryKeys.ENCHANTMENT)
										.getEntry(Enchantments.INFINITY);
									return enchantment.isPresent() ? EnchantedBookItem.forEnchantment(
										new EnchantmentLevelEntry(enchantment.get(), 1)) : Items.BOOK.getDefaultStack();
								})
								.maxUses(2).experience(40).multiplier(0.01f),
							new TradeFactory()
								.buyItem1(Items.DIAMOND).buyItem1Min(16).buyItem1Max(48)
								.sellItemGenerator((entity, random) -> {
									Optional<RegistryEntry.Reference<Enchantment>> enchantment = 
										entity.getWorld().getRegistryManager().get(RegistryKeys.ENCHANTMENT)
										.getEntry(Enchantments.FLAME);
									return enchantment.isPresent() ? EnchantedBookItem.forEnchantment(
										new EnchantmentLevelEntry(enchantment.get(), 1)) : Items.BOOK.getDefaultStack();
								})
								.maxUses(2).experience(40).multiplier(0.01f),
							new TradeFactory()
								.buyItem1(Items.DIAMOND).buyItem1Min(16).buyItem1Max(48)
								.sellItemGenerator((entity, random) -> {
									Optional<RegistryEntry.Reference<Enchantment>> enchantment = 
										entity.getWorld().getRegistryManager().get(RegistryKeys.ENCHANTMENT)
										.getEntry(Enchantments.MULTISHOT);
									return enchantment.isPresent() ? EnchantedBookItem.forEnchantment(
										new EnchantmentLevelEntry(enchantment.get(), 1)) : Items.BOOK.getDefaultStack();
								})
								.maxUses(2).experience(40).multiplier(0.01f),
							new TradeFactory()
								.buyItem1(Items.DIAMOND).buyItem1Min(16).buyItem1Max(48)
								.sellItemGenerator((entity, random) -> {
									Optional<RegistryEntry.Reference<Enchantment>> enchantment = 
										entity.getWorld().getRegistryManager().get(RegistryKeys.ENCHANTMENT)
										.getEntry(Enchantments.PIERCING);
									return enchantment.isPresent() ? EnchantedBookItem.forEnchantment(
										new EnchantmentLevelEntry(enchantment.get(), 4)) : Items.BOOK.getDefaultStack();
								})
								.maxUses(2).experience(40).multiplier(0.01f),
							new TradeFactory()
								.buyItem1(Items.DIAMOND).buyItem1Min(16).buyItem1Max(48)
								.sellItemGenerator((entity, random) -> {
									Optional<RegistryEntry.Reference<Enchantment>> enchantment = 
										entity.getWorld().getRegistryManager().get(RegistryKeys.ENCHANTMENT)
										.getEntry(Enchantments.QUICK_CHARGE);
									return enchantment.isPresent() ? EnchantedBookItem.forEnchantment(
										new EnchantmentLevelEntry(enchantment.get(), 3)) : Items.BOOK.getDefaultStack();
								})
								.maxUses(2).experience(40).multiplier(0.01f),
						},
						5,
						new TradeOffers.Factory[]{
							new TradeFactory()
								.buyItem1(Items.EMERALD)
								.sellItemMax(8)
								.sellItemGenerator((entity, random) -> {
									List<FireworkExplosionComponent> explosionList = new ArrayList<>();
									for (int i = 0; i < 8; ++i) {
										explosionList.add(new FireworkExplosionComponent(
											FireworkExplosionComponent.Type.BURST, 
											IntList.of(random.nextInt(256) << 16, random.nextInt(256) << 8, random.nextInt(256)), 
											IntList.of(random.nextInt(256) << 16, random.nextInt(256) << 8, random.nextInt(256)), 
											false, 
											false));
									}

									ItemStack rocket = Items.FIREWORK_ROCKET.getDefaultStack();
									rocket.set(DataComponentTypes.FIREWORKS, new FireworksComponent(1 + random.nextInt(3),  explosionList));
									return rocket;
								})
								.maxUses(8),
							new TradeFactory()
								.buyItem1(Items.EMERALD)
								.sellItemMin(8).sellItemMax(16)
								.sellItemGenerator((entity, random) -> {
									List<RegistryEntry<Potion>> list = Registries.POTION.streamEntries().filter((entry) -> {
										return !(entry.value()).getEffects().isEmpty() && entity.getWorld().getBrewingRecipeRegistry().isBrewable(entry);
									}).collect(Collectors.toList());
									RegistryEntry<Potion> potion = Util.getRandom(list, random);

									ItemStack arrow = Items.TIPPED_ARROW.getDefaultStack();
									arrow.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(potion));
									return arrow;
								})
								.maxUses(8),
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
								new TradeFactory()
									.buyItem1(Items.PAPER).buyItem1Min(16).buyItem1Max(32)
									.sellItem(Items.COPPER_INGOT)
									.maxUses(12).experience(2).multiplier(0.05f),
								new TradeFactory()
									.buyItem1(Items.COPPER_INGOT).buyItem1Min(4).buyItem1Max(8)
									.sellItem(Items.BOOK)
									.maxUses(12).experience(2).multiplier(0.05f),
							}
						)
						.put(
							2,
							new TradeOffers.Factory[]{
								new TradeFactory()
									.buyItem1(Items.BOOK)
									.sellItem(Items.IRON_INGOT).sellItemMax(2)
									.maxUses(8).experience(5).multiplier(0.05f),
								new TradeFactory()
									.buyItem1(Items.FEATHER).buyItem1Min(4).buyItem1Max(8)
									.sellItem(Items.IRON_INGOT).sellItemMax(2)
									.maxUses(8).experience(5).multiplier(0.05f),
							}
						)
						.put(
							3,
							new TradeOffers.Factory[]{
								new TradeFactory()
									.buyItem1(Items.INK_SAC).buyItem1Min(4).buyItem1Max(8)
									.sellItem(Items.LAPIS_LAZULI).sellItemMin(4).sellItemMax(8)
									.maxUses(8).experience(10).multiplier(0.05f),
							}
						)
						.put(
							4,
							new TradeOffers.Factory[]{
								new TradeFactory()
									.buyItem1(Items.DIAMOND).buyItem1Min(16).buyItem1Max(48)
									.sellItemGenerator((entity, random) -> {
										Optional<RegistryEntry.Reference<Enchantment>> enchantment = 
											entity.getWorld().getRegistryManager().get(RegistryKeys.ENCHANTMENT)
											.getEntry(Enchantments.POWER);
										return enchantment.isPresent() ? EnchantedBookItem.forEnchantment(
											new EnchantmentLevelEntry(enchantment.get(), 5)) : Items.BOOK.getDefaultStack();
									})
									.maxUses(2).experience(40).multiplier(0.01f),
							}
						)
						.put(5, 
							new TradeOffers.Factory[]{
								new TradeFactory()
									.buyItem1(Items.EMERALD)
									.sellItem(Items.NAME_TAG)
									.maxUses(4),
							}
						)
						.build()
				)
			);
			map.put(
				VillagerProfession.CARTOGRAPHER,
				copyToFastUtilMap(
					ImmutableMap.of(
						1,
						new TradeOffers.Factory[]{
							new TradeFactory()
								.buyItem1(Items.PAPER).buyItem1Min(16).buyItem1Max(32)
								.sellItem(Items.COPPER_INGOT)
								.maxUses(12).experience(2).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.COPPER_INGOT).buyItem1Min(4).buyItem1Max(8)
								.sellItem(Items.COMPASS)
								.maxUses(4).experience(2).multiplier(0.05f),
						},
						2,
						new TradeOffers.Factory[]{
							new TradeFactory()
								.buyItem1(Items.IRON_INGOT).buyItem1Min(4).buyItem1Max(8)
								.sellItem(Items.CLOCK)
								.maxUses(4).experience(5).multiplier(0.05f),
						},
						3,
						new TradeOffers.Factory[]{
							new TradeFactory()
								.buyItem1(Items.LAPIS_LAZULI).buyItem1Min(48).buyItem1Max(64)
								.sellItemGenerator((entity, random) -> {
									if (entity.getWorld() instanceof ServerWorld serverWorld) {
										BlockPos blockPos = serverWorld.locateStructure(StructureTags.ON_OCEAN_EXPLORER_MAPS, entity.getBlockPos(), 100, true);
										if (blockPos != null) {
											ItemStack mapItem = FilledMapItem.createMap(serverWorld, blockPos.getX(), blockPos.getZ(), (byte)2, true, true);
											FilledMapItem.fillExplorationMap(serverWorld, mapItem);
											MapState.addDecorationsNbt(mapItem, blockPos, "+", MapDecorationTypes.MONUMENT);
											mapItem.set(DataComponentTypes.ITEM_NAME, Text.translatable("filled_map.monument"));
											return mapItem;
										}
									}
									return Items.MAP.getDefaultStack();
								})
								.maxUses(1).experience(20).multiplier(0.01f),
							new TradeFactory()
								.buyItem1(Items.LAPIS_LAZULI).buyItem1Min(48).buyItem1Max(64)
								.sellItemGenerator((entity, random) -> {
									if (entity.getWorld() instanceof ServerWorld serverWorld) {
										BlockPos blockPos = serverWorld.locateStructure(StructureTags.ON_WOODLAND_EXPLORER_MAPS, entity.getBlockPos(), 100, true);
										if (blockPos != null) {
											ItemStack mapItem = FilledMapItem.createMap(serverWorld, blockPos.getX(), blockPos.getZ(), (byte)2, true, true);
											FilledMapItem.fillExplorationMap(serverWorld, mapItem);
											MapState.addDecorationsNbt(mapItem, blockPos, "+", MapDecorationTypes.MANSION);
											mapItem.set(DataComponentTypes.ITEM_NAME, Text.translatable("filled_map.mansion"));
											return mapItem;
										}
									}
									return Items.MAP.getDefaultStack();
								})
								.maxUses(1).experience(20).multiplier(0.01f),
							new TradeFactory()
								.buyItem1(Items.LAPIS_LAZULI).buyItem1Min(48).buyItem1Max(64)
								.sellItemGenerator((entity, random) -> {
									if (entity.getWorld() instanceof ServerWorld serverWorld) {
										BlockPos blockPos = serverWorld.locateStructure(StructureTags.ON_TRIAL_CHAMBERS_MAPS, entity.getBlockPos(), 100, true);
										if (blockPos != null) {
											ItemStack mapItem = FilledMapItem.createMap(serverWorld, blockPos.getX(), blockPos.getZ(), (byte)2, true, true);
											FilledMapItem.fillExplorationMap(serverWorld, mapItem);
											MapState.addDecorationsNbt(mapItem, blockPos, "+", MapDecorationTypes.TRIAL_CHAMBERS);
											mapItem.set(DataComponentTypes.ITEM_NAME, Text.translatable("filled_map.trial_chambers"));
											return mapItem;
										}
									}
									return Items.MAP.getDefaultStack();
								})
								.maxUses(1).experience(20).multiplier(0.01f),
						},
						4,
						new TradeOffers.Factory[]{
							new TradeFactory()
								.buyItem1(Items.DIAMOND).buyItem1Min(32).buyItem1Max(64)
								.sellItemGenerator((entity, random) -> {
									Optional<RegistryEntry.Reference<Enchantment>> enchantment = 
										entity.getWorld().getRegistryManager().get(RegistryKeys.ENCHANTMENT)
										.getEntry(Enchantments.DEPTH_STRIDER);
									return enchantment.isPresent() ? EnchantedBookItem.forEnchantment(
										new EnchantmentLevelEntry(enchantment.get(), 3)) : Items.BOOK.getDefaultStack();
								})
								.maxUses(2).experience(40).multiplier(0.01f),
						},
						5,
						new TradeOffers.Factory[]{
							new TradeFactory()
								.buyItem1(Items.EMERALD).buyItem1Min(4).buyItem1Max(8)
								.sellItem(Items.GLOBE_BANNER_PATTERN)
								.maxUses(2),
						}
					)
				)
			);
			map.put(
				VillagerProfession.CLERIC,
				copyToFastUtilMap(
					ImmutableMap.of(
						1,
						new TradeOffers.Factory[]{
							new TradeFactory()
								.buyItem1(Items.ROTTEN_FLESH).buyItem1Min(16).buyItem1Max(24)
								.sellItem(Items.COPPER_INGOT)
								.maxUses(16).experience(2).multiplier(0.05f),
						},
						2,
						new TradeOffers.Factory[]{
							new TradeFactory()
								.buyItem1(Items.SPIDER_EYE).buyItem1Max(4)
								.sellItem(Items.IRON_INGOT)
								.maxUses(16).experience(5).multiplier(0.05f),
						},
						3,
						new TradeOffers.Factory[]{
							new TradeFactory()
								.buyItem1(Items.LAPIS_LAZULI).buyItem1Min(16).buyItem1Max(32)
								.sellItem(Items.EXPERIENCE_BOTTLE).sellItemMin(4).sellItemMax(8)
								.maxUses(16).experience(10).multiplier(0.05f),
						},
						4,
						new TradeOffers.Factory[]{
							new TradeFactory()
								.buyItem1(Items.DIAMOND).buyItem1Min(32).buyItem1Max(64)
								.sellItemGenerator((entity, random) -> {
									Optional<RegistryEntry.Reference<Enchantment>> enchantment = 
										entity.getWorld().getRegistryManager().get(RegistryKeys.ENCHANTMENT)
										.getEntry(Enchantments.FEATHER_FALLING);
									return enchantment.isPresent() ? EnchantedBookItem.forEnchantment(
										new EnchantmentLevelEntry(enchantment.get(), 4)) : Items.BOOK.getDefaultStack();
								})
								.maxUses(2).experience(40).multiplier(0.01f),
						},
						5,
						new TradeOffers.Factory[]{
							new TradeFactory()
								.buyItem1(Items.EMERALD).buyItem1Max(4)
								.sellItem(Items.ENDER_PEARL).sellItemMax(2)
								.maxUses(4)
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
							new TradeFactory()
								.buyItem1(Items.COPPER_INGOT).buyItem1Min(2).buyItem1Max(4)
								.sellItem(Items.SHIELD)
								.maxUses(16).experience(2).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.COAL).buyItem1Min(4).buyItem1Max(8)
								.sellItem(Items.COPPER_INGOT)
								.maxUses(16).experience(2).multiplier(0.05f),
						},
						2,
						new TradeOffers.Factory[]{
							new TradeFactory()
								.buyItem1(Items.RAW_COPPER).buyItem1Min(2).buyItem1Max(4)
								.sellItem(Items.IRON_INGOT)
								.maxUses(16).experience(5).multiplier(0.01f),
							new TradeFactory()
								.buyItem1(Items.RAW_IRON).buyItem1Min(2).buyItem1Max(4)
								.sellItem(Items.IRON_INGOT)
								.maxUses(16).experience(5).multiplier(0.02f),
						},
						3,
						new TradeOffers.Factory[]{
							new TradeFactory()
								.buyItem1(Items.LAPIS_LAZULI).buyItem1Min(48).buyItem1Max(64)
								.sellItem(Items.CHAINMAIL_BOOTS)
								.maxUses(4).experience(10).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.LAPIS_LAZULI).buyItem1Min(48).buyItem1Max(64)
								.sellItem(Items.CHAINMAIL_LEGGINGS)
								.maxUses(4).experience(10).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.LAPIS_LAZULI).buyItem1Min(48).buyItem1Max(64)
								.sellItem(Items.CHAINMAIL_CHESTPLATE)
								.maxUses(4).experience(10).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.LAPIS_LAZULI).buyItem1Min(48).buyItem1Max(64)
								.sellItem(Items.CHAINMAIL_HELMET)
								.maxUses(4).experience(10).multiplier(0.05f),
						},
						4,
						new TradeOffers.Factory[]{
							new TradeFactory()
								.buyItem1(Items.DIAMOND).buyItem1Min(32).buyItem1Max(64)
								.sellItemGenerator((entity, random) -> {
									Optional<RegistryEntry.Reference<Enchantment>> enchantment = 
										entity.getWorld().getRegistryManager().get(RegistryKeys.ENCHANTMENT)
										.getEntry(Enchantments.PROTECTION);
									return enchantment.isPresent() ? EnchantedBookItem.forEnchantment(
										new EnchantmentLevelEntry(enchantment.get(), 4)) : Items.BOOK.getDefaultStack();
								})
								.maxUses(2).experience(40).multiplier(0.01f),
							new TradeFactory()
								.buyItem1(Items.DIAMOND).buyItem1Min(32).buyItem1Max(64)
								.sellItemGenerator((entity, random) -> {
									Optional<RegistryEntry.Reference<Enchantment>> enchantment = 
										entity.getWorld().getRegistryManager().get(RegistryKeys.ENCHANTMENT)
										.getEntry(Enchantments.PROJECTILE_PROTECTION);
									return enchantment.isPresent() ? EnchantedBookItem.forEnchantment(
										new EnchantmentLevelEntry(enchantment.get(), 4)) : Items.BOOK.getDefaultStack();
								})
								.maxUses(2).experience(40).multiplier(0.01f),
							new TradeFactory()
								.buyItem1(Items.DIAMOND).buyItem1Min(32).buyItem1Max(64)
								.sellItemGenerator((entity, random) -> {
									Optional<RegistryEntry.Reference<Enchantment>> enchantment = 
										entity.getWorld().getRegistryManager().get(RegistryKeys.ENCHANTMENT)
										.getEntry(Enchantments.BLAST_PROTECTION);
									return enchantment.isPresent() ? EnchantedBookItem.forEnchantment(
										new EnchantmentLevelEntry(enchantment.get(), 4)) : Items.BOOK.getDefaultStack();
								})
								.maxUses(2).experience(40).multiplier(0.01f),
							new TradeFactory()
								.buyItem1(Items.DIAMOND).buyItem1Min(32).buyItem1Max(64)
								.sellItemGenerator((entity, random) -> {
									Optional<RegistryEntry.Reference<Enchantment>> enchantment = 
										entity.getWorld().getRegistryManager().get(RegistryKeys.ENCHANTMENT)
										.getEntry(Enchantments.FIRE_PROTECTION);
									return enchantment.isPresent() ? EnchantedBookItem.forEnchantment(
										new EnchantmentLevelEntry(enchantment.get(), 4)) : Items.BOOK.getDefaultStack();
								})
								.maxUses(2).experience(40).multiplier(0.01f),
						},
						5,
						new TradeOffers.Factory[]{
							new TradeFactory()
								.buyItem1(Items.EMERALD).buyItem1Max(4)
								.sellItem(Items.DIAMOND_HELMET)
								.maxUses(2).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.EMERALD).buyItem1Max(4)
								.sellItem(Items.DIAMOND_CHESTPLATE)
								.maxUses(2).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.EMERALD).buyItem1Max(4)
								.sellItem(Items.DIAMOND_LEGGINGS)
								.maxUses(2).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.EMERALD).buyItem1Max(4)
								.sellItem(Items.DIAMOND_BOOTS)
								.maxUses(2).multiplier(0.05f),
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
							new TradeFactory()
								.buyItem1(Items.COAL).buyItem1Min(4).buyItem1Max(8)
								.sellItem(Items.COPPER_INGOT)
								.maxUses(16).experience(2).multiplier(0.05f),
						},
						2,
						new TradeOffers.Factory[]{
							new TradeFactory()
								.buyItem1(Items.RAW_COPPER).buyItem1Min(2).buyItem1Max(4)
								.sellItem(Items.IRON_INGOT)
								.maxUses(16).experience(5).multiplier(0.01f),
							new TradeFactory()
								.buyItem1(Items.RAW_IRON).buyItem1Min(2).buyItem1Max(4)
								.sellItem(Items.IRON_INGOT)
								.maxUses(16).experience(5).multiplier(0.02f),
						},
						3,
						new TradeOffers.Factory[]{
							new TradeFactory()
								.buyItem1(Items.LAPIS_LAZULI).buyItem1Min(32).buyItem1Max(48)
								.sellItem(Items.IRON_SWORD)
								.maxUses(4).experience(10).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.LAPIS_LAZULI).buyItem1Min(32).buyItem1Max(48)
								.sellItem(Items.IRON_AXE)
								.maxUses(4).experience(10).multiplier(0.05f),
						},
						4,
						new TradeOffers.Factory[]{
							new TradeFactory()
								.buyItem1(Items.DIAMOND).buyItem1Min(32).buyItem1Max(64)
								.sellItemGenerator((entity, random) -> {
									Optional<RegistryEntry.Reference<Enchantment>> enchantment = 
										entity.getWorld().getRegistryManager().get(RegistryKeys.ENCHANTMENT)
										.getEntry(Enchantments.SHARPNESS);
									return enchantment.isPresent() ? EnchantedBookItem.forEnchantment(
										new EnchantmentLevelEntry(enchantment.get(), 5)) : Items.BOOK.getDefaultStack();
								})
								.maxUses(2).experience(40).multiplier(0.01f),
							new TradeFactory()
								.buyItem1(Items.DIAMOND).buyItem1Min(32).buyItem1Max(64)
								.sellItemGenerator((entity, random) -> {
									Optional<RegistryEntry.Reference<Enchantment>> enchantment = 
										entity.getWorld().getRegistryManager().get(RegistryKeys.ENCHANTMENT)
										.getEntry(Enchantments.SMITE);
									return enchantment.isPresent() ? EnchantedBookItem.forEnchantment(
										new EnchantmentLevelEntry(enchantment.get(), 5)) : Items.BOOK.getDefaultStack();
								})
								.maxUses(2).experience(40).multiplier(0.01f),
							new TradeFactory()
								.buyItem1(Items.DIAMOND).buyItem1Min(32).buyItem1Max(64)
								.sellItemGenerator((entity, random) -> {
									Optional<RegistryEntry.Reference<Enchantment>> enchantment = 
										entity.getWorld().getRegistryManager().get(RegistryKeys.ENCHANTMENT)
										.getEntry(Enchantments.BANE_OF_ARTHROPODS);
									return enchantment.isPresent() ? EnchantedBookItem.forEnchantment(
										new EnchantmentLevelEntry(enchantment.get(), 5)) : Items.BOOK.getDefaultStack();
								})
								.maxUses(2).experience(40).multiplier(0.01f),
						},
						5,
						new TradeOffers.Factory[]{
							new TradeFactory()
								.buyItem1(Items.EMERALD).buyItem1Max(2)
								.sellItem(Items.DIAMOND_SWORD)
								.maxUses(4).multiplier(0.02f),
							new TradeFactory()
								.buyItem1(Items.EMERALD).buyItem1Max(2)
								.sellItem(Items.DIAMOND_AXE)
								.maxUses(4).multiplier(0.02f),
						}
					)
				)
			);
			map.put(
				VillagerProfession.TOOLSMITH,
				copyToFastUtilMap(
					ImmutableMap.of(
						1,
						new TradeOffers.Factory[]{
							new TradeFactory()
								.buyItem1(Items.COAL).buyItem1Min(4).buyItem1Max(8)
								.sellItem(Items.COPPER_INGOT)
								.maxUses(16).experience(2).multiplier(0.05f),
						},
						2,
						new TradeOffers.Factory[]{
							new TradeFactory()
								.buyItem1(Items.RAW_COPPER).buyItem1Min(2).buyItem1Max(4)
								.sellItem(Items.IRON_INGOT)
								.maxUses(16).experience(5).multiplier(0.01f),
							new TradeFactory()
								.buyItem1(Items.RAW_IRON).buyItem1Min(2).buyItem1Max(4)
								.sellItem(Items.IRON_INGOT)
								.maxUses(16).experience(5).multiplier(0.02f),
						},
						3,
						new TradeOffers.Factory[]{
							new TradeFactory()
								.buyItem1(Items.LAPIS_LAZULI).buyItem1Min(32).buyItem1Max(48)
								.sellItem(Items.IRON_PICKAXE)
								.maxUses(4).experience(10).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.LAPIS_LAZULI).buyItem1Min(32).buyItem1Max(48)
								.sellItem(Items.IRON_SHOVEL)
								.maxUses(4).experience(10).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.LAPIS_LAZULI).buyItem1Min(32).buyItem1Max(48)
								.sellItem(Items.IRON_HOE)
								.maxUses(4).experience(10).multiplier(0.05f),
						},
						4,
						new TradeOffers.Factory[]{
							new TradeFactory()
								.buyItem1(Items.DIAMOND).buyItem1Min(32).buyItem1Max(64)
								.sellItemGenerator((entity, random) -> {
									Optional<RegistryEntry.Reference<Enchantment>> enchantment = 
										entity.getWorld().getRegistryManager().get(RegistryKeys.ENCHANTMENT)
										.getEntry(Enchantments.EFFICIENCY);
									return enchantment.isPresent() ? EnchantedBookItem.forEnchantment(
										new EnchantmentLevelEntry(enchantment.get(), 5)) : Items.BOOK.getDefaultStack();
								})
								.maxUses(2).experience(40).multiplier(0.01f),
							new TradeFactory()
								.buyItem1(Items.DIAMOND).buyItem1Min(32).buyItem1Max(64)
								.sellItemGenerator((entity, random) -> {
									Optional<RegistryEntry.Reference<Enchantment>> enchantment = 
										entity.getWorld().getRegistryManager().get(RegistryKeys.ENCHANTMENT)
										.getEntry(Enchantments.UNBREAKING);
									return enchantment.isPresent() ? EnchantedBookItem.forEnchantment(
										new EnchantmentLevelEntry(enchantment.get(), 3)) : Items.BOOK.getDefaultStack();
								})
								.maxUses(2).experience(40).multiplier(0.01f),
						},
						5,
						new TradeOffers.Factory[]{
							new TradeFactory()
								.buyItem1(Items.EMERALD).buyItem1Max(2)
								.sellItem(Items.DIAMOND_PICKAXE)
								.maxUses(4).multiplier(0.02f),
							new TradeFactory()
								.buyItem1(Items.EMERALD).buyItem1Max(2)
								.sellItem(Items.DIAMOND_SHOVEL)
								.maxUses(4).multiplier(0.02f),
							new TradeFactory()
								.buyItem1(Items.EMERALD).buyItem1Max(2)
								.sellItem(Items.DIAMOND_HOE)
								.maxUses(4).multiplier(0.02f),
						}
					)
				)
			);
			map.put(
				VillagerProfession.BUTCHER,
				copyToFastUtilMap(
					ImmutableMap.of(
						1,
						new TradeOffers.Factory[]{
							new TradeFactory()
								.buyItem1(Items.COAL).buyItem1Min(4).buyItem1Max(8)
								.sellItem(Items.COPPER_INGOT)
								.maxUses(16).experience(2).multiplier(0.05f),
						},
						2,
						new TradeOffers.Factory[]{
							new TradeFactory()
								.buyItem1(Items.IRON_INGOT).buyItem1Max(4)
								.sellItem(Items.COOKED_CHICKEN)
								.maxUses(16).experience(5).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.IRON_INGOT).buyItem1Max(4)
								.sellItem(Items.COOKED_RABBIT)
								.maxUses(16).experience(5).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.IRON_INGOT).buyItem1Max(4)
								.sellItem(Items.COOKED_MUTTON)
								.maxUses(16).experience(5).multiplier(0.05f),
						},
						3,
						new TradeOffers.Factory[]{
							new TradeFactory()
								.buyItem1(Items.LAPIS_LAZULI).buyItem1Min(8).buyItem1Max(16)
								.sellItem(Items.COOKED_PORKCHOP)
								.maxUses(16).experience(10).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.LAPIS_LAZULI).buyItem1Min(8).buyItem1Max(16)
								.sellItem(Items.COOKED_BEEF)
								.maxUses(16).experience(10).multiplier(0.05f),
						},
						4,
						new TradeOffers.Factory[]{
							new TradeFactory()
								.buyItem1(Items.DIAMOND).buyItem1Min(32).buyItem1Max(64)
								.sellItemGenerator((entity, random) -> {
									Optional<RegistryEntry.Reference<Enchantment>> enchantment = 
										entity.getWorld().getRegistryManager().get(RegistryKeys.ENCHANTMENT)
										.getEntry(Enchantments.LOOTING);
									return enchantment.isPresent() ? EnchantedBookItem.forEnchantment(
										new EnchantmentLevelEntry(enchantment.get(), 3)) : Items.BOOK.getDefaultStack();
								})
								.maxUses(2).experience(40).multiplier(0.01f),
						},
						5,
						new TradeOffers.Factory[]{
							new TradeFactory()
								.buyItem1(Items.EMERALD).buyItem1Min(4).buyItem1Max(8)
								.sellItem(Items.PLAYER_HEAD)
								.maxUses(2).multiplier(0.01f),
						}
					)
				)
			);
			map.put(
				VillagerProfession.LEATHERWORKER,
				copyToFastUtilMap(
					ImmutableMap.of(
						1,
						new TradeOffers.Factory[]{
							new TradeFactory()
								.buyItem1(Items.LEATHER).buyItem1Min(4).buyItem1Max(8)
								.sellItem(Items.COPPER_INGOT)
								.maxUses(16).experience(2).multiplier(0.05f),
						},
						2,
						new TradeOffers.Factory[]{
							new TradeFactory()
								.buyItem1(Items.RABBIT_HIDE).buyItem1Min(4).buyItem1Max(8)
								.sellItem(Items.IRON_INGOT)
								.maxUses(16).experience(5).multiplier(0.05f),
						},
						3,
						new TradeOffers.Factory[]{
							new TradeFactory()
								.buyItem1(Items.LAPIS_LAZULI).buyItem1Min(16).buyItem1Max(32)
								.sellItem(Items.LEATHER)
								.maxUses(16).experience(10).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.LAPIS_LAZULI).buyItem1Min(16).buyItem1Max(32)
								.sellItem(Items.RABBIT_HIDE)
								.maxUses(16).experience(10).multiplier(0.05f),
						},
						4,
						new TradeOffers.Factory[]{
							new TradeFactory()
								.buyItem1(Items.DIAMOND).buyItem1Min(16).buyItem1Max(48)
								.sellItemGenerator((entity, random) -> {
									Optional<RegistryEntry.Reference<Enchantment>> enchantment = 
										entity.getWorld().getRegistryManager().get(RegistryKeys.ENCHANTMENT)
										.getEntry(Enchantments.POWER);
									return enchantment.isPresent() ? EnchantedBookItem.forEnchantment(
										new EnchantmentLevelEntry(enchantment.get(), 5)) : Items.BOOK.getDefaultStack();
								})
								.maxUses(2).experience(40).multiplier(0.01f),
						},
						5,
						new TradeOffers.Factory[]{
							new TradeFactory()
								.buyItem1(Items.EMERALD).buyItem1Min(2).buyItem1Max(4)
								.sellItem(Items.SADDLE)
								.maxUses(4).multiplier(0.05f),
						}
					)
				)
			);
			map.put(
				VillagerProfession.MASON,
				copyToFastUtilMap(
					ImmutableMap.of(
						1,
						new TradeOffers.Factory[]{
							new TradeFactory()
								.buyItem1(Items.CLAY_BALL).buyItem1Min(8).buyItem1Max(16)
								.sellItem(Items.COPPER_INGOT)
								.maxUses(16).experience(2).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.COPPER_INGOT)
								.sellItem(Items.BRICK).sellItemMin(2).sellItemMax(4)
								.maxUses(16).experience(2).multiplier(0.05f),
						},
						2,
						new TradeOffers.Factory[]{
							new TradeFactory()
								.buyItem1(Items.IRON_INGOT)
								.sellItem(Items.STONE_BRICKS).sellItemMin(8).sellItemMax(16)
								.maxUses(16).experience(5).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.STONE).buyItem1Min(8).buyItem1Max(16)
								.sellItem(Items.IRON_INGOT)
								.maxUses(16).experience(5).multiplier(0.05f),
						},
						3,
						new TradeOffers.Factory[]{
							new TradeFactory()
								.buyItem1(Items.LAPIS_LAZULI).buyItem1Min(8).buyItem1Max(16)
								.sellItem(Items.GRANITE).sellItemMin(8).sellItemMax(16)
								.maxUses(16).experience(10).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.LAPIS_LAZULI).buyItem1Min(8).buyItem1Max(16)
								.sellItem(Items.ANDESITE).sellItemMin(8).sellItemMax(16)
								.maxUses(16).experience(10).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.LAPIS_LAZULI).buyItem1Min(8).buyItem1Max(16)
								.sellItem(Items.DIORITE).sellItemMin(8).sellItemMax(16)
								.maxUses(16).experience(10).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.LAPIS_LAZULI).buyItem1Min(8).buyItem1Max(16)
								.sellItem(Items.DRIPSTONE_BLOCK).sellItemMin(8).sellItemMax(16)
								.maxUses(16).experience(10).multiplier(0.05f),
						},
						4,
						new TradeOffers.Factory[]{
							new TradeFactory()
								.buyItem1(Items.DIAMOND).buyItem1Min(32).buyItem1Max(64)
								.sellItemGenerator((entity, random) -> {
									Optional<RegistryEntry.Reference<Enchantment>> enchantment = 
										entity.getWorld().getRegistryManager().get(RegistryKeys.ENCHANTMENT)
										.getEntry(Enchantments.SILK_TOUCH);
									return enchantment.isPresent() ? EnchantedBookItem.forEnchantment(
										new EnchantmentLevelEntry(enchantment.get(), 1)) : Items.BOOK.getDefaultStack();
								})
								.maxUses(2).experience(40).multiplier(0.01f),
							new TradeFactory()
								.buyItem1(Items.DIAMOND).buyItem1Min(32).buyItem1Max(64)
								.sellItemGenerator((entity, random) -> {
									Optional<RegistryEntry.Reference<Enchantment>> enchantment = 
										entity.getWorld().getRegistryManager().get(RegistryKeys.ENCHANTMENT)
										.getEntry(Enchantments.FORTUNE);
									return enchantment.isPresent() ? EnchantedBookItem.forEnchantment(
										new EnchantmentLevelEntry(enchantment.get(), 3)) : Items.BOOK.getDefaultStack();
								})
								.maxUses(2).experience(40).multiplier(0.01f),
						},
						5,
						new TradeOffers.Factory[]{
							new TradeFactory()
								.buyItem1(Items.EMERALD).buyItem1Max(4)
								.sellItem(Items.QUARTZ_BLOCK).sellItemMin(32).sellItemMax(64)
								.maxUses(8).multiplier(0.05f),
							new TradeFactory()
								.buyItem1(Items.EMERALD).buyItem1Max(4)
								.sellItem(Items.TERRACOTTA).sellItemMin(32).sellItemMax(64)
								.maxUses(8).multiplier(0.05f)
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
