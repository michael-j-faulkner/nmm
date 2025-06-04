package mjf.nmm.mixin.entities;

import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import com.google.common.collect.ImmutableList;

import net.minecraft.block.Blocks;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.TradeOffers;
import net.minecraft.world.World;

@Mixin(WanderingTraderEntity.class)
public abstract class WanderingTraderEntityMixin extends MerchantEntity {
    public WanderingTraderEntityMixin(EntityType<? extends MerchantEntity> entityType, World world) {
        super(entityType, world);
    }

    @Overwrite
	public void fillRecipes() {
		TradeOfferList tradeOfferList = this.getOffers();

		for (Pair<TradeOffers.Factory[], Integer> pair : CUSTOM_WANDERING_TRADER_TRADES) {
			TradeOffers.Factory[] factorys = pair.getLeft();
			this.fillRecipesFromPool(tradeOfferList, factorys, pair.getRight());
		}
	}

	private static final List<Pair<TradeOffers.Factory[], Integer>> CUSTOM_WANDERING_TRADER_TRADES = ImmutableList.<Pair<TradeOffers.Factory[], Integer>>builder()
		.add(
			Pair.of(
				new TradeOffers.Factory[]{
					new TradeOffers.SellItemFactory(Items.PACKED_ICE, 1, 1, 6, 1),
					new TradeOffers.SellItemFactory(Items.BLUE_ICE, 6, 1, 6, 1),
					new TradeOffers.SellItemFactory(Items.GUNPOWDER, 1, 4, 2, 1),
					new TradeOffers.SellItemFactory(Items.PODZOL, 3, 3, 6, 1),
					new TradeOffers.SellItemFactory(Blocks.ACACIA_LOG, 1, 8, 4, 1),
					new TradeOffers.SellItemFactory(Blocks.BIRCH_LOG, 1, 8, 4, 1),
					new TradeOffers.SellItemFactory(Blocks.DARK_OAK_LOG, 1, 8, 4, 1),
					new TradeOffers.SellItemFactory(Blocks.JUNGLE_LOG, 1, 8, 4, 1),
					new TradeOffers.SellItemFactory(Blocks.OAK_LOG, 1, 8, 4, 1),
					new TradeOffers.SellItemFactory(Blocks.SPRUCE_LOG, 1, 8, 4, 1),
					new TradeOffers.SellItemFactory(Blocks.CHERRY_LOG, 1, 8, 4, 1),
					new TradeOffers.SellItemFactory(Blocks.MANGROVE_LOG, 1, 8, 4, 1),
					new TradeOffers.SellItemFactory(Blocks.PALE_OAK_LOG, 1, 8, 4, 1),
					new TradeOffers.SellEnchantedToolFactory(Items.IRON_PICKAXE, 1, 1, 1, 0.2F),
					new TradeOffers.SellItemFactory(PotionContentsComponent.createStack(Items.POTION, Potions.LONG_INVISIBILITY), 5, 1, 1, 1)
				},
				2
			)
		)
		.add(
			Pair.of(
				new TradeOffers.Factory[]{
					new TradeOffers.SellItemFactory(Items.TROPICAL_FISH_BUCKET, 3, 1, 4, 1),
					new TradeOffers.SellItemFactory(Items.PUFFERFISH_BUCKET, 3, 1, 4, 1),
					new TradeOffers.SellItemFactory(Items.SEA_PICKLE, 2, 1, 5, 1),
					new TradeOffers.SellItemFactory(Items.SLIME_BALL, 4, 1, 5, 1),
					new TradeOffers.SellItemFactory(Items.GLOWSTONE, 2, 1, 5, 1),
					new TradeOffers.SellItemFactory(Items.NAUTILUS_SHELL, 5, 1, 5, 1),
					new TradeOffers.SellItemFactory(Items.FERN, 1, 1, 12, 1),
					new TradeOffers.SellItemFactory(Items.SUGAR_CANE, 1, 1, 8, 1),
					new TradeOffers.SellItemFactory(Items.PUMPKIN, 1, 1, 4, 1),
					new TradeOffers.SellItemFactory(Items.KELP, 3, 1, 12, 1),
					new TradeOffers.SellItemFactory(Items.CACTUS, 3, 1, 8, 1),
					new TradeOffers.SellItemFactory(Items.DANDELION, 1, 1, 12, 1),
					new TradeOffers.SellItemFactory(Items.POPPY, 1, 1, 12, 1),
					new TradeOffers.SellItemFactory(Items.BLUE_ORCHID, 1, 1, 8, 1),
					new TradeOffers.SellItemFactory(Items.ALLIUM, 1, 1, 12, 1),
					new TradeOffers.SellItemFactory(Items.AZURE_BLUET, 1, 1, 12, 1),
					new TradeOffers.SellItemFactory(Items.RED_TULIP, 1, 1, 12, 1),
					new TradeOffers.SellItemFactory(Items.ORANGE_TULIP, 1, 1, 12, 1),
					new TradeOffers.SellItemFactory(Items.WHITE_TULIP, 1, 1, 12, 1),
					new TradeOffers.SellItemFactory(Items.PINK_TULIP, 1, 1, 12, 1),
					new TradeOffers.SellItemFactory(Items.OXEYE_DAISY, 1, 1, 12, 1),
					new TradeOffers.SellItemFactory(Items.CORNFLOWER, 1, 1, 12, 1),
					new TradeOffers.SellItemFactory(Items.LILY_OF_THE_VALLEY, 1, 1, 7, 1),
					new TradeOffers.SellItemFactory(Items.OPEN_EYEBLOSSOM, 1, 1, 7, 1),
					new TradeOffers.SellItemFactory(Items.WHEAT_SEEDS, 1, 1, 12, 1),
					new TradeOffers.SellItemFactory(Items.BEETROOT_SEEDS, 1, 1, 12, 1),
					new TradeOffers.SellItemFactory(Items.PUMPKIN_SEEDS, 1, 1, 12, 1),
					new TradeOffers.SellItemFactory(Items.MELON_SEEDS, 1, 1, 12, 1),
					new TradeOffers.SellItemFactory(Items.ACACIA_SAPLING, 5, 1, 8, 1),
					new TradeOffers.SellItemFactory(Items.BIRCH_SAPLING, 5, 1, 8, 1),
					new TradeOffers.SellItemFactory(Items.DARK_OAK_SAPLING, 5, 1, 8, 1),
					new TradeOffers.SellItemFactory(Items.JUNGLE_SAPLING, 5, 1, 8, 1),
					new TradeOffers.SellItemFactory(Items.OAK_SAPLING, 5, 1, 8, 1),
					new TradeOffers.SellItemFactory(Items.SPRUCE_SAPLING, 5, 1, 8, 1),
					new TradeOffers.SellItemFactory(Items.CHERRY_SAPLING, 5, 1, 8, 1),
					new TradeOffers.SellItemFactory(Items.PALE_OAK_SAPLING, 5, 1, 8, 1),
					new TradeOffers.SellItemFactory(Items.MANGROVE_PROPAGULE, 5, 1, 8, 1),
					new TradeOffers.SellItemFactory(Items.RED_DYE, 1, 3, 12, 1),
					new TradeOffers.SellItemFactory(Items.WHITE_DYE, 1, 3, 12, 1),
					new TradeOffers.SellItemFactory(Items.BLUE_DYE, 1, 3, 12, 1),
					new TradeOffers.SellItemFactory(Items.PINK_DYE, 1, 3, 12, 1),
					new TradeOffers.SellItemFactory(Items.BLACK_DYE, 1, 3, 12, 1),
					new TradeOffers.SellItemFactory(Items.GREEN_DYE, 1, 3, 12, 1),
					new TradeOffers.SellItemFactory(Items.LIGHT_GRAY_DYE, 1, 3, 12, 1),
					new TradeOffers.SellItemFactory(Items.MAGENTA_DYE, 1, 3, 12, 1),
					new TradeOffers.SellItemFactory(Items.YELLOW_DYE, 1, 3, 12, 1),
					new TradeOffers.SellItemFactory(Items.GRAY_DYE, 1, 3, 12, 1),
					new TradeOffers.SellItemFactory(Items.PURPLE_DYE, 1, 3, 12, 1),
					new TradeOffers.SellItemFactory(Items.LIGHT_BLUE_DYE, 1, 3, 12, 1),
					new TradeOffers.SellItemFactory(Items.LIME_DYE, 1, 3, 12, 1),
					new TradeOffers.SellItemFactory(Items.ORANGE_DYE, 1, 3, 12, 1),
					new TradeOffers.SellItemFactory(Items.BROWN_DYE, 1, 3, 12, 1),
					new TradeOffers.SellItemFactory(Items.CYAN_DYE, 1, 3, 12, 1),
					new TradeOffers.SellItemFactory(Items.BRAIN_CORAL_BLOCK, 3, 1, 8, 1),
					new TradeOffers.SellItemFactory(Items.BUBBLE_CORAL_BLOCK, 3, 1, 8, 1),
					new TradeOffers.SellItemFactory(Items.FIRE_CORAL_BLOCK, 3, 1, 8, 1),
					new TradeOffers.SellItemFactory(Items.HORN_CORAL_BLOCK, 3, 1, 8, 1),
					new TradeOffers.SellItemFactory(Items.TUBE_CORAL_BLOCK, 3, 1, 8, 1),
					new TradeOffers.SellItemFactory(Items.VINE, 1, 3, 4, 1),
					new TradeOffers.SellItemFactory(Items.PALE_HANGING_MOSS, 1, 3, 4, 1),
					new TradeOffers.SellItemFactory(Items.BROWN_MUSHROOM, 1, 3, 4, 1),
					new TradeOffers.SellItemFactory(Items.RED_MUSHROOM, 1, 3, 4, 1),
					new TradeOffers.SellItemFactory(Items.LILY_PAD, 1, 5, 2, 1),
					new TradeOffers.SellItemFactory(Items.SMALL_DRIPLEAF, 1, 2, 5, 1),
					new TradeOffers.SellItemFactory(Items.SAND, 1, 8, 8, 1),
					new TradeOffers.SellItemFactory(Items.RED_SAND, 1, 4, 6, 1),
					new TradeOffers.SellItemFactory(Items.POINTED_DRIPSTONE, 1, 2, 5, 1),
					new TradeOffers.SellItemFactory(Items.ROOTED_DIRT, 1, 2, 5, 1),
					new TradeOffers.SellItemFactory(Items.MOSS_BLOCK, 1, 2, 5, 1),
					new TradeOffers.SellItemFactory(Items.PALE_MOSS_BLOCK, 1, 2, 5, 1),
					new TradeOffers.SellItemFactory(Items.WILDFLOWERS, 1, 1, 12, 1),
					new TradeOffers.SellItemFactory(Items.TALL_DRY_GRASS, 1, 1, 12, 1),
					new TradeOffers.SellItemFactory(Items.FIREFLY_BUSH, 3, 1, 12, 1)
				},
				5
			)
		)
		.build();
}
