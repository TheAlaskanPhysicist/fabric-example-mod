package com.stanieldev.relativity.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.stanieldev.relativity.history.EntityHistory;
import com.stanieldev.relativity.history.EntityHistoryManager;
import com.stanieldev.relativity.history.EntitySnapshot;
import com.stanieldev.relativity.mixin.LivingEntityAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public class DelayedEntityRenderMixin {
    private static boolean renderingDelayed = false;

    @Inject(method = "render", at = @At("HEAD"))
    private <E extends Entity> void relativity$renderDelayed(
            E entity,
            double x,
            double y,
            double z,
            float yaw,
            float tickDelta,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            CallbackInfo ci
    ) {
        if (renderingDelayed) { return; }

        EntityHistory history = EntityHistoryManager.getEntityHistory(entity.getUUID());
        if (history == null) { return; }

        EntitySnapshot older = history.getTicksAgo(21);
        EntitySnapshot newer = history.getTicksAgo(20);

        if (older == null || newer == null) { return; }

        Entity copy = entity.getType().create(Minecraft.getInstance().level);
        if (copy == null) { return; }

        copy.load(newer.nbt());
        Vec3 position = older.position().lerp(newer.position(), tickDelta);

        float delayedYaw = newer.nbt().getFloat("RelativityYaw");
        float delayedPitch = newer.nbt().getFloat("RelativityPitch");

        copy.setPos(
                position.x,
                position.y,
                position.z
        );

        copy.xOld = older.position().x;
        copy.yOld = older.position().y;
        copy.zOld = older.position().z;

        copy.setYRot(delayedYaw);
        copy.setXRot(delayedPitch);

        if (copy instanceof LivingEntity living) {
            living.setYHeadRot(newer.nbt().getFloat("RelativityHeadYaw"));
            living.yHeadRotO = living.getYHeadRot();
            if (living instanceof Mob mob) {
                mob.yBodyRot = newer.nbt().getFloat("RelativityBodyYaw");
                mob.yBodyRotO = mob.yBodyRot;
            }
        }


        copy.yRotO = delayedYaw;
        copy.xRotO = delayedPitch;

        Vec3 camera = Minecraft.getInstance()
                .gameRenderer
                .getMainCamera()
                .getPosition();

        renderingDelayed = true;

        try {
            Minecraft.getInstance()
                    .getEntityRenderDispatcher()
                    .render(
                            copy,
                            position.x - camera.x,
                            position.y - camera.y,
                            position.z - camera.z,
                            delayedYaw,
                            0.0F,
                            poseStack,
                            buffer,
                            light
                    );
        } finally {
            renderingDelayed = false;
        }
    }
}