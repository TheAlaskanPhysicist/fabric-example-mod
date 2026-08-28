package com.stanieldev.relativity.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.stanieldev.relativity.client.render.RelativityRenderHelper;
import com.stanieldev.relativity.client.render.RelativityRenderHelper.SavedEntityState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayDeque;
import java.util.Deque;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

    @Unique
    private static final ThreadLocal<Deque<SavedEntityState>> relativity$stateStack = ThreadLocal.withInitial(ArrayDeque::new);

    @Inject(
            method = "render",
            at = @At("HEAD")
    )
    private <E extends Entity> void onBeforeRender(
            E entity, double x, double y, double z,
            float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource bufferSource, int packedLight, CallbackInfo ci
    ) {
        SavedEntityState saved = RelativityRenderHelper.applyDelayedState(entity, partialTick);
        relativity$stateStack.get().push(saved != null ? saved : new SavedEntityState());

        if (saved == null || saved.delayedPos == null) { return; }

        // Calculate where vanilla renderer WOULD draw the entity (sub-tick interpolated real position)
        double realX = Mth.lerp(partialTick, entity.xo, entity.getX());
        double realY = Mth.lerp(partialTick, entity.yo, entity.getY());
        double realZ = Mth.lerp(partialTick, entity.zo, entity.getZ());

        // Shift PoseStack by the delta between real sub-tick position and delayed historical position
        double offsetX = saved.delayedPos.x - realX;
        double offsetY = saved.delayedPos.y - realY;
        double offsetZ = saved.delayedPos.z - realZ;

        poseStack.pushPose();
        poseStack.translate(offsetX, offsetY, offsetZ);
    }

    @Inject(
            method = "render",
            at = @At("RETURN")
    )
    private <E extends Entity> void onAfterRender(
            E entity, double x, double y, double z,
            float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource bufferSource, int packedLight, CallbackInfo ci
    ) {
        Deque<SavedEntityState> stack = relativity$stateStack.get();
        if (!stack.isEmpty()) {
            SavedEntityState saved = stack.pop();
            if (saved.delayedPos != null) { poseStack.popPose(); }
            RelativityRenderHelper.restoreState(entity, saved);
        }
    }
}