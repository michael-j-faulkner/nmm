package mjf.nmm.commands;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import mjf.nmm.NightmareMode;
import mjf.nmm.entities.ScalingDifficulty;

import static net.minecraft.server.command.CommandManager.*;

import java.util.Collection;

public class Commands {
    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            dispatcher.register(literal("scalingdifficulty")
            .requires(source -> source.hasPermissionLevel(2))
            .then(argument("pos", BlockPosArgumentType.blockPos())
                .executes(context -> {
                    NightmareMode.LOGGER.info("About to execute percent");
                    try {
                        context.getSource().sendFeedback(() -> 
                        Text.literal("The percent difficulty is %f"
                            .formatted(ScalingDifficulty.getPercentDifficulty(
                                context.getSource().getWorld(), 
                                BlockPosArgumentType.getBlockPos(context, "pos").toCenterPos()))), 
                            false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return 0;
                }))
            .then(argument("targets", EntityArgumentType.players())
                .executes(context -> {
                    NightmareMode.LOGGER.info("About to execute players");
                    Collection<ServerPlayerEntity> targets = EntityArgumentType.getPlayers(context, "targets");
                    for (ServerPlayerEntity player : targets) {
                        NightmareMode.LOGGER.info("In for loop");
                        try {
                            context.getSource().sendFeedback(() ->
                            Text.literal("%s's difficulty is %f/10.0"
                                .formatted(player.getDisplayName().getLiteralString(), ScalingDifficulty.getPlayerDifficulty(context.getSource().getServer(), player))), false);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    return targets.size();
                })
            )));
    }
}
