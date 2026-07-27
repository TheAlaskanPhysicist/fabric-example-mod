package com.stanieldev.relativity.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.stanieldev.relativity.history.EntityHistory;
import com.stanieldev.relativity.history.EntityHistoryManager;
import com.stanieldev.relativity.history.EntitySnapshot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.stanieldev.relativity.config.RelativityConfig.SPEED_OF_LIGHT;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mixin(LevelRenderer.class)
public class DelayedEntityRenderMixin {

    private static boolean renderingDelayed = false;
    private static final Map<UUID, Entity> fakeEntities = new HashMap<>();
    private static final Map<UUID, Boolean> initialized = new HashMap<>();

    private static Entity getFake(Entity entity) {
        return fakeEntities.computeIfAbsent(
                entity.getUUID(),
                id -> entity.getType().create(Minecraft.getInstance().level)
        );
    }

    @Inject(
            method = "renderEntity",
            at = @At("TAIL")
    )
    private void relativity$renderDelayed(
            Entity entity,
            double cameraX,
            double cameraY,
            double cameraZ,
            float tickDelta,
            PoseStack poseStack,
            MultiBufferSource buffer,
            CallbackInfo ci
    ) {
        if (renderingDelayed) return;

        EntityHistory history =
                EntityHistoryManager.getEntityHistory(entity.getUUID());

        if (history == null) return;

        double distance = entity.distanceTo(Minecraft.getInstance().player);
        int ticksAgo = (int)(distance / SPEED_OF_LIGHT);


        EntitySnapshot older = history.getTicksAgo(ticksAgo + 1);
        EntitySnapshot newer = history.getTicksAgo(ticksAgo);

        if (older == null || newer == null) return;

        Entity copy = getFake(entity);

        if (copy == null) return;

        if (!initialized.containsKey(entity.getUUID())) {
            copy.load(newer.nbt());
            initialized.put(entity.getUUID(), true);
        }

        if (copy == null) return;

        copy.load(newer.nbt());


        Vec3 pos = older.position().lerp(
                newer.position(),
                tickDelta
        );

        float yaw = Mth.rotLerp(
                tickDelta,
                older.nbt().getFloat("RelativityYaw"),
                newer.nbt().getFloat("RelativityYaw")
        );

        float pitch = Mth.lerp(
                tickDelta,
                older.nbt().getFloat("RelativityPitch"),
                newer.nbt().getFloat("RelativityPitch")
        );

        copy.setPos(
                pos.x,
                pos.y,
                pos.z
        );

        copy.setYRot(yaw);
        copy.setXRot(pitch);

        copy.yRotO = yaw;
        copy.xRotO = pitch;

        copy.xOld = pos.x;
        copy.yOld = pos.y;
        copy.zOld = pos.z;


        if (copy instanceof LivingEntity living) {
            float headYaw = Mth.rotLerp(
                    tickDelta,
                    older.nbt().getFloat("RelativityHeadYaw"),
                    newer.nbt().getFloat("RelativityHeadYaw")
            );

            living.setYHeadRot(headYaw);
            living.yHeadRotO = headYaw;

            if (living instanceof Mob mob) {
                float bodyYaw = Mth.rotLerp(
                        tickDelta,
                        older.nbt().getFloat("RelativityBodyYaw"),
                        newer.nbt().getFloat("RelativityBodyYaw")
                );

                mob.yBodyRot = bodyYaw;
                mob.yBodyRotO = bodyYaw;
            }
        }

        Vec3 camera =
                Minecraft.getInstance()
                        .gameRenderer
                        .getMainCamera()
                        .getPosition();

        renderingDelayed = true;

        try {
            Minecraft.getInstance()
                    .getEntityRenderDispatcher()
                    .render(
                            copy,
                            pos.x - camera.x,
                            pos.y - camera.y,
                            pos.z - camera.z,
                            yaw,
                            tickDelta,
                            poseStack,
                            buffer,
                            15728880
                    );
        } finally {
            renderingDelayed = false;
        }





    }
}