package mjf.nmm;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mjf.nmm.entities.ai.sensors.CustomSensorType;
import mjf.nmm.events.Events;

public class NightmareMode implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final String MODID = "nmm";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	@Override
	public void onInitialize() {
		Events.registerEvents();
		CustomSensorType.register();
	}
}