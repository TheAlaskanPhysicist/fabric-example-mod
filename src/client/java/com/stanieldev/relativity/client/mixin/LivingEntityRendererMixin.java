package com.stanieldev.relativity.client.mixin;

import com.stanieldev.relativity.history.EntityHistory;
import com.stanieldev.relativity.history.EntityHistoryManager;
import com.stanieldev.relativity.history.EntitySnapshot;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity> {

    private static float relativity$limbSwing;
    private static float relativity$limbSwingAmount;
    private static boolean relativity$hasOverride = false;


    @Inject(
            method = "render",
            at = @At("HEAD")
    )
    private void relativity$beforeRender(
            T entity,
            float entityYaw,
            float partialTick,
            com.mojang.blaze3d.vertex.PoseStack poseStack,
            net.minecraft.client.renderer.MultiBufferSource buffer,
            int light,
            CallbackInfo ci
    ) {
        EntityHistory history =
                EntityHistoryManager.getEntityHistory(entity.getUUID());

        if (history == null) {
            return;
        }

        EntitySnapshot older = history.getTicksAgo(21);
        EntitySnapshot newer = history.getTicksAgo(20);

        if (older == null || newer == null) {
            return;
        }

        relativity$limbSwing = Mth.lerp(
                partialTick,
                older.limbSwing(),
                newer.limbSwing()
        );

        relativity$limbSwingAmount = Mth.lerp(
                partialTick,
                older.limbSwingAmount(),
                newer.limbSwingAmount()
        );

        relativity$hasOverride = true;
    }


    @Inject(
            method = "render",
            at = @At("RETURN")
    )
    private void relativity$afterRender(
            T entity,
            float entityYaw,
            float partialTick,
            com.mojang.blaze3d.vertex.PoseStack poseStack,
            net.minecraft.client.renderer.MultiBufferSource buffer,
            int light,
            CallbackInfo ci
    ) {
        relativity$hasOverride = false;
    }
}