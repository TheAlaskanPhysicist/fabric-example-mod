package com.stanieldev.relativity;

import com.stanieldev.relativity.history.EntityHistory;
import com.stanieldev.relativity.history.HistoryManager;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.resources.ResourceLocation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Relativity implements ModInitializer {
	public static final String MOD_ID = "relativity";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			long tick = server.getTickCount();
			for (var level : server.getAllLevels()) {
				for (var entity : level.getAllEntities()) {
					HistoryManager.record(entity, tick);
				}
			}

			if (tick % 100 == 0) {
				System.out.println("Tracked entities: " + HistoryManager.getEntityCount());
				for (var player : server.getPlayerList().getPlayers()) {
					EntityHistory history = HistoryManager.getHistory(player.getUUID());
					if (history != null) {
						System.out.println("Snapshots: " + history.getSnapshots().size());
						var snapshots = history.getSnapshots();
						if (!snapshots.isEmpty()) {
							System.out.println("Oldest: " + snapshots.get(0).getPosition());
							System.out.println("Newest: " + snapshots.get(snapshots.size() - 1).getPosition());
						}
					}
				}
			}

		});
		LOGGER.info("Relativity initialization loaded!");
	}

	public static ResourceLocation id(String path) {
		return new ResourceLocation(MOD_ID, path);
	}
}
