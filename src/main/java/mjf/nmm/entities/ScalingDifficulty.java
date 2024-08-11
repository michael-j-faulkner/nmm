package mjf.nmm.entities;

import java.util.OptionalDouble;

import mjf.nmm.NightmareMode;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.ServerWorldAccess;

public class ScalingDifficulty {
    public static final Identifier PERMANENT_DAMAGE_IDENTIFIER = Identifier.of(NightmareMode.MODID, "permanent_damage");

    public static int getDifficulty(ServerWorldAccess world, Vec3d pos) {
        OptionalDouble avgPlayerDifficulty = world.toServerWorld().getPlayers().stream().filter(player -> player.squaredDistanceTo(pos) < 128 * 128 && !player.isSpectator()).mapToDouble(player -> getPlayerDifficulty(world.getServer(), player)).average();
        double moonDifficulty = 2.0 * world.getMoonSize();

        int difficulty = (int)Math.round(MathHelper.clamp((avgPlayerDifficulty.isPresent() ? avgPlayerDifficulty.getAsDouble() : 0.0) + moonDifficulty, 0.0, 10.0));
        return difficulty;
    }

    public static double getPercentDifficulty(ServerWorldAccess world, Vec3d pos) {
        return getDifficulty(world, pos) / 10.0;
    }

    public static <E extends ServerPlayerEntity> double getPlayerDifficulty(MinecraftServer server, E player) {
        double difficulty = 0.0;
        // Iron Age
        if (player.getAdvancementTracker().getProgress(server.getAdvancementLoader().get(Identifier.of("story/smelt_iron"))).isDone()
            || player.getAdvancementTracker().getProgress(server.getAdvancementLoader().get(Identifier.of("story/iron_tools"))).isDone()
            || player.getAdvancementTracker().getProgress(server.getAdvancementLoader().get(Identifier.of("story/deflect_arrow"))).isDone()
            || player.getAdvancementTracker().getProgress(server.getAdvancementLoader().get(Identifier.of("story/obtain_armor"))).isDone())
            difficulty += 1;
        
        // Diamond Age
        if (player.getAdvancementTracker().getProgress(server.getAdvancementLoader().get(Identifier.of("story/mine_diamond"))).isDone()
            || player.getAdvancementTracker().getProgress(server.getAdvancementLoader().get(Identifier.of("story/shiny_gear"))).isDone())
            difficulty += 1;
        
        // Enchanting Age
        if (player.getAdvancementTracker().getProgress(server.getAdvancementLoader().get(Identifier.of("story/enchant_item"))).isDone())
            difficulty += 1;
        
        // Village Age
        if (player.getAdvancementTracker().getProgress(server.getAdvancementLoader().get(Identifier.of("adventure/trade"))).isDone())
            difficulty += 1;
        
        // Nether Age
        if (player.getAdvancementTracker().getProgress(server.getAdvancementLoader().get(Identifier.of("nether/root"))).isDone())
            difficulty += 1;
        
        // Brewing Age
        if (player.getAdvancementTracker().getProgress(server.getAdvancementLoader().get(Identifier.of("nether/obtain_blaze_rod"))).isDone())
            difficulty += 1;
        
        // Netherite Age
        if (player.getAdvancementTracker().getProgress(server.getAdvancementLoader().get(Identifier.of("nether/obtain_ancient_debris"))).isDone()
            || player.getAdvancementTracker().getProgress(server.getAdvancementLoader().get(Identifier.of("nether/netherite_armor"))).isDone())
            difficulty += 1;
        
        // End Age
        if (player.getAdvancementTracker().getProgress(server.getAdvancementLoader().get(Identifier.of("end/root"))).isDone())
            difficulty += 1;
        
        // Elytra Age
        if (player.getAdvancementTracker().getProgress(server.getAdvancementLoader().get(Identifier.of("end/elytra"))).isDone())
            difficulty += 1;
        
        // Wither Age
        if (player.getAdvancementTracker().getProgress(server.getAdvancementLoader().get(Identifier.of("nether/summon_wither"))).isDone())
            difficulty += 1;

        return difficulty;
    }
}
