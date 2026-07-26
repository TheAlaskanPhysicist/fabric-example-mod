package com.stanieldev.relativity;

import com.stanieldev.relativity.history.EntityHistoryManager;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.resources.ResourceLocation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.stanieldev.relativity.config.RelativityConfig.PRUNE_FREQUENCY;

public class Relativity implements ModInitializer {
	public static final String MOD_ID = "relativity";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {

		// Entity history storage thread
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			// long tick = server.getTickCount();
			long tick = server.overworld().getGameTime();
			for (var level : server.getAllLevels()) {
				for (var entity : level.getAllEntities()) {
					EntityHistoryManager.record(entity, tick);
				}
			}
			if (tick % PRUNE_FREQUENCY == 0) {
				int current_count = EntityHistoryManager.getEntityCount();
				EntityHistoryManager.prune(tick);
				LOGGER.info("Relativity Entity Pruner: " + current_count + " -> " + EntityHistoryManager.getEntityCount());
			}
		});
		LOGGER.info("Relativity initialization loaded!");
	}

	public static ResourceLocation id(String path) {
		return new ResourceLocation(MOD_ID, path);
	}
}
