package com.stanieldev.relativity.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.stanieldev.relativity.history.EntityHistory;
import com.stanieldev.relativity.history.EntitySnapshot;
import com.stanieldev.relativity.history.HistoryManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class PositionTrajectoryRenderer {
    public static void render_old() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }

        EntityHistory history = HistoryManager.getHistory(minecraft.player.getUUID());
        if (history == null) { return; }

        for (EntitySnapshot snapshot : history.getSnapshots()) {
            Vec3 pos = snapshot.getPosition();
            System.out.println("Would render at: " + pos);
        }
    }

    public static void render(PoseStack poseStack, MultiBufferSource bufferSource) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) { return; }

        EntityHistory history =  HistoryManager.getHistory(player.getUUID());
        if (history == null) { return; }

        Vec3 camera = minecraft.gameRenderer.getMainCamera().getPosition();
        for (EntitySnapshot snapshot : history.getSnapshots()) {
            Vec3 pos = snapshot.getPosition().subtract(camera);
            float size = 0.1f;
            LevelRenderer.renderLineBox(
                    poseStack,
                    bufferSource.getBuffer(net.minecraft.client.renderer.RenderType.lines()),
                    pos.x - size,
                    pos.y - size,
                    pos.z - size,
                    pos.x + size,
                    pos.y + size,
                    pos.z + size,
                    1.0f, // red
                    1.0f, // green
                    1.0f, // blue
                    1.0f  // alpha
            );
        }
    }
}
