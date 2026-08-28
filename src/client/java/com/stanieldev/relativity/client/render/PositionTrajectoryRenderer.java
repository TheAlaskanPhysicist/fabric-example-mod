package com.stanieldev.relativity.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.stanieldev.relativity.history.EntityHistory;
import com.stanieldev.relativity.history.EntityHistoryManager;
import com.stanieldev.relativity.history.EntitySnapshot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class PositionTrajectoryRenderer {

    public static void render(PoseStack poseStack, MultiBufferSource bufferSource) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;

        Vec3 camera = minecraft.gameRenderer.getMainCamera().getPosition();
        float partialTick = minecraft.getFrameTime();
        double currentTick = minecraft.level.getGameTime() + partialTick;

        for (Entity entity : minecraft.level.entitiesForRendering()) {
            EntityHistory history = EntityHistoryManager.getEntityHistory(entity.getUUID());
            if (history == null || history.isEmpty()) continue;

            // Render historical snapshots (White boxes)
            for (EntitySnapshot snapshot : history.getSnapshotHistory()) {
                Vec3 pos = snapshot.position().subtract(camera);
                float size = 0.05f;
                LevelRenderer.renderLineBox(
                        poseStack,
                        bufferSource.getBuffer(RenderType.lines()),
                        pos.x - size, pos.y - size, pos.z - size,
                        pos.x + size, pos.y + size, pos.z + size,
                        1.0f, 1.0f, 1.0f, 0.4f
                );
            }

            // Real position interpolated from latest history snapshots (Green Box)
            EntitySnapshot latest = history.getLastSnapshot();
            if (latest != null) {
                Vec3 realPos = latest.position().subtract(camera);
                LevelRenderer.renderLineBox(
                        poseStack,
                        bufferSource.getBuffer(RenderType.lines()),
                        realPos.x - 0.2, realPos.y - 0.2, realPos.z - 0.2,
                        realPos.x + 0.2, realPos.y + 0.2, realPos.z + 0.2,
                        0.0f, 1.0f, 0.0f, 1.0f
                );
            }

            // Retarded time render target position (Red Box)
            double targetTick = history.solveRetardedTime(currentTick, camera);
            var state = history.getInterpolatedState(targetTick);

            if (state != null) {
                Vec3 delayedPos = state.position().subtract(camera);
                LevelRenderer.renderLineBox(
                        poseStack,
                        bufferSource.getBuffer(RenderType.lines()),
                        delayedPos.x - 0.2, delayedPos.y - 0.2, delayedPos.z - 0.2,
                        delayedPos.x + 0.2, delayedPos.y + 0.2, delayedPos.z + 0.2,
                        1.0f, 0.0f, 0.0f, 1.0f
                );
            }
        }
    }
}