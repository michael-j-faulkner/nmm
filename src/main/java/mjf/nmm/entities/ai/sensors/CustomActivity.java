package mjf.nmm.entities.ai.sensors;

import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

// CURRENTLY UNUSED -- LEFT AS AN EXAMPLE 
public class CustomActivity {
    public static Activity MINE;

    private static Activity register(String id) {
        return Registry.register(Registries.ACTIVITY, id, new Activity(id));
    }

    public static void register() {
        MINE = register("mine");
    }
}
