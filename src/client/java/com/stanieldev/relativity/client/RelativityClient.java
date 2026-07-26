package com.stanieldev.relativity.client;

import com.stanieldev.relativity.client.debug.DebugState;
import com.stanieldev.relativity.client.render.PositionTrajectoryRenderer;
import com.stanieldev.relativity.history.EntityHistory;
import com.stanieldev.relativity.history.EntityHistoryManager;

import com.stanieldev.relativity.history.EntitySnapshot;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static com.stanieldev.relativity.config.RelativityConfig.PRUNE_FREQUENCY;

public class RelativityClient implements ClientModInitializer {
	public static final String MOD_ID = "relativity";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeClient() {

		// Debugging
		initializeDebugKeybinds();
		initializeDebugRenderer();

		// History Tracking
		initializeHistoryTracker();

		LOGGER.info("Relativity client initialization loaded!");
	}

	private static boolean isF4Down() {
		long window = Minecraft.getInstance().getWindow().getWindow();
		return GLFW.glfwGetKey(window, GLFW.GLFW_KEY_F4) == GLFW.GLFW_PRESS;
	}

	private void initializeDebugKeybinds() {

		// Debug Keybinds
		KeyMapping showEntityTrajectoryKey = new KeyMapping(
				"key.relativity.show_entity_trajectory_toggle",
				GLFW.GLFW_KEY_T,
				"category.relativity"
		);

		// Client Listener
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (!isF4Down()) { return; }
			while (showEntityTrajectoryKey.consumeClick()) {
				DebugState.toggleShowEntityTrajectory();
				System.out.println("Trajectory rendering: " + DebugState.showEntityTrajectory);
				assert Minecraft.getInstance().player != null;
				Minecraft.getInstance().player.displayClientMessage(
						Component.literal("Trajectory: " + (DebugState.showEntityTrajectory ? "ON" : "OFF")), true
				);
			}
		});
	}

	private void initializeDebugRenderer() {
		WorldRenderEvents.AFTER_ENTITIES.register(context -> {
			if (DebugState.showEntityTrajectory) {
				PositionTrajectoryRenderer.render(context.matrixStack(), context.consumers());
			}
		});
	}

	private void initializeHistoryTracker() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.level == null) { return; }
			long tick = client.level.getGameTime();

			for (var entity : client.level.entitiesForRendering()) {
				EntityHistoryManager.record(entity, tick);
			}
			if (tick % PRUNE_FREQUENCY == 0) {
				int current_count = EntityHistoryManager.getEntityCount();
				EntityHistoryManager.prune(tick);
				LOGGER.info("Relativity Entity Pruner: " + current_count + " -> " + EntityHistoryManager.getEntityCount());
			}
		});
	}
}