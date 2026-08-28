package com.stanieldev.relativity.client.debug;

import com.stanieldev.relativity.history.EntityHistory;
import com.stanieldev.relativity.history.EntityHistoryManager;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class RelativityDebugHUD {

    public static void register() {
        HudRenderCallback.EVENT.register((guiGraphics, tickDelta) -> {
            if (!DebugState.showEntityTrajectory) return;

            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null || mc.player == null) return;

            Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();
            double currentTick = mc.level.getGameTime() + tickDelta;

            Entity targetEntity = null;
            double minDistance = Double.MAX_VALUE;

            for (Entity entity : mc.level.entitiesForRendering()) {
                if (entity == mc.player) continue;
                double dist = entity.position().distanceTo(cameraPos);
                if (dist < minDistance) {
                    minDistance = dist;
                    targetEntity = entity;
                }
            }

            if (targetEntity == null) return;

            EntityHistory history = EntityHistoryManager.getEntityHistory(targetEntity.getUUID());
            if (history == null || history.isEmpty()) return;

            double targetTick = history.solveRetardedTime(currentTick, cameraPos);
            double delay = currentTick - targetTick;

            int x = 10;
            int y = 10;
            int color = 0x00FF00;

            guiGraphics.drawString(mc.font, "--- Relativity Debug HUD ---", x, y, color, true);
            guiGraphics.drawString(mc.font, String.format("Target Entity: %s", targetEntity.getName().getString()), x, y + 10, color, true);
            guiGraphics.drawString(mc.font, String.format("Distance: %.2f blocks", minDistance), x, y + 20, color, true);
            guiGraphics.drawString(mc.font, String.format("Current Tick: %.2f", currentTick), x, y + 30, color, true);
            guiGraphics.drawString(mc.font, String.format("Retarded Tick: %.2f", targetTick), x, y + 40, color, true);
            guiGraphics.drawString(mc.font, String.format("Render Delay: %.2f ticks (%.2fs)", delay, delay / 20.0), x, y + 50, color, true);
            guiGraphics.drawString(mc.font, String.format("Snapshot Queue Size: %d", history.size()), x, y + 60, color, true);
        });
    }
}