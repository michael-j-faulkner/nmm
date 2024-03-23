package mjf.nmm.entities.ai.sensors;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;

import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class CustomMemoryModuleType <U> {
    public static MemoryModuleType<List<PlayerEntity>> NEAREST_TARGETABLE_PLAYERS;
    public static MemoryModuleType<PlayerEntity> NEAREST_TARGETABLE_PLAYER;
    public static MemoryModuleType<BlockPos> MINE_BLOCK_LOCATION;

    private static <U> MemoryModuleType<U> register(String id, Codec<U> codec) {
        return Registry.register(Registries.MEMORY_MODULE_TYPE, new Identifier(id), new MemoryModuleType<U>(Optional.of(codec)));
    }

    private static <U> MemoryModuleType<U> register(String id) {
        return Registry.register(Registries.MEMORY_MODULE_TYPE, new Identifier(id), new MemoryModuleType<U>(Optional.empty()));
    }

    public static void register() {
        NEAREST_TARGETABLE_PLAYERS  = register("nearest_targetable_players");
        NEAREST_TARGETABLE_PLAYER   = register("nearest_targetable_player");
        MINE_BLOCK_LOCATION         = register("mine_block_location", BlockPos.CODEC);
    }
}
