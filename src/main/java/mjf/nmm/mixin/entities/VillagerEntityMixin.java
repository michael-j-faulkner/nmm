package mjf.nmm.mixin.entities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapDecorationTypes;
import net.minecraft.item.map.MapState;
import net.minecraft.potion.Potion;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EnchantmentTags;
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
import net.minecraft.world.gen.structure.Structures;

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
		Optional<RegistryKey<VillagerProfession>> optional = this.getVillagerData().profession().getKey();
        return optional.isPresent() ? CUSTOM_TRADES.get(optional.get()) : null;
    }

	private static ItemStack enchantWithLevel(ItemStack itemStack, int level, Random random, Entity entity) {
		Registry<Enchantment> enchantmentRegistry = entity.getRegistryManager().getOrThrow(Enchantments.AQUA_AFFINITY.getRegistryRef());
		return EnchantmentHelper.enchant(random, itemStack, level, StreamSupport.stream(enchantmentRegistry.iterateEntries(EnchantmentTags.NON_TREASURE).spliterator(), false));
	}

	private static ItemStack getMaxBookWithOneOf(Random random, Entity entity, List<RegistryKey<Enchantment>> enchantments) {
		Registry<Enchantment> enchantmentRegistry = entity.getRegistryManager().getOrThrow(Enchantments.AQUA_AFFINITY.getRegistryRef());
		RegistryKey<Enchantment> enchantment = enchantments.get(random.nextInt(enchantments.size()));
		
		return EnchantmentHelper.getEnchantedBookWith(
			new EnchantmentLevelEntry(enchantmentRegistry.getEntry(enchantment.getValue()).get(), enchantmentRegistry.get(enchantment).getMaxLevel()));
	}

	private static final Map<RegistryKey<VillagerProfession>, Int2ObjectMap<TradeOffers.Factory[]>> CUSTOM_TRADES = Util.make(() -> {
		Map<RegistryKey<VillagerProfession>, Int2ObjectMap<TradeOffers.Factory[]>> trades = new HashMap<>();
		
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
				},
				3, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.DIAMOND).buyItem1Min(8).buyItem1Max(16)
						.sellItemGenerator((entity, random) -> enchantWithLevel(Items.DIAMOND_HELMET.getDefaultStack(), 10, random, entity))
						.experience(40).maxUses(2).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.DIAMOND).buyItem1Min(6).buyItem1Max(12)
						.sellItemGenerator((entity, random) -> enchantWithLevel(Items.DIAMOND_BOOTS.getDefaultStack(), 10, random, entity))
						.experience(40).maxUses(2).multiplier(0.01f),
				},
				4, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.DIAMOND).buyItem1Min(12).buyItem1Max(24)
						.sellItemGenerator((entity, random) -> enchantWithLevel(Items.DIAMOND_CHESTPLATE.getDefaultStack(), 10, random, entity))
						.experience(45).maxUses(2).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.DIAMOND).buyItem1Min(11).buyItem1Max(22)
						.sellItemGenerator((entity, random) -> enchantWithLevel(Items.DIAMOND_LEGGINGS.getDefaultStack(), 10, random, entity))
						.experience(45).maxUses(2).multiplier(0.01f),
				},
				5, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.EMERALD).buyItem1Min(8).buyItem1Max(16)
						.sellItem(Items.BELL),
					new TradeFactory()
						.buyItem1(Items.EMERALD).buyItem1Min(16).buyItem1Max(24)
						.sellItemGenerator((entity, random) -> getMaxBookWithOneOf(random, entity, List.of(
							Enchantments.AQUA_AFFINITY,
							Enchantments.BLAST_PROTECTION,
							Enchantments.DEPTH_STRIDER,
							Enchantments.FEATHER_FALLING,
							Enchantments.FIRE_PROTECTION,
							Enchantments.PROJECTILE_PROTECTION,
							Enchantments.PROTECTION,
							Enchantments.RESPIRATION,
							Enchantments.THORNS
						))).maxUses(2).multiplier(0.01f),
				}
			))
		);

		trades.put(VillagerProfession.BUTCHER, 
			new Int2ObjectOpenHashMap<>(Map.of(
				1, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.COAL).buyItem1Min(16).buyItem1Max(24)
						.sellItem(Items.IRON_INGOT)
						.experience(2).maxUses(16).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.SWEET_BERRIES).buyItem1Min(4).buyItem1Max(8)
						.sellItem(Items.IRON_INGOT)
						.experience(2).maxUses(16).multiplier(0.01f),
				},
				2, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.IRON_INGOT)
						.sellItem(Items.COOKED_RABBIT).sellItemMin(2).sellItemMax(4)
						.experience(5).maxUses(16).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.IRON_INGOT)
						.sellItem(Items.COOKED_MUTTON).sellItemMin(2).sellItemMax(4)
						.experience(5).maxUses(16).multiplier(0.01f),
				},
				3, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.IRON_INGOT)
						.sellItem(Items.COOKED_BEEF).sellItemMin(2).sellItemMax(4)
						.experience(10).maxUses(16).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.IRON_INGOT)
						.sellItem(Items.COOKED_PORKCHOP).sellItemMin(2).sellItemMax(4)
						.experience(10).maxUses(16).multiplier(0.01f),
				},
				4, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.DIAMOND)
						.sellItem(Items.LEATHER).sellItemMin(8).sellItemMax(16)
						.experience(20).maxUses(16).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.DIAMOND)
						.sellItem(Items.RABBIT_HIDE).sellItemMin(4).sellItemMax(12)
						.experience(20).maxUses(16).multiplier(0.01f),
				},
				5, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.EMERALD)
						.sellItem(Items.RABBIT_FOOT)
						.maxUses(4),
				}
			))
		);

		trades.put(VillagerProfession.CARTOGRAPHER, 
			new Int2ObjectOpenHashMap<>(Map.of(
				1, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.PAPER).buyItem1Min(16).buyItem1Max(24)
						.sellItem(Items.IRON_INGOT)
						.experience(2).maxUses(16).multiplier(0.01f),
				},
				2, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.DIAMOND).buyItem1Min(4).buyItem1Max(8)
						.sellItemGenerator((entity, random) -> {
							ServerWorld serverWorld = (ServerWorld)entity.getWorld();
							BlockPos blockPos = serverWorld.locateStructure(StructureTags.ON_TRIAL_CHAMBERS_MAPS, entity.getBlockPos(), 100, true);
							if (blockPos != null) {
								ItemStack map = FilledMapItem.createMap(serverWorld, blockPos.getX(), blockPos.getZ(), (byte)2, true, true);
								FilledMapItem.fillExplorationMap(serverWorld, map);
								MapState.addDecorationsNbt(map, blockPos, "+", MapDecorationTypes.TRIAL_CHAMBERS);
								map.set(DataComponentTypes.ITEM_NAME, Text.translatable("filled_map.trial_chambers"));
								return map;
							} else {
								return ItemStack.EMPTY;
							}
						}).experience(60).maxUses(16).multiplier(0.01f),
				},
				3, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.DIAMOND).buyItem1Min(4).buyItem1Max(8)
						.sellItemGenerator((entity, random) -> {
							ServerWorld serverWorld = (ServerWorld)entity.getWorld();
							BlockPos blockPos = serverWorld.locateStructure(StructureTags.ON_OCEAN_EXPLORER_MAPS, entity.getBlockPos(), 100, true);
							if (blockPos != null) {
								ItemStack map = FilledMapItem.createMap(serverWorld, blockPos.getX(), blockPos.getZ(), (byte)2, true, true);
								FilledMapItem.fillExplorationMap(serverWorld, map);
								MapState.addDecorationsNbt(map, blockPos, "+", MapDecorationTypes.MONUMENT);
								map.set(DataComponentTypes.ITEM_NAME, Text.translatable("filled_map.monument"));
								return map;
							} else {
								return ItemStack.EMPTY;
							}
						}).experience(80).maxUses(16).multiplier(0.01f),
				},
				4, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.DIAMOND).buyItem1Min(4).buyItem1Max(8)
						.sellItemGenerator((entity, random) -> {
							ServerWorld serverWorld = (ServerWorld)entity.getWorld();
							BlockPos blockPos = serverWorld.locateStructure(StructureTags.ON_WOODLAND_EXPLORER_MAPS, entity.getBlockPos(), 100, true);
							if (blockPos != null) {
								ItemStack map = FilledMapItem.createMap(serverWorld, blockPos.getX(), blockPos.getZ(), (byte)2, true, true);
								FilledMapItem.fillExplorationMap(serverWorld, map);
								MapState.addDecorationsNbt(map, blockPos, "+", MapDecorationTypes.MANSION);
								map.set(DataComponentTypes.ITEM_NAME, Text.translatable("filled_map.mansion"));
								return map;
							} else {
								return ItemStack.EMPTY;
							}
						}).experience(100).maxUses(16).multiplier(0.01f),
				},
				5, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.EMERALD).buyItem1Min(8).buyItem1Max(16)
						.sellItem(Items.GLOBE_BANNER_PATTERN)
				}
			))
		);

		trades.put(VillagerProfession.CLERIC, 
			new Int2ObjectOpenHashMap<>(Map.of(
				1, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.STRING).buyItem1Min(2).buyItem1Max(4)
						.sellItem(Items.IRON_INGOT)
						.experience(2).maxUses(16).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.ROTTEN_FLESH).buyItem1Min(4).buyItem1Max(8)
						.sellItem(Items.IRON_INGOT)
						.experience(2).maxUses(16).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.PHANTOM_MEMBRANE).buyItem1Min(2).buyItem1Max(4)
						.sellItem(Items.IRON_INGOT)
						.experience(2).maxUses(16).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.BONE).buyItem1Min(2).buyItem1Max(4)
						.sellItem(Items.IRON_INGOT)
						.experience(2).maxUses(16).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.GUNPOWDER).buyItem1Min(2).buyItem1Max(4)
						.sellItem(Items.IRON_INGOT)
						.experience(2).maxUses(16).multiplier(0.01f),
				},
				2, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.SLIME_BALL)
						.sellItem(Items.LAPIS_LAZULI).sellItemMin(4).sellItemMax(12)
						.experience(10).maxUses(16).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.SPIDER_EYE)
						.sellItem(Items.LAPIS_LAZULI).sellItemMin(4).sellItemMax(12)
						.experience(10).maxUses(16).multiplier(0.01f),
				},
				3, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.GHAST_TEAR)
						.sellItem(Items.EMERALD)
						.experience(20).maxUses(8),
					new TradeFactory()
						.buyItem1(Items.BLAZE_ROD)
						.sellItem(Items.EMERALD)
						.experience(20).maxUses(8),
					new TradeFactory()
						.buyItem1(Items.MAGMA_CREAM)
						.sellItem(Items.EMERALD)
						.experience(20).maxUses(8),
					new TradeFactory()
						.buyItem1(Items.ENDER_PEARL)
						.sellItem(Items.EMERALD)
						.experience(20).maxUses(8),
					new TradeFactory()
						.buyItem1(Items.DRAGON_BREATH)
						.sellItem(Items.EMERALD)
						.experience(20).maxUses(8),
					new TradeFactory()
						.buyItem1(Items.PRISMARINE_CRYSTALS)
						.sellItem(Items.EMERALD)
						.experience(20).maxUses(8),
				},
				4, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.WITHER_SKELETON_SKULL)
						.sellItemGenerator((entity, random) -> getMaxBookWithOneOf(random, entity, List.of(Enchantments.MENDING)))
						.experience(100).maxUses(2),
					new TradeFactory()
						.buyItem1(Items.HEAVY_CORE)
						.sellItemGenerator((entity, random) -> getMaxBookWithOneOf(random, entity, List.of(Enchantments.MENDING)))
						.experience(100).maxUses(2),
					new TradeFactory()
						.buyItem1(Items.DRAGON_EGG)
						.sellItemGenerator((entity, random) -> getMaxBookWithOneOf(random, entity, List.of(Enchantments.MENDING)))
						.experience(100).maxUses(2),
					new TradeFactory()
						.buyItem1(Items.ELYTRA)
						.sellItemGenerator((entity, random) -> getMaxBookWithOneOf(random, entity, List.of(Enchantments.MENDING)))
						.experience(100).maxUses(2),
					new TradeFactory()
						.buyItem1(Items.SHULKER_SHELL)
						.sellItemGenerator((entity, random) -> getMaxBookWithOneOf(random, entity, List.of(Enchantments.MENDING)))
						.experience(100).maxUses(2),
					new TradeFactory()
						.buyItem1(Items.CREAKING_HEART)
						.sellItemGenerator((entity, random) -> getMaxBookWithOneOf(random, entity, List.of(Enchantments.MENDING)))
						.experience(100).maxUses(2),
					new TradeFactory()
						.buyItem1(Items.TOTEM_OF_UNDYING)
						.sellItemGenerator((entity, random) -> getMaxBookWithOneOf(random, entity, List.of(Enchantments.MENDING)))
						.experience(100).maxUses(2),
					new TradeFactory()
						.buyItem1(Items.SPONGE).buyItem1Count(16)
						.sellItemGenerator((entity, random) -> getMaxBookWithOneOf(random, entity, List.of(Enchantments.MENDING)))
						.experience(100).maxUses(2),
				},
				5, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.WITHER_SKELETON_SKULL)
						.sellItem(Items.ENCHANTED_GOLDEN_APPLE)
						.maxUses(2),
					new TradeFactory()
						.buyItem1(Items.HEAVY_CORE)
						.sellItem(Items.ENCHANTED_GOLDEN_APPLE)
						.maxUses(2),
					new TradeFactory()
						.buyItem1(Items.DRAGON_EGG)
						.sellItem(Items.ENCHANTED_GOLDEN_APPLE)
						.maxUses(2),
					new TradeFactory()
						.buyItem1(Items.ELYTRA)
						.sellItem(Items.ENCHANTED_GOLDEN_APPLE)
						.maxUses(2),
					new TradeFactory()
						.buyItem1(Items.SHULKER_SHELL)
						.sellItem(Items.ENCHANTED_GOLDEN_APPLE)
						.maxUses(2),
					new TradeFactory()
						.buyItem1(Items.CREAKING_HEART)
						.sellItem(Items.ENCHANTED_GOLDEN_APPLE)
						.maxUses(2),
					new TradeFactory()
						.buyItem1(Items.TOTEM_OF_UNDYING)
						.sellItem(Items.ENCHANTED_GOLDEN_APPLE)
						.maxUses(2),
					new TradeFactory()
						.buyItem1(Items.SPONGE).buyItem1Count(16)
						.sellItem(Items.ENCHANTED_GOLDEN_APPLE)
						.maxUses(2),
				}
			))
		);

		trades.put(VillagerProfession.FARMER, 
			new Int2ObjectOpenHashMap<>(Map.of(
				1, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.WHEAT_SEEDS).buyItem1Min(8).buyItem1Max(16)
						.sellItem(Items.IRON_INGOT)
						.experience(2).maxUses(16).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.MELON_SEEDS).buyItem1Min(8).buyItem1Max(16)
						.sellItem(Items.IRON_INGOT)
						.experience(2).maxUses(16).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.PUMPKIN_SEEDS).buyItem1Min(8).buyItem1Max(16)
						.sellItem(Items.IRON_INGOT)
						.experience(2).maxUses(16).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.BEETROOT_SEEDS).buyItem1Min(8).buyItem1Max(16)
						.sellItem(Items.IRON_INGOT)
						.experience(2).maxUses(16).multiplier(0.01f),
				},
				2, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.IRON_INGOT)
						.sellItem(Items.WHEAT).sellItemMin(4).sellItemMax(8)
						.experience(5).maxUses(16).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.IRON_INGOT)
						.sellItem(Items.CARROT).sellItemMin(2).sellItemMax(6)
						.experience(5).maxUses(16).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.IRON_INGOT)
						.sellItem(Items.BAKED_POTATO).sellItemMin(2).sellItemMax(6)
						.experience(5).maxUses(16).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.IRON_INGOT)
						.sellItem(Items.BEETROOT).sellItemMin(2).sellItemMax(6)
						.experience(5).maxUses(16).multiplier(0.01f),
				},
				3, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.DIAMOND)
						.sellItem(Items.APPLE).sellItemMin(2).sellItemMax(4)
						.experience(10).maxUses(16).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.DIAMOND)
						.sellItem(Items.CAKE)
						.experience(10).maxUses(16).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.DIAMOND)
						.sellItem(Items.PUMPKIN_PIE).sellItemMin(2).sellItemMax(4)
						.experience(10).maxUses(16).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.DIAMOND)
						.sellItem(Items.COOKIE).sellItemMin(4).sellItemMax(8)
						.experience(10).maxUses(16).multiplier(0.01f),
				},
				4, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.DIAMOND)
						.sellItem(Items.GOLDEN_CARROT).sellItemMin(4).sellItemMax(8)
						.experience(20).maxUses(8).multiplier(0.01f),
				},
				5, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.EMERALD)
						.sellItem(Items.GOLDEN_APPLE).sellItemMin(4).sellItemMax(8)
						.maxUses(4)
				}
			))
		);

		trades.put(VillagerProfession.FISHERMAN, 
			new Int2ObjectOpenHashMap<>(Map.of(
				1, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.STRING).buyItem1Min(2).buyItem1Max(4)
						.sellItem(Items.IRON_INGOT)
						.experience(2).maxUses(16).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.ROTTEN_FLESH).buyItem1Min(4).buyItem1Max(8)
						.sellItem(Items.IRON_INGOT)
						.experience(2).maxUses(16).multiplier(0.01f),
				},
				2, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.IRON_INGOT)
						.sellItem(Items.COOKED_SALMON).sellItemMin(2).sellItemMax(4)
						.experience(5).maxUses(8).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.IRON_INGOT)
						.sellItem(Items.COOKED_COD).sellItemMin(2).sellItemMax(4)
						.experience(5).maxUses(8).multiplier(0.01f),
				},
				3, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.DIAMOND)
						.sellItem(Items.PUFFERFISH)
						.experience(20).maxUses(4).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.DIAMOND).buyItem1Count(2).buyItem1Max(4)
						.sellItemGenerator((entity, random) -> enchantWithLevel(Items.FISHING_ROD.getDefaultStack(), 15, random, entity))
						.experience(20).maxUses(2).multiplier(0.01f),
				},
				4, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.DIAMOND)
						.sellItem(Items.PUFFERFISH_BUCKET)
						.experience(25).maxUses(4).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.DIAMOND)
						.sellItem(Items.COD_BUCKET)
						.experience(25).maxUses(4).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.DIAMOND)
						.sellItem(Items.SALMON_BUCKET)
						.experience(25).maxUses(4).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.DIAMOND)
						.sellItem(Items.TROPICAL_FISH_BUCKET)
						.experience(25).maxUses(4).multiplier(0.01f),
				},
				5, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.EMERALD).buyItem1Min(16).buyItem1Max(24)
						.sellItemGenerator((entity, random) -> getMaxBookWithOneOf(random, entity, List.of(
							Enchantments.IMPALING,
							Enchantments.LOYALTY,
							Enchantments.LUCK_OF_THE_SEA,
							Enchantments.LURE,
							Enchantments.RIPTIDE
						))).maxUses(2).multiplier(0.01f),
				}
			))
		);

		trades.put(VillagerProfession.FLETCHER, 
			new Int2ObjectOpenHashMap<>(Map.of(
				1, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.FLINT).buyItem1Min(4).buyItem1Max(8)
						.sellItem(Items.IRON_INGOT)
						.experience(2).maxUses(16).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.FEATHER).buyItem1Min(4).buyItem1Max(8)
						.sellItem(Items.IRON_INGOT)
						.experience(2).maxUses(16).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.STRING).buyItem1Min(2).buyItem1Max(4)
						.sellItem(Items.IRON_INGOT)
						.experience(2).maxUses(16).multiplier(0.01f),
				},
				2, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.IRON_INGOT)
						.sellItem(Items.ARROW).sellItemCount(4)
						.experience(5).maxUses(16).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.IRON_INGOT).buyItem1Min(4).buyItem1Max(8)
						.sellItem(Items.BOW)
						.experience(20).maxUses(16).multiplier(0.01f),
				},
				3, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.DIAMOND).buyItem1Min(2).buyItem1Max(6)
						.sellItemGenerator((entity, random) -> enchantWithLevel(Items.BOW.getDefaultStack(), 15, random, entity))
						.experience(25).maxUses(2).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.DIAMOND).buyItem1Min(2).buyItem1Max(6)
						.sellItemGenerator((entity, random) -> enchantWithLevel(Items.CROSSBOW.getDefaultStack(), 15, random, entity))
						.experience(25).maxUses(2).multiplier(0.01f),
				},
				4, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.DIAMOND)
						.sellItemGenerator((entity, random) -> {
							RegistryEntry<Potion> potion = Util.getRandom(Registries.POTION.streamEntries()
								.filter(entry -> !(entry.value()).getEffects().isEmpty() && entity.getWorld().getBrewingRecipeRegistry().isBrewable(entry))
								.collect(Collectors.toList()), random);
							ItemStack arrow = Items.TIPPED_ARROW.getDefaultStack();
							arrow.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(potion));
							return arrow;
						}).sellItemCount(4)
						.experience(20).maxUses(16).multiplier(0.01f),
				},
				5, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.EMERALD).buyItem1Min(16).buyItem1Max(24)
						.sellItemGenerator((entity, random) -> getMaxBookWithOneOf(random, entity, List.of(
							Enchantments.FLAME,
							Enchantments.INFINITY,
							Enchantments.MULTISHOT,
							Enchantments.PIERCING,
							Enchantments.POWER,
							Enchantments.PUNCH,
							Enchantments.QUICK_CHARGE
						))).maxUses(2).multiplier(0.01f),
				}
			))
		);

		trades.put(VillagerProfession.LEATHERWORKER, 
			new Int2ObjectOpenHashMap<>(Map.of(
				1, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.LEATHER).buyItem1Min(4).buyItem1Max(8)
						.sellItem(Items.IRON_INGOT)
						.experience(2).maxUses(16).multiplier(0.01f),
				},
				2, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.IRON_INGOT).buyItem1Min(4).buyItem1Max(8)
						.sellItem(Items.LEATHER_HELMET)
						.experience(20).maxUses(4).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.IRON_INGOT).buyItem1Min(4).buyItem1Max(8)
						.sellItem(Items.LEATHER_BOOTS)
						.experience(20).maxUses(4).multiplier(0.01f),
				},
				3, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.IRON_INGOT).buyItem1Min(6).buyItem1Max(12)
						.sellItem(Items.LEATHER_CHESTPLATE)
						.experience(25).maxUses(4).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.IRON_INGOT).buyItem1Min(6).buyItem1Max(12)
						.sellItem(Items.LEATHER_LEGGINGS)
						.experience(25).maxUses(4).multiplier(0.01f),
				},
				4, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.IRON_INGOT).buyItem1Min(8).buyItem1Max(16)
						.sellItem(Items.LEATHER_HORSE_ARMOR)
						.experience(25).maxUses(4).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.RABBIT_HIDE).buyItem1Min(2).buyItem1Max(4)
						.sellItem(Items.IRON_INGOT)
						.experience(25).maxUses(16).multiplier(0.01f),
				},
				5, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.EMERALD).buyItem1Min(8).buyItem1Max(16)
						.sellItem(Items.SADDLE)
						.maxUses(2),
				}
			))
		);

		trades.put(VillagerProfession.LIBRARIAN, 
			new Int2ObjectOpenHashMap<>(Map.of(
				1, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.PAPER).buyItem1Min(16).buyItem1Max(24)
						.sellItem(Items.IRON_INGOT)
						.experience(2).maxUses(16).multiplier(0.01f),
				},
				2, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.IRON_INGOT).buyItem1Min(2).buyItem1Max(4)
						.sellItem(Items.BOOK)
						.experience(10).maxUses(16).multiplier(0.01f),
				},
				3, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.IRON_INGOT)
						.sellItem(Items.GLASS).sellItemMin(2).sellItemMax(6)
						.experience(10).maxUses(32).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.IRON_INGOT).buyItem1Min(2).buyItem1Max(4)
						.sellItem(Items.WRITABLE_BOOK)
						.experience(10).maxUses(16).multiplier(0.01f),
				},
				4, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.DIAMOND)
						.sellItem(Items.REPEATER).sellItemMin(4).sellItemMax(8)
						.experience(10).maxUses(16).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.DIAMOND)
						.sellItem(Items.PISTON).sellItemMin(4).sellItemMax(8)
						.experience(10).maxUses(16).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.DIAMOND)
						.sellItem(Items.REDSTONE_LAMP).sellItemMin(4).sellItemMax(8)
						.experience(10).maxUses(16).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.DIAMOND)
						.sellItem(Items.COMPARATOR)
						.experience(10).maxUses(16).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.DIAMOND)
						.sellItem(Items.DISPENSER)
						.experience(10).maxUses(16).multiplier(0.01f),
				},
				5, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.EMERALD).buyItem1Min(16).buyItem1Max(24)
						.sellItemGenerator((entity, random) -> getMaxBookWithOneOf(random, entity, List.of(
							Enchantments.UNBREAKING
						))).maxUses(2).multiplier(0.01f),
				}
			))
		);

		trades.put(VillagerProfession.MASON, 
			new Int2ObjectOpenHashMap<>(Map.of(
				1, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.GRANITE).buyItem1Min(12).buyItem1Max(20)
						.sellItem(Items.IRON_INGOT)
						.experience(2).maxUses(16).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.ANDESITE).buyItem1Min(12).buyItem1Max(20)
						.sellItem(Items.IRON_INGOT)
						.experience(2).maxUses(16).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.DIORITE).buyItem1Min(12).buyItem1Max(20)
						.sellItem(Items.IRON_INGOT)
						.experience(2).maxUses(16).multiplier(0.01f),
				},
				2, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.CLAY_BALL).buyItem1Min(8).buyItem1Max(16)
						.sellItem(Items.IRON_INGOT)
						.experience(5).maxUses(16).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.IRON_INGOT)
						.sellItem(Items.BRICK).sellItemMin(8).sellItemMax(32)
						.experience(5).maxUses(64).multiplier(0.01f),
				},
				3, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.IRON_INGOT)
						.sellItem(Items.POLISHED_ANDESITE).sellItemMin(4).sellItemMax(8)
						.experience(5).maxUses(16).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.IRON_INGOT)
						.sellItem(Items.POLISHED_DIORITE).sellItemMin(4).sellItemMax(8)
						.experience(5).maxUses(16).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.IRON_INGOT)
						.sellItem(Items.POLISHED_GRANITE).sellItemMin(4).sellItemMax(8)
						.experience(5).maxUses(16).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.IRON_INGOT)
						.sellItem(Items.POLISHED_DEEPSLATE).sellItemMin(4).sellItemMax(8)
						.experience(5).maxUses(16).multiplier(0.01f),
				},
				4, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.DIAMOND)
						.sellItemGenerator((entity, random) -> {
							switch (random.nextInt(16)) {
								case 0:
									return Items.WHITE_TERRACOTTA.getDefaultStack();
								case 1:
									return Items.ORANGE_TERRACOTTA.getDefaultStack();
								case 2:
									return Items.YELLOW_TERRACOTTA.getDefaultStack();
								case 3:
									return Items.RED_TERRACOTTA.getDefaultStack();
								case 4:
									return Items.PINK_TERRACOTTA.getDefaultStack();
								case 5:
									return Items.BLUE_TERRACOTTA.getDefaultStack();
								case 6:
									return Items.CYAN_TERRACOTTA.getDefaultStack();
								case 7:
									return Items.LIGHT_BLUE_TERRACOTTA.getDefaultStack();
								case 8:
									return Items.GREEN_TERRACOTTA.getDefaultStack();
								case 9:
									return Items.LIME_TERRACOTTA.getDefaultStack();
								case 10:
									return Items.LIGHT_GRAY_TERRACOTTA.getDefaultStack();
								case 11:
									return Items.GRAY_TERRACOTTA.getDefaultStack();
								case 12:
									return Items.BLACK_TERRACOTTA.getDefaultStack();
								case 13:
									return Items.PURPLE_TERRACOTTA.getDefaultStack();
								case 14:
									return Items.MAGENTA_TERRACOTTA.getDefaultStack();
								case 15:
									return Items.BROWN_TERRACOTTA.getDefaultStack();
								default:
									return ItemStack.EMPTY;
							}
						}).sellItemMin(4).sellItemMax(8)
						.experience(10).maxUses(16).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.DIAMOND)
						.sellItemGenerator((entity, random) -> {
							switch (random.nextInt(16)) {
								case 0:
									return Items.WHITE_GLAZED_TERRACOTTA.getDefaultStack();
								case 1:
									return Items.ORANGE_GLAZED_TERRACOTTA.getDefaultStack();
								case 2:
									return Items.YELLOW_GLAZED_TERRACOTTA.getDefaultStack();
								case 3:
									return Items.RED_GLAZED_TERRACOTTA.getDefaultStack();
								case 4:
									return Items.PINK_GLAZED_TERRACOTTA.getDefaultStack();
								case 5:
									return Items.BLUE_GLAZED_TERRACOTTA.getDefaultStack();
								case 6:
									return Items.CYAN_GLAZED_TERRACOTTA.getDefaultStack();
								case 7:
									return Items.LIGHT_BLUE_GLAZED_TERRACOTTA.getDefaultStack();
								case 8:
									return Items.GREEN_GLAZED_TERRACOTTA.getDefaultStack();
								case 9:
									return Items.LIME_GLAZED_TERRACOTTA.getDefaultStack();
								case 10:
									return Items.LIGHT_GRAY_GLAZED_TERRACOTTA.getDefaultStack();
								case 11:
									return Items.GRAY_GLAZED_TERRACOTTA.getDefaultStack();
								case 12:
									return Items.BLACK_GLAZED_TERRACOTTA.getDefaultStack();
								case 13:
									return Items.PURPLE_GLAZED_TERRACOTTA.getDefaultStack();
								case 14:
									return Items.MAGENTA_GLAZED_TERRACOTTA.getDefaultStack();
								case 15:
									return Items.BROWN_GLAZED_TERRACOTTA.getDefaultStack();
								default:
									return ItemStack.EMPTY;
							}
						}).sellItemMin(4).sellItemMax(8)
						.experience(10).maxUses(16).multiplier(0.01f),
				},
				5, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.EMERALD)
						.sellItem(Items.QUARTZ_BLOCK).sellItemMin(4).sellItemMax(8)
						.maxUses(16),
				}
			))
		);

		trades.put(VillagerProfession.SHEPHERD, 
			new Int2ObjectOpenHashMap<>(Map.of(
				1, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.WHITE_DYE).buyItem1Min(4).buyItem1Max(8)
						.sellItem(Items.IRON_INGOT)
						.experience(2).maxUses(16).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.YELLOW_DYE).buyItem1Min(4).buyItem1Max(8)
						.sellItem(Items.IRON_INGOT)
						.experience(2).maxUses(16).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.RED_DYE).buyItem1Min(4).buyItem1Max(8)
						.sellItem(Items.IRON_INGOT)
						.experience(2).maxUses(16).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.PINK_DYE).buyItem1Min(4).buyItem1Max(8)
						.sellItem(Items.IRON_INGOT)
						.experience(2).maxUses(16).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.LIGHT_GRAY_DYE).buyItem1Min(4).buyItem1Max(8)
						.sellItem(Items.IRON_INGOT)
						.experience(2).maxUses(16).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.GRAY_DYE).buyItem1Min(4).buyItem1Max(8)
						.sellItem(Items.IRON_INGOT)
						.experience(2).maxUses(16).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.PURPLE_DYE).buyItem1Min(4).buyItem1Max(8)
						.sellItem(Items.IRON_INGOT)
						.experience(2).maxUses(16).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.MAGENTA_DYE).buyItem1Min(4).buyItem1Max(8)
						.sellItem(Items.IRON_INGOT)
						.experience(2).maxUses(16).multiplier(0.01f),
				},
				2, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.BLACK_DYE).buyItem1Min(4).buyItem1Max(8)
						.sellItem(Items.IRON_INGOT)
						.experience(5).maxUses(16).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.GREEN_DYE).buyItem1Min(4).buyItem1Max(8)
						.sellItem(Items.IRON_INGOT)
						.experience(5).maxUses(16).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.BROWN_DYE).buyItem1Min(4).buyItem1Max(8)
						.sellItem(Items.IRON_INGOT)
						.experience(5).maxUses(16).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.CYAN_DYE).buyItem1Min(4).buyItem1Max(8)
						.sellItem(Items.IRON_INGOT)
						.experience(5).maxUses(16).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.LIME_DYE).buyItem1Min(4).buyItem1Max(8)
						.sellItem(Items.IRON_INGOT)
						.experience(5).maxUses(16).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.ORANGE_DYE).buyItem1Min(4).buyItem1Max(8)
						.sellItem(Items.IRON_INGOT)
						.experience(5).maxUses(16).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.BLUE_DYE).buyItem1Min(4).buyItem1Max(8)
						.sellItem(Items.IRON_INGOT)
						.experience(5).maxUses(16).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.LIGHT_BLUE_DYE).buyItem1Min(4).buyItem1Max(8)
						.sellItem(Items.IRON_INGOT)
						.experience(5).maxUses(16).multiplier(0.01f),
				},
				3, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.IRON_INGOT)
						.sellItem(Items.WHITE_WOOL).sellItemMin(4).sellItemMax(8)
						.experience(10).maxUses(16).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.IRON_INGOT)
						.sellItem(Items.YELLOW_WOOL).sellItemMin(4).sellItemMax(8)
						.experience(10).maxUses(16).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.IRON_INGOT)
						.sellItem(Items.RED_WOOL).sellItemMin(4).sellItemMax(8)
						.experience(10).maxUses(16).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.IRON_INGOT)
						.sellItem(Items.PINK_WOOL).sellItemMin(4).sellItemMax(8)
						.experience(10).maxUses(16).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.IRON_INGOT)
						.sellItem(Items.LIGHT_GRAY_WOOL).sellItemMin(4).sellItemMax(8)
						.experience(10).maxUses(16).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.IRON_INGOT)
						.sellItem(Items.GRAY_WOOL).sellItemMin(4).sellItemMax(8)
						.experience(10).maxUses(16).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.IRON_INGOT)
						.sellItem(Items.PURPLE_WOOL).sellItemMin(4).sellItemMax(8)
						.experience(10).maxUses(16).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.IRON_INGOT)
						.sellItem(Items.MAGENTA_WOOL).sellItemMin(4).sellItemMax(8)
						.experience(10).maxUses(16).multiplier(0.01f),
				},
				4, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.IRON_INGOT)
						.sellItem(Items.BLACK_WOOL).sellItemMin(4).sellItemMax(8)
						.experience(10).maxUses(16).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.IRON_INGOT)
						.sellItem(Items.GREEN_WOOL).sellItemMin(4).sellItemMax(8)
						.experience(10).maxUses(16).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.IRON_INGOT)
						.sellItem(Items.BROWN_WOOL).sellItemMin(4).sellItemMax(8)
						.experience(10).maxUses(16).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.IRON_INGOT)
						.sellItem(Items.CYAN_WOOL).sellItemMin(4).sellItemMax(8)
						.experience(10).maxUses(16).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.IRON_INGOT)
						.sellItem(Items.LIME_WOOL).sellItemMin(4).sellItemMax(8)
						.experience(10).maxUses(16).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.IRON_INGOT)
						.sellItem(Items.ORANGE_WOOL).sellItemMin(4).sellItemMax(8)
						.experience(10).maxUses(16).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.IRON_INGOT)
						.sellItem(Items.BLUE_WOOL).sellItemMin(4).sellItemMax(8)
						.experience(10).maxUses(16).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.IRON_INGOT)
						.sellItem(Items.LIGHT_BLUE_WOOL).sellItemMin(4).sellItemMax(8)
						.experience(10).maxUses(16).multiplier(0.01f),
				},
				5, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.EMERALD).buyItem1Min(8).buyItem1Max(16)
						.sellItem(Items.NAME_TAG)
				}
			))
		);

		trades.put(VillagerProfession.TOOLSMITH, 
			new Int2ObjectOpenHashMap<>(Map.of(
				1, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.IRON_INGOT).buyItem1Min(3).buyItem1Max(6)
						.sellItemGenerator((entity, random) -> enchantWithLevel(Items.IRON_SHOVEL.getDefaultStack(), 10, random, entity))
						.experience(20).maxUses(2).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.IRON_INGOT).buyItem1Min(4).buyItem1Max(8)
						.sellItemGenerator((entity, random) -> enchantWithLevel(Items.IRON_HOE.getDefaultStack(), 10, random, entity))
						.experience(20).maxUses(2).multiplier(0.01f),
				},
				2, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.IRON_INGOT).buyItem1Min(6).buyItem1Max(12)
						.sellItemGenerator((entity, random) -> enchantWithLevel(Items.IRON_AXE.getDefaultStack(), 10, random, entity))
						.experience(25).maxUses(2).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.IRON_INGOT).buyItem1Min(6).buyItem1Max(12)
						.sellItemGenerator((entity, random) -> enchantWithLevel(Items.IRON_PICKAXE.getDefaultStack(), 10, random, entity))
						.experience(25).maxUses(2).multiplier(0.01f),
				},
				3, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.DIAMOND).buyItem1Min(3).buyItem1Max(6)
						.sellItemGenerator((entity, random) -> enchantWithLevel(Items.DIAMOND_SHOVEL.getDefaultStack(), 10, random, entity))
						.experience(40).maxUses(2).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.DIAMOND).buyItem1Min(4).buyItem1Max(8)
						.sellItemGenerator((entity, random) -> enchantWithLevel(Items.DIAMOND_HOE.getDefaultStack(), 10, random, entity))
						.experience(40).maxUses(2).multiplier(0.01f),
				},
				4, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.DIAMOND).buyItem1Min(6).buyItem1Max(12)
						.sellItemGenerator((entity, random) -> enchantWithLevel(Items.DIAMOND_AXE.getDefaultStack(), 10, random, entity))
						.experience(45).maxUses(2).multiplier(0.01f),
					new TradeFactory()
						.buyItem1(Items.DIAMOND).buyItem1Min(6).buyItem1Max(12)
						.sellItemGenerator((entity, random) -> enchantWithLevel(Items.DIAMOND_PICKAXE.getDefaultStack(), 10, random, entity))
						.experience(45).maxUses(2).multiplier(0.01f),
				},
				5, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.EMERALD).buyItem1Min(8).buyItem1Max(16)
						.sellItem(Items.BELL),
					new TradeFactory()
						.buyItem1(Items.EMERALD).buyItem1Min(16).buyItem1Max(24)
						.sellItemGenerator((entity, random) -> getMaxBookWithOneOf(random, entity, List.of(
							Enchantments.EFFICIENCY,
							Enchantments.FORTUNE,
							Enchantments.SILK_TOUCH
						))).maxUses(2).multiplier(0.01f),
				}
			))
		);

		trades.put(VillagerProfession.WEAPONSMITH, 
			new Int2ObjectOpenHashMap<>(Map.of(
				1, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.IRON_INGOT).buyItem1Min(4).buyItem1Max(8)
						.sellItemGenerator((entity, random) -> enchantWithLevel(Items.IRON_SWORD.getDefaultStack(), 10, random, entity))
						.experience(20).maxUses(2).multiplier(0.01f),
				},
				2, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.DIAMOND).buyItem1Min(4).buyItem1Max(8)
						.sellItemGenerator((entity, random) -> enchantWithLevel(Items.DIAMOND_SWORD.getDefaultStack(), 10, random, entity))
						.experience(50).maxUses(2).multiplier(0.01f),
				},
				3, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.EMERALD).buyItem1Min(16).buyItem1Max(24)
						.sellItemGenerator((entity, random) -> getMaxBookWithOneOf(random, entity, List.of(
							Enchantments.BANE_OF_ARTHROPODS,
							Enchantments.SHARPNESS,
							Enchantments.SMITE,
							Enchantments.BREACH
						))).experience(40).maxUses(2).multiplier(0.01f),
				},
				4, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.EMERALD).buyItem1Min(16).buyItem1Max(24)
						.sellItemGenerator((entity, random) -> getMaxBookWithOneOf(random, entity, List.of(
							Enchantments.DENSITY,
							Enchantments.LOOTING,
							Enchantments.SWEEPING_EDGE,
							Enchantments.FIRE_ASPECT,
							Enchantments.KNOCKBACK
						))).experience(50).maxUses(2).multiplier(0.01f),
				},
				5, new TradeOffers.Factory[] {
					new TradeFactory()
						.buyItem1(Items.EMERALD).buyItem1Min(8).buyItem1Max(16)
						.sellItem(Items.BELL)
				}
			))
		);

		return trades;
	});
}
