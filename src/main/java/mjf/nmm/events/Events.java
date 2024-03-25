package mjf.nmm.events;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;

public class Events {
    public static void registerEvents() {
		ServerLifecycleEvents.SERVER_STARTING.register(Events::onServerStarting);
        ServerWorldEvents.LOAD.register(Events::onWorldLoad);
    }

	private static void onServerStarting(MinecraftServer server) {
		// Ensure the game is on hard
		server.setDifficulty(Difficulty.HARD, true);
		server.setDifficultyLocked(true);
	}

	private static void onWorldLoad(MinecraftServer server, ServerWorld world) {
		// Prevent sleep from skipping the night
		world.getGameRules().get(GameRules.PLAYERS_SLEEPING_PERCENTAGE).set(101, server);
	}
}
