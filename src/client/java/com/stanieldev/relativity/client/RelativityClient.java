package com.stanieldev.relativity.client;

import com.stanieldev.relativity.history.HistoryManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.stanieldev.relativity.client.render.PositionTrajectoryRenderer;

public class RelativityClient implements ClientModInitializer {
	public static final String MOD_ID = "relativity";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		WorldRenderEvents.AFTER_ENTITIES.register(context -> {
			PositionTrajectoryRenderer.render(
					context.matrixStack(),
					context.consumers()
			);
		});
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			Minecraft minecraft = Minecraft.getInstance();
			if (minecraft.level == null) {
				return;
			}
			long tick = minecraft.level.getGameTime();
			for (var entity : minecraft.level.entitiesForRendering()) {
				HistoryManager.record(entity, tick);
			}
		});
		LOGGER.info("Relativity client initialization loaded!");
	}
}