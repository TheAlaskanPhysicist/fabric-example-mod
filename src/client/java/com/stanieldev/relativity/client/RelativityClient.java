package com.stanieldev.relativity.client;

import com.stanieldev.relativity.client.debug.DebugState;
import com.stanieldev.relativity.history.EntityHistoryManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.stanieldev.relativity.client.render.PositionTrajectoryRenderer;

public class RelativityClient implements ClientModInitializer {
	public static final String MOD_ID = "relativity";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static KeyMapping showEntityTrajectoryKey;

	private static boolean isF4Down() {
		long window = Minecraft.getInstance().getWindow().getWindow();
		return GLFW.glfwGetKey(window, GLFW.GLFW_KEY_F4) == GLFW.GLFW_PRESS;
	}


	@Override
	public void onInitializeClient() {

		// Debug Toggling Keybinds
		showEntityTrajectoryKey = new KeyMapping(
				"key.relativity.toggle_show_entity_trajectory",
				GLFW.GLFW_KEY_T,
				"category.relativity"
		);
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (showEntityTrajectoryKey.consumeClick()) {
				if (isF4Down()) {
					DebugState.toggleShowEntityTrajectory();
					System.out.println("Trajectory rendering: " + DebugState.showEntityTrajectory);
                    assert Minecraft.getInstance().player != null;
                    Minecraft.getInstance().player.displayClientMessage(
						Component.literal("Trajectory: " + (DebugState.showEntityTrajectory ? "ON" : "OFF")), true
					);
				}
			}
		});

		// Debug Rendering
		WorldRenderEvents.AFTER_ENTITIES.register(context -> {
			if (DebugState.showEntityTrajectory) {
				PositionTrajectoryRenderer.render(
						context.matrixStack(),
						context.consumers()
				);
			}
		});

		// History Tracking
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			Minecraft minecraft = Minecraft.getInstance();
			if (minecraft.level == null) {
				return;
			}
			long tick = minecraft.level.getGameTime();
			for (var entity : minecraft.level.entitiesForRendering()) {
				EntityHistoryManager.record(entity, tick);
			}
		});
		LOGGER.info("Relativity client initialization loaded!");
	}
}