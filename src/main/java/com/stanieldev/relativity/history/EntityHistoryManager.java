package com.stanieldev.relativity.history;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

import java.util.*;

import static com.stanieldev.relativity.config.RelativityConfig.TEMP_MAX_TIME_RETARDATION;

public class EntityHistoryManager {

    private static final Map<UUID, EntityHistory> histories = new HashMap<>();

    public static void record(Entity entity, long tick) {
        EntityHistory history = histories.computeIfAbsent(entity.getUUID(), id -> new EntityHistory());

        float yRot = entity.getYRot();
        float xRot = entity.getXRot();
        float yHeadRot = yRot;
        float yBodyRot = yRot;
        float limbPos = 0.0f;
        float limbSpeed = 0.0f;
        int deathTime = 0;

        if (entity instanceof LivingEntity living) {
            yHeadRot = living.getYHeadRot();
            yBodyRot = living.yBodyRot;
            limbPos = living.walkAnimation.position();
            limbSpeed = living.walkAnimation.speed();
            deathTime = living.deathTime;
        }

        CompoundTag tag = new CompoundTag();
        entity.saveWithoutId(tag);

        history.add(new EntitySnapshot(
                tick,
                entity.position(),
                yRot,
                xRot,
                yHeadRot,
                yBodyRot,
                limbPos,
                limbSpeed,
                deathTime,
                tag
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
