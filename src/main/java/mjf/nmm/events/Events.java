package mjf.nmm.events;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;

public class Events {
    public static void registerEvents() {
		ServerLifecycleEvents.SERVER_STARTED.register(Events::serverStarted);
        ServerWorldEvents.LOAD.register(Events::onWorldLoad);
    }

	private static void onWorldLoad(MinecraftServer server, ServerWorld world) {
	}

	private static void serverStarted(MinecraftServer server) {
		// Prevent sleep from skipping the night
		server.setDifficulty(Difficulty.HARD, true);
		server.setDifficultyLocked(true);
		server.getGameRules().get(GameRules.PLAYERS_SLEEPING_PERCENTAGE).set(101, server);
		server.getGameRules().get(GameRules.UNIVERSAL_ANGER).set(true, server);
		server.getGameRules().get(GameRules.FORGIVE_DEAD_PLAYERS).set(false, server);
		server.getGameRules().get(GameRules.DO_INSOMNIA).set(false, server);
	}
}
