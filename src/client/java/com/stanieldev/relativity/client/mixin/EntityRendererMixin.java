package com.stanieldev.relativity.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.stanieldev.relativity.history.EntityHistory;
import com.stanieldev.relativity.history.EntityHistoryManager;
import com.stanieldev.relativity.history.EntitySnapshot;
import com.stanieldev.relativity.mixin.WalkAnimationStateAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public class EntityRendererMixin {

    private static boolean renderingCopy = false;

    @Inject(
            method = "render",
            at = @At("HEAD")
    )
    private <E extends Entity> void relativity$beforeRender(
            E entity,
            double x,
            double y,
            double z,
            float yaw,
            float tickDelta,
            PoseStack poseStack,
            MultiBufferSource multiBufferSource,
            int light,
            CallbackInfo ci
    ) {

        if (renderingCopy) {
            return;
        }

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

        Minecraft minecraft = Minecraft.getInstance();

        Entity copy = entity.getType().create(minecraft.level);

        if (copy == null) {
            return;
        }

        Vec3 position = older.position().lerp(
                newer.position(),
                tickDelta
        );

        float delayedYaw = Mth.rotLerp(
                tickDelta,
                older.yaw(),
                newer.yaw()
        );

        float delayedPitch = Mth.lerp(
                tickDelta,
                older.pitch(),
                newer.pitch()
        );

        float delayedHeadYaw = Mth.rotLerp(
                tickDelta,
                older.headYaw(),
                newer.headYaw()
        );

        float delayedBodyYaw = Mth.rotLerp(
                tickDelta,
                older.bodyYaw(),
                newer.bodyYaw()
        );


        copy.moveTo(
                position.x,
                position.y,
                position.z,
                delayedYaw,
                delayedPitch
        );

        copy.setYRot(delayedYaw);
        copy.setXRot(delayedPitch);

        // Make Minecraft interpolation use the snapshot state
        copy.yRotO = delayedYaw;
        copy.xRotO = delayedPitch;

        int delayedAge = (int) Mth.lerp(
                tickDelta,
                older.age(),
                newer.age()
        );




        if (copy instanceof LivingEntity livingCopy) {

            livingCopy.setYHeadRot(delayedHeadYaw);
            livingCopy.setYBodyRot(delayedBodyYaw);

            livingCopy.yHeadRotO = delayedHeadYaw;
            livingCopy.yBodyRotO = delayedBodyYaw;

            livingCopy.tickCount = delayedAge;
        }

        if (copy instanceof LivingEntity livingCopy) {

            if (entity instanceof LivingEntity originalLiving) {
                livingCopy.walkAnimation.update(
                        originalLiving.walkAnimation.speed(),
                        originalLiving.walkAnimation.position()
                );
            }
        }



        Vec3 camera = minecraft.gameRenderer
                .getMainCamera()
                .getPosition();


        EntityRenderDispatcher dispatcher =
                minecraft.getEntityRenderDispatcher();


        renderingCopy = true;

        try {
            dispatcher.render(
                    copy,
                    position.x - camera.x,
                    position.y - camera.y,
                    position.z - camera.z,
                    delayedYaw,
                    tickDelta,
                    poseStack,
                    multiBufferSource,
                    light
            );
        } finally {
            renderingCopy = false;
        }
    }
}