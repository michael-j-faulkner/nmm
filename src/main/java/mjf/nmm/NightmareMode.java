package mjf.nmm;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.minecraft.world.GameRules;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.gen.feature.DefaultBiomeFeatures;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mjf.nmm.entities.ai.sensors.CustomActivity;
import mjf.nmm.entities.ai.sensors.CustomMemoryModuleType;
import mjf.nmm.entities.ai.sensors.CustomSensorType;
import mjf.nmm.events.Events;
import mjf.nmm.world.AlterSpawns;

public class NightmareMode implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final String MODID = "nmm";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	@Override
	public void onInitialize() {
		Events.registerEvents();
		CustomActivity.register();
		AlterSpawns.alterSpawns();
	}
}