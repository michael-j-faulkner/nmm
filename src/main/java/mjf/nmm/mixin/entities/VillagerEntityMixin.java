package mjf.nmm.mixin.entities;

import java.lang.foreign.Linker.Option;
import java.util.Map;
import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapDecorationTypes;
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
import net.minecraft.village.VillagerType;
import net.minecraft.world.World;

@Mixin(VillagerEntity.class)
public abstract class VillagerEntityMixin extends MerchantEntity {
    public VillagerEntityMixin(EntityType<? extends MerchantEntity> entityType, World world) {
        super(entityType, world);
    }

    public static class TradeFactory implements Factory {
        private final Item buyItem1;
        private final Optional<Item> buyItem2;
        private final ItemStack sellItem;
        private final int maxUses;
        private final int experience;
        private final float multiplier;
		private final int buyItem1Min;
		private final int buyItem1Max;
		private final int buyItem2Min;
		private final int buyItem2Max;
		private final int sellItemMin;
		private final int sellItemMax;

		public TradeFactory(Item buyItem1,
							Item sellItem,
							int maxUses, int experience, float multiplier) {
			this(buyItem1, 1, 
				sellItem, 1, 
				maxUses, experience, multiplier);
		}
		
		public TradeFactory(Item buyItem1,
							Item sellItem, int sellItemMin, int sellItemMax, 
							int maxUses, int experience, float multiplier) {
			this(buyItem1, 1, 1, 
				sellItem, sellItemMin, sellItemMax, 
				maxUses, experience, multiplier);
		}
		
		public TradeFactory(Item buyItem1, int buyItem1Min, int buyItem1Max,
							Item sellItem, 
							int maxUses, int experience, float multiplier) {
			this(buyItem1, buyItem1Min, buyItem1Max, 
				sellItem, 1, 1, 
				maxUses, experience, multiplier);
		}

		
		public TradeFactory(Item buyItem1, int buyItem1Count, 
							Item sellItem, int sellItemCount, 
							int maxUses, int experience, float multiplier) {
			this(buyItem1, buyItem1Count,
				Optional.empty(), 0,
				sellItem, sellItemCount,
				maxUses, experience, multiplier);
		}

		public TradeFactory(Item buyItem1, int buyItem1Min, int buyItem1Max,
							Item sellItem, int sellItemMin, int sellItemMax, 
							int maxUses, int experience, float multiplier) {
			this(buyItem1, buyItem1Min, buyItem1Max, 
				Optional.empty(), 0, 0, 
				sellItem, sellItemMin, sellItemMax, 
				maxUses, experience, multiplier);
		}
		
		public TradeFactory(Item buyItem1, int buyItem1Count, Optional<Item> buyItem2, int buyItem2Count, 
							Item sellItem, int sellItemCount, int maxUses, int experience, float multiplier) {
			this(buyItem1, buyItem1Count, buyItem1Count, 
				buyItem2, buyItem2Count, buyItem2Count, 
				sellItem, sellItemCount, sellItemCount, 
				maxUses, experience, multiplier);
		}
		
		public TradeFactory(Item buyItem1, int buyItem1Min, int buyItem1Max, Optional<Item> buyItem2, int buyItem2Min, int buyItem2Max, 
							Item sellItem, int sellItemMin, int sellItemMax, int maxUses, int experience, float multiplier) {
			this(buyItem1, buyItem1Min, buyItem1Max, 
				buyItem2, buyItem2Min, buyItem2Max, 
				new ItemStack(sellItem), sellItemMin, sellItemMax, 
				maxUses, experience, multiplier);
		}

		public TradeFactory(Item buyItem1, int buyItem1Min, int buyItem1Max, Optional<Item> buyItem2, int buyItem2Min, int buyItem2Max, 
							ItemStack sellItem, int sellItemMin, int sellItemMax, int maxUses, int experience, float multiplier) {
			this.buyItem1 = buyItem1;
			this.buyItem2 = buyItem2;
			this.sellItem = sellItem;
			this.buyItem1Min = buyItem1Min;
			this.buyItem1Max = buyItem1Max;
			this.buyItem2Min = buyItem2Min;
			this.buyItem2Max = buyItem2Max;
			this.sellItemMin = sellItemMin;
			this.sellItemMax = sellItemMax;
			this.maxUses = maxUses;
			this.experience = experience;
			this.multiplier = multiplier;
		}

        @Override
        public TradeOffer create(Entity entity, Random random) {
			ItemStack sellItem = this.sellItem.copy();
			sellItem.setCount(random.nextBetween(this.sellItemMin, this.sellItemMax));
            return new TradeOffer(
				new TradedItem(this.buyItem1, random.nextBetween(this.buyItem1Min, this.buyItem1Max)), 
				this.buyItem2.isPresent() ? Optional.of(new TradedItem(this.buyItem2.get(), random.nextBetween(this.buyItem2Min, this.buyItem2Max))) : Optional.empty(), 
				sellItem, 
				this.maxUses, 
				this.experience, 
				this.multiplier);
        }
    }

	public class SuspiciousStewFactory extends TradeFactory {
		public SuspiciousStewFactory(Item buyItem1, int buyItem1Min, int buyItem1Max,
									 Optional<Item> buyItem2, int buyItem2Min, int buyItem2Max,
									 int maxUses, int experience, float multiplier) {
			ItemStack stew = new ItemStack(Items.SUSPICIOUS_STEW);
			super(buyItem1, buyItem1Min, buyItem1Max, buyItem2, buyItem2Min, buyItem2Max, stew, 1, 1, maxUses, experience, multiplier);
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
							new TradeFactory(Items.COPPER_INGOT, 1, 4, Items.BREAD, 1, 8, 64, 1, 0.05f),
							new TradeFactory(Items.COPPER_INGOT, 1, 4, Items.POTATO, 1, 8, 64, 1, 0.05f),
							new TradeFactory(Items.COPPER_INGOT, 1, 4, Items.CARROT, 1, 8, 64, 1, 0.05f),
							new TradeFactory(Items.COPPER_INGOT, 1, 4, Items.BEETROOT, 1, 8, 64, 1, 0.05f),
							new TradeFactory(Items.COPPER_INGOT, 1, 4, Items.APPLE, 1, 8, 64, 1, 0.05f)
						},
						2,
						new TradeOffers.Factory[]{
							new TradeFactory(Items.IRON_INGOT, 1, 4, Items.BEETROOT_SOUP, 4, 5, 0.05f),
							new TradeFactory(Items.IRON_INGOT, 1, 4, Items.MUSHROOM_STEW, 4, 5, 0.05f),
							new TradeFactory(Items.IRON_INGOT, 1, 4, 
											 Optional.empty(), 0, 0,
											 new ItemStack(Items.SUSPICIOUS_STEW), 1, 4, 
											 4, 5, 0.05f),
						},
						3,
						new TradeOffers.Factory[]{new TradeOffers.SellItemFactory(Items.COOKIE, 3, 18, 10), new TradeOffers.BuyItemFactory(Blocks.MELON, 4, 12, 20)},
						4,
						new TradeOffers.Factory[]{
							new TradeOffers.SellItemFactory(Blocks.CAKE, 1, 1, 12, 15),
							new TradeOffers.SellSuspiciousStewFactory(StatusEffects.NIGHT_VISION, 100, 15),
							new TradeOffers.SellSuspiciousStewFactory(StatusEffects.JUMP_BOOST, 160, 15),
							new TradeOffers.SellSuspiciousStewFactory(StatusEffects.WEAKNESS, 140, 15),
							new TradeOffers.SellSuspiciousStewFactory(StatusEffects.BLINDNESS, 120, 15),
							new TradeOffers.SellSuspiciousStewFactory(StatusEffects.POISON, 280, 15),
							new TradeOffers.SellSuspiciousStewFactory(StatusEffects.SATURATION, 7, 15)
						},
						5,
						new TradeOffers.Factory[]{
							new TradeOffers.SellItemFactory(Items.GOLDEN_CARROT, 3, 3, 30), new TradeOffers.SellItemFactory(Items.GLISTERING_MELON_SLICE, 4, 3, 30)
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
							new TradeOffers.BuyItemFactory(Items.STRING, 20, 16, 2),
							new TradeOffers.BuyItemFactory(Items.COAL, 10, 16, 2),
							new TradeOffers.ProcessItemFactory(Items.COD, 6, 1, Items.COOKED_COD, 6, 16, 1, 0.05F),
							new TradeOffers.SellItemFactory(Items.COD_BUCKET, 3, 1, 16, 1)
						},
						2,
						new TradeOffers.Factory[]{
							new TradeOffers.BuyItemFactory(Items.COD, 15, 16, 10),
							new TradeOffers.ProcessItemFactory(Items.SALMON, 6, 1, Items.COOKED_SALMON, 6, 16, 5, 0.05F),
							new TradeOffers.SellItemFactory(Items.CAMPFIRE, 2, 1, 5)
						},
						3,
						new TradeOffers.Factory[]{
							new TradeOffers.BuyItemFactory(Items.SALMON, 13, 16, 20), new TradeOffers.SellEnchantedToolFactory(Items.FISHING_ROD, 3, 3, 10, 0.2F)
						},
						4,
						new TradeOffers.Factory[]{new TradeOffers.BuyItemFactory(Items.TROPICAL_FISH, 6, 12, 30)},
						5,
						new TradeOffers.Factory[]{
							new TradeOffers.BuyItemFactory(Items.PUFFERFISH, 4, 12, 30),
							new TradeOffers.TypeAwareBuyForOneEmeraldFactory(
								1,
								12,
								30,
								ImmutableMap.<VillagerType, Item>builder()
									.put(VillagerType.PLAINS, Items.OAK_BOAT)
									.put(VillagerType.TAIGA, Items.SPRUCE_BOAT)
									.put(VillagerType.SNOW, Items.SPRUCE_BOAT)
									.put(VillagerType.DESERT, Items.JUNGLE_BOAT)
									.put(VillagerType.JUNGLE, Items.JUNGLE_BOAT)
									.put(VillagerType.SAVANNA, Items.ACACIA_BOAT)
									.put(VillagerType.SWAMP, Items.DARK_OAK_BOAT)
									.build()
							)
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
							new TradeOffers.BuyItemFactory(Blocks.WHITE_WOOL, 18, 16, 2),
							new TradeOffers.BuyItemFactory(Blocks.BROWN_WOOL, 18, 16, 2),
							new TradeOffers.BuyItemFactory(Blocks.BLACK_WOOL, 18, 16, 2),
							new TradeOffers.BuyItemFactory(Blocks.GRAY_WOOL, 18, 16, 2),
							new TradeOffers.SellItemFactory(Items.SHEARS, 2, 1, 1)
						},
						2,
						new TradeOffers.Factory[]{
							new TradeOffers.BuyItemFactory(Items.WHITE_DYE, 12, 16, 10),
							new TradeOffers.BuyItemFactory(Items.GRAY_DYE, 12, 16, 10),
							new TradeOffers.BuyItemFactory(Items.BLACK_DYE, 12, 16, 10),
							new TradeOffers.BuyItemFactory(Items.LIGHT_BLUE_DYE, 12, 16, 10),
							new TradeOffers.BuyItemFactory(Items.LIME_DYE, 12, 16, 10),
							new TradeOffers.SellItemFactory(Blocks.WHITE_WOOL, 1, 1, 16, 5),
							new TradeOffers.SellItemFactory(Blocks.ORANGE_WOOL, 1, 1, 16, 5),
							new TradeOffers.SellItemFactory(Blocks.MAGENTA_WOOL, 1, 1, 16, 5),
							new TradeOffers.SellItemFactory(Blocks.LIGHT_BLUE_WOOL, 1, 1, 16, 5),
							new TradeOffers.SellItemFactory(Blocks.YELLOW_WOOL, 1, 1, 16, 5),
							new TradeOffers.SellItemFactory(Blocks.LIME_WOOL, 1, 1, 16, 5),
							new TradeOffers.SellItemFactory(Blocks.PINK_WOOL, 1, 1, 16, 5),
							new TradeOffers.SellItemFactory(Blocks.GRAY_WOOL, 1, 1, 16, 5),
							new TradeOffers.SellItemFactory(Blocks.LIGHT_GRAY_WOOL, 1, 1, 16, 5),
							new TradeOffers.SellItemFactory(Blocks.CYAN_WOOL, 1, 1, 16, 5),
							new TradeOffers.SellItemFactory(Blocks.PURPLE_WOOL, 1, 1, 16, 5),
							new TradeOffers.SellItemFactory(Blocks.BLUE_WOOL, 1, 1, 16, 5),
							new TradeOffers.SellItemFactory(Blocks.BROWN_WOOL, 1, 1, 16, 5),
							new TradeOffers.SellItemFactory(Blocks.GREEN_WOOL, 1, 1, 16, 5),
							new TradeOffers.SellItemFactory(Blocks.RED_WOOL, 1, 1, 16, 5),
							new TradeOffers.SellItemFactory(Blocks.BLACK_WOOL, 1, 1, 16, 5),
							new TradeOffers.SellItemFactory(Blocks.WHITE_CARPET, 1, 4, 16, 5),
							new TradeOffers.SellItemFactory(Blocks.ORANGE_CARPET, 1, 4, 16, 5),
							new TradeOffers.SellItemFactory(Blocks.MAGENTA_CARPET, 1, 4, 16, 5),
							new TradeOffers.SellItemFactory(Blocks.LIGHT_BLUE_CARPET, 1, 4, 16, 5),
							new TradeOffers.SellItemFactory(Blocks.YELLOW_CARPET, 1, 4, 16, 5),
							new TradeOffers.SellItemFactory(Blocks.LIME_CARPET, 1, 4, 16, 5),
							new TradeOffers.SellItemFactory(Blocks.PINK_CARPET, 1, 4, 16, 5),
							new TradeOffers.SellItemFactory(Blocks.GRAY_CARPET, 1, 4, 16, 5),
							new TradeOffers.SellItemFactory(Blocks.LIGHT_GRAY_CARPET, 1, 4, 16, 5),
							new TradeOffers.SellItemFactory(Blocks.CYAN_CARPET, 1, 4, 16, 5),
							new TradeOffers.SellItemFactory(Blocks.PURPLE_CARPET, 1, 4, 16, 5),
							new TradeOffers.SellItemFactory(Blocks.BLUE_CARPET, 1, 4, 16, 5),
							new TradeOffers.SellItemFactory(Blocks.BROWN_CARPET, 1, 4, 16, 5),
							new TradeOffers.SellItemFactory(Blocks.GREEN_CARPET, 1, 4, 16, 5),
							new TradeOffers.SellItemFactory(Blocks.RED_CARPET, 1, 4, 16, 5),
							new TradeOffers.SellItemFactory(Blocks.BLACK_CARPET, 1, 4, 16, 5)
						},
						3,
						new TradeOffers.Factory[]{
							new TradeOffers.BuyItemFactory(Items.YELLOW_DYE, 12, 16, 20),
							new TradeOffers.BuyItemFactory(Items.LIGHT_GRAY_DYE, 12, 16, 20),
							new TradeOffers.BuyItemFactory(Items.ORANGE_DYE, 12, 16, 20),
							new TradeOffers.BuyItemFactory(Items.RED_DYE, 12, 16, 20),
							new TradeOffers.BuyItemFactory(Items.PINK_DYE, 12, 16, 20),
							new TradeOffers.SellItemFactory(Blocks.WHITE_BED, 3, 1, 12, 10),
							new TradeOffers.SellItemFactory(Blocks.YELLOW_BED, 3, 1, 12, 10),
							new TradeOffers.SellItemFactory(Blocks.RED_BED, 3, 1, 12, 10),
							new TradeOffers.SellItemFactory(Blocks.BLACK_BED, 3, 1, 12, 10),
							new TradeOffers.SellItemFactory(Blocks.BLUE_BED, 3, 1, 12, 10),
							new TradeOffers.SellItemFactory(Blocks.BROWN_BED, 3, 1, 12, 10),
							new TradeOffers.SellItemFactory(Blocks.CYAN_BED, 3, 1, 12, 10),
							new TradeOffers.SellItemFactory(Blocks.GRAY_BED, 3, 1, 12, 10),
							new TradeOffers.SellItemFactory(Blocks.GREEN_BED, 3, 1, 12, 10),
							new TradeOffers.SellItemFactory(Blocks.LIGHT_BLUE_BED, 3, 1, 12, 10),
							new TradeOffers.SellItemFactory(Blocks.LIGHT_GRAY_BED, 3, 1, 12, 10),
							new TradeOffers.SellItemFactory(Blocks.LIME_BED, 3, 1, 12, 10),
							new TradeOffers.SellItemFactory(Blocks.MAGENTA_BED, 3, 1, 12, 10),
							new TradeOffers.SellItemFactory(Blocks.ORANGE_BED, 3, 1, 12, 10),
							new TradeOffers.SellItemFactory(Blocks.PINK_BED, 3, 1, 12, 10),
							new TradeOffers.SellItemFactory(Blocks.PURPLE_BED, 3, 1, 12, 10)
						},
						4,
						new TradeOffers.Factory[]{
							new TradeOffers.BuyItemFactory(Items.BROWN_DYE, 12, 16, 30),
							new TradeOffers.BuyItemFactory(Items.PURPLE_DYE, 12, 16, 30),
							new TradeOffers.BuyItemFactory(Items.BLUE_DYE, 12, 16, 30),
							new TradeOffers.BuyItemFactory(Items.GREEN_DYE, 12, 16, 30),
							new TradeOffers.BuyItemFactory(Items.MAGENTA_DYE, 12, 16, 30),
							new TradeOffers.BuyItemFactory(Items.CYAN_DYE, 12, 16, 30),
							new TradeOffers.SellItemFactory(Items.WHITE_BANNER, 3, 1, 12, 15),
							new TradeOffers.SellItemFactory(Items.BLUE_BANNER, 3, 1, 12, 15),
							new TradeOffers.SellItemFactory(Items.LIGHT_BLUE_BANNER, 3, 1, 12, 15),
							new TradeOffers.SellItemFactory(Items.RED_BANNER, 3, 1, 12, 15),
							new TradeOffers.SellItemFactory(Items.PINK_BANNER, 3, 1, 12, 15),
							new TradeOffers.SellItemFactory(Items.GREEN_BANNER, 3, 1, 12, 15),
							new TradeOffers.SellItemFactory(Items.LIME_BANNER, 3, 1, 12, 15),
							new TradeOffers.SellItemFactory(Items.GRAY_BANNER, 3, 1, 12, 15),
							new TradeOffers.SellItemFactory(Items.BLACK_BANNER, 3, 1, 12, 15),
							new TradeOffers.SellItemFactory(Items.PURPLE_BANNER, 3, 1, 12, 15),
							new TradeOffers.SellItemFactory(Items.MAGENTA_BANNER, 3, 1, 12, 15),
							new TradeOffers.SellItemFactory(Items.CYAN_BANNER, 3, 1, 12, 15),
							new TradeOffers.SellItemFactory(Items.BROWN_BANNER, 3, 1, 12, 15),
							new TradeOffers.SellItemFactory(Items.YELLOW_BANNER, 3, 1, 12, 15),
							new TradeOffers.SellItemFactory(Items.ORANGE_BANNER, 3, 1, 12, 15),
							new TradeOffers.SellItemFactory(Items.LIGHT_GRAY_BANNER, 3, 1, 12, 15)
						},
						5,
						new TradeOffers.Factory[]{new TradeOffers.SellItemFactory(Items.PAINTING, 2, 3, 30)}
					)
				)
			);
			map.put(
				VillagerProfession.FLETCHER,
				copyToFastUtilMap(
					ImmutableMap.of(
						1,
						new TradeOffers.Factory[]{
							new TradeOffers.BuyItemFactory(Items.STICK, 32, 16, 2),
							new TradeOffers.SellItemFactory(Items.ARROW, 1, 16, 1),
							new TradeOffers.ProcessItemFactory(Blocks.GRAVEL, 10, 1, Items.FLINT, 10, 12, 1, 0.05F)
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
