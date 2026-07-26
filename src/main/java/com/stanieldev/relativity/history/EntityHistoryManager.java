package com.stanieldev.relativity.history;

import com.stanieldev.relativity.mixin.LivingEntityAccessor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.WalkAnimationState;

import java.util.*;

import static com.stanieldev.relativity.config.RelativityConfig.TEMP_MAX_TIME_RETARDATION;

public class EntityHistoryManager {

    // Entity history storage
    private static final Map<UUID, EntityHistory> histories = new HashMap<>();
    public static void record(Entity entity, long tick) {
        EntityHistory history = histories.computeIfAbsent(entity.getUUID(), id -> new EntityHistory());

        CompoundTag tag = new CompoundTag();
        entity.saveWithoutId(tag);

        tag.putFloat("RelativityYaw", entity.getYRot());
        tag.putFloat("RelativityPitch", entity.getXRot());

        if (entity instanceof LivingEntity living) {
            tag.putFloat("RelativityHeadYaw", living.getYHeadRot());

            if (living instanceof Mob mob) {
                tag.putFloat("RelativityBodyYaw", mob.yBodyRot);
            }
        }

        history.add(new EntitySnapshot(
                tick,
                entity.position(),
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
