package com.stanieldev.relativity.history;

import com.stanieldev.relativity.mixin.LivingEntityAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.*;

import static com.stanieldev.relativity.config.RelativityConfig.TEMP_MAX_TIME_RETARDATION;

public class EntityHistoryManager {

    // Entity history storage
    private static final Map<UUID, EntityHistory> histories = new HashMap<>();
    public static void record(Entity entity, long tick) {
        EntityHistory history = histories.computeIfAbsent(entity.getUUID(), id -> new EntityHistory());

        float bodyYaw = 0;
        float limbSwing = 0;
        float limbSwingAmount = 0;
        int age = entity.tickCount;

        if (entity instanceof LivingEntity living) {

            LivingEntityAccessor accessor =
                    (LivingEntityAccessor) living;

            bodyYaw = accessor.getBodyYaw();

            limbSwing = accessor
                    .getWalkAnimation()
                    .position();

            limbSwingAmount = accessor
                    .getWalkAnimation()
                    .speed();
        }

        history.add(new EntitySnapshot(
                tick,
                entity.position(),
                entity.getDeltaMovement(),  // Velocity in blocks/tick
                entity.getYRot(),
                entity.getXRot(),
                entity.getYHeadRot(),
                bodyYaw,
                limbSwing,
                limbSwingAmount,
                entity.tickCount
        ));
    }
    public static void prune(long currentTick) {
        histories.entrySet().removeIf(entry -> {
            EntityHistory history = entry.getValue();
            long age = currentTick - history.getLastRecordedTick();
            return age > TEMP_MAX_TIME_RETARDATION;
        });
    }
    public static int getEntityCount() { return histories.size(); }
    public static boolean isEmpty() { return histories.isEmpty(); }
    public static EntityHistory getEntityHistory(UUID uuid) {
        return histories.get(uuid);
    }
    public static Collection<EntityHistory> getEntityHistories() { return new ArrayList<>(histories.values()); }
}
