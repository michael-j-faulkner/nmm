package mjf.nmm.entities;

import java.util.OptionalDouble;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.ServerWorldAccess;

public class ScalingDifficulty {
    private static final double HOURS_UNTIL_MAX = 50.0;

    public static int getDifficulty(ServerWorldAccess world, Vec3d pos) {
        BlockPos blockPos = BlockPos.ofFloored(pos.getX(), pos.getY(), pos.getZ());
        // When world is ChunkRegion, the world is paritially loaded and we can't get the chunk
        long inhabitedTime = world instanceof ChunkRegion ? 0 : world.getChunk(blockPos).getInhabitedTime();
        double inhabitedDifficulty = 3.0 * MathHelper.clamp(inhabitedTime / (20.0 * 60.0 * 60.0 * HOURS_UNTIL_MAX), 0.0, 1.0);
        @SuppressWarnings("resource")
        OptionalDouble avgPlayerDifficulty = world.toServerWorld().getPlayers().stream().filter(player -> player.squaredDistanceTo(pos) < 128 * 128 && !player.isSpectator()).mapToDouble(player -> getPlayerDifficulty(world.getServer(), player)).average();
        double moonDifficulty = 2.0 * world.getMoonSize();

        int difficulty = (int)Math.round(MathHelper.clamp(inhabitedDifficulty + (avgPlayerDifficulty.isPresent() ? avgPlayerDifficulty.getAsDouble() : 0.0) + moonDifficulty, 0.0, 10.0));
        return difficulty;
    }

    public static double getPercentDifficulty(ServerWorldAccess world, Vec3d pos) {
        return getDifficulty(world, pos) / 10.0;
    }

    public static <E extends ServerPlayerEntity> double getPlayerDifficulty(MinecraftServer server, E player) {
        double difficulty = 0.0;
        // Iron Age
        if (player.getAdvancementTracker().getProgress(server.getAdvancementLoader().get(new Identifier("story/smelt_iron"))).isDone()
            || player.getAdvancementTracker().getProgress(server.getAdvancementLoader().get(new Identifier("story/iron_tools"))).isDone()
            || player.getAdvancementTracker().getProgress(server.getAdvancementLoader().get(new Identifier("story/deflect_arrow"))).isDone()
            || player.getAdvancementTracker().getProgress(server.getAdvancementLoader().get(new Identifier("story/obtain_armor"))).isDone())
            difficulty += 1;
        
        // Diamond Age
        if (player.getAdvancementTracker().getProgress(server.getAdvancementLoader().get(new Identifier("story/mine_diamond"))).isDone()
            || player.getAdvancementTracker().getProgress(server.getAdvancementLoader().get(new Identifier("story/shiny_gear"))).isDone())
            difficulty += 1;
        
        // Enchanting Age
        if (player.getAdvancementTracker().getProgress(server.getAdvancementLoader().get(new Identifier("story/enchant_item"))).isDone())
            difficulty += 1;
        
        // Village Age
        if (player.getAdvancementTracker().getProgress(server.getAdvancementLoader().get(new Identifier("adventure/trade"))).isDone())
            difficulty += 1;
        
        // Nether Age
        if (player.getAdvancementTracker().getProgress(server.getAdvancementLoader().get(new Identifier("nether/root"))).isDone())
            difficulty += 1;
        
        // Brewing Age
        if (player.getAdvancementTracker().getProgress(server.getAdvancementLoader().get(new Identifier("nether/obtain_blaze_rod"))).isDone())
            difficulty += 1;
        
        // Netherite Age
        if (player.getAdvancementTracker().getProgress(server.getAdvancementLoader().get(new Identifier("nether/obtain_ancient_debris"))).isDone()
            || player.getAdvancementTracker().getProgress(server.getAdvancementLoader().get(new Identifier("nether/netherite_armor"))).isDone())
            difficulty += 1;
        
        // End Age
        if (player.getAdvancementTracker().getProgress(server.getAdvancementLoader().get(new Identifier("end/root"))).isDone())
            difficulty += 1;
        
        // Elytra Age
        if (player.getAdvancementTracker().getProgress(server.getAdvancementLoader().get(new Identifier("end/elytra"))).isDone())
            difficulty += 1;
        
        // Wither Age
        if (player.getAdvancementTracker().getProgress(server.getAdvancementLoader().get(new Identifier("nether/summon_wither"))).isDone())
            difficulty += 1;

        return difficulty;
    }
}
