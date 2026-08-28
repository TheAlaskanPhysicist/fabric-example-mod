package com.stanieldev.relativity.client.render;

import com.stanieldev.relativity.history.EntityHistory;
import com.stanieldev.relativity.history.EntityHistoryManager;
import com.stanieldev.relativity.history.RenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class RelativityRenderHelper {

    public static class SavedEntityState {
        public Vec3 pos;
        public double xo, yo, zo; // Added to prevent entity sub-tick spazzing
        public Vec3 delayedPos;
        public float yRot, xRot;
        public float yHeadRot, yBodyRot;
        public float limbPos, limbSpeed;
    }

    public static SavedEntityState applyDelayedState(Entity entity, float partialTick) {
        EntityHistory history = EntityHistoryManager.getEntityHistory(entity.getUUID());
        if (history == null || history.isEmpty()) return null;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.gameRenderer == null) return null;

        Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();
        double currentTick = mc.level.getGameTime() + partialTick;

        // Solve light-cone intersection (t_ret)
        double targetTick = history.solveRetardedTime(currentTick, cameraPos);
        RenderState state = history.getInterpolatedState(targetTick);
        if (state == null) return null;

        // Save original real-time entity state
        SavedEntityState saved = new SavedEntityState();
        saved.pos = entity.position();
        saved.yRot = entity.getYRot();
        saved.xRot = entity.getXRot();

        if (entity instanceof LivingEntity living) {
            saved.yHeadRot = living.getYHeadRot();
            saved.yBodyRot = living.yBodyRot;
            saved.limbPos = living.walkAnimation.position();
            saved.limbSpeed = living.walkAnimation.speed();
        }

        // Apply historical snapshot values to live entity instance
        entity.setYRot(state.yRot());
        entity.setXRot(state.xRot());
        entity.yRotO = state.yRot();
        entity.xRotO = state.xRot();

        if (entity instanceof LivingEntity living) {
            living.yHeadRot = state.yHeadRot();
            living.yHeadRotO = state.yHeadRot();
            living.yBodyRot = state.yBodyRot();
            living.yBodyRotO = state.yBodyRot();
            living.walkAnimation.setSpeed(state.limbSpeed());
        }

        saved.pos = entity.position();
        saved.xo = entity.xo;
        saved.yo = entity.yo;
        saved.zo = entity.zo;
        saved.delayedPos = state.position();

        return saved;
    }

    public static void restoreState(Entity entity, SavedEntityState saved) {
        if (saved == null) return;

        entity.setYRot(saved.yRot);
        entity.setXRot(saved.xRot);

        if (entity instanceof LivingEntity living) {
            living.yHeadRot = saved.yHeadRot;
            living.yBodyRot = saved.yBodyRot;
            living.walkAnimation.setSpeed(saved.limbSpeed);
        }
    }
}